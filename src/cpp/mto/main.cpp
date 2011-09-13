#include <iostream>

#include <boost/asio.hpp>
#include <boost/asio/ip/tcp.hpp>
#include <boost/bind.hpp>
#include <boost/unordered_set.hpp>
#include <boost/unordered_map.hpp>

#include "messages.h"
#include "options.h"
#include "connection.h"


using namespace std;
using namespace boost;
using namespace boost::asio;
using namespace boost::asio::ip;
using namespace boost::system;


void onFirstConnectionToPeerEstablished();
void establishConnection();


io_service ioService;
 
/** Ports described as 'listening' */
unordered_set<pair<unsigned int, unsigned short> > availablePorts;


/** Open connections tunneled via proxy */
unordered_map<Identifier, Connection*> remoteConnections;


/** Connections between proxies. First established is pointed by mainSocket ptr */
tcp::socket peerAccept(ioService), peerConnect(ioService), *mainSocket;

/**
 * This class waits on internal socket, i.e. socket accepting connections from inside of the cluster.
 * Once a connection to peer is established, this class starts an accept (event) loop
 */
class IndoorAcceptHandler
{
public:

  /** Ptr to new socket */
  tcp::socket * s;
  
  /** Reference for accepting socket */
  tcp::acceptor & acceptor;
  
  IndoorAcceptHandler(tcp::acceptor & acc) : acceptor(acc){ s = new tcp::socket(ioService); }
  
  void start_accept() { acceptor.async_accept(*s, *this); }
  
  /** Triggered on accept */
  void operator()(const error_code& ec)
  {
    if (!ec)
    {
      handle();
      s = new tcp::socket(ioService);
      start_accept();
    }
    else
    {
      cerr << "Aborting, got error: " << ec.message() << endl;
      exit(1);
    }
  }
  
  /** Once accept succeded, reads the header and constructs a connection */
  void handle()
  {
    char * reqBuf = new char[Request::getSize()];
    Connection * conn = new Connection(s, reqBuf);
    async_read(*s,
               buffer(reqBuf, Request::getSize()),
               transfer_all(),
               bind(&Connection::readRequest, conn, placeholders::error, placeholders::bytes_transferred));
  }
};


/**
 * The part resposible for interconnection between two proxies.
 * 
 * Two instances arise (one for connection 1->2, second for 2->1 )
 */
class PeerConnectionHandler
{
public:
  /** Whether this side has been doing connect or accept */
  enum WhichConnection{Connect, Accept} whichConnection;

  /** Ptr to this inter-proxy socket */
  tcp::socket * socket;
  
  char * dataBufffer;
  
  /** Ptr to the acceptor, kept in order to free it */
  tcp::acceptor * acc;
  
  PeerConnectionHandler(WhichConnection type, tcp::acceptor * peerAcceptor = 0) 
    : whichConnection(type), acc(peerAcceptor)
  {
  }
  
  /** Fires on accept / connect */
  void ConnectionEstablished(const error_code& ec)
  {
    if(ec)
    {
      // first connection may fail
      static bool firstTime = true;
      if(firstTime)
      {
        cerr << "First connection failed: " << ec.message() << endl;
        firstTime = false;
        delete this;
        return;
      }
      
      // if seconf fails as well, indicate error and close the app
      cerr << "Aborting, got error: " << ec.message() << endl;
      exit(1);
      return;
    }

    // prints informationa about connection success
    switch(whichConnection)
    {
      case Accept:
        socket = &peerAccept;
        delete acc;
        cerr << "Established connection with peer (accepted)" << endl;
        break;
      case Connect:
        socket = &peerConnect;
        cerr << "Established connection with peer (connected)" << endl;
        break;
    }
    
    // Sets one of te connections as main
    if(peerAccept.is_open())
      mainSocket = &peerAccept;
    else if(peerConnect.is_open())
      mainSocket = &peerConnect;
    else
    {
      cerr << "wat? (should never happen)" << endl;
      exit(1);
    }

    // Start normal operation
    startReadHeader();
    
    // Start indoor accepting thread
    onFirstConnectionToPeerEstablished();
  }
  
  /** Starts reading new header from peer */
  void startReadHeader()
  {
    dataBufffer = new char[Header::getSize()];
    async_read(*socket, buffer(dataBufffer, Header::getSize()), transfer_all(), 
               bind(&PeerConnectionHandler::readHeader, this, placeholders::error, placeholders::bytes_transferred));
  }
  
  /** Parses header */
  void readHeader(const error_code& e, size_t)
  {
    if(e)
    {
      cerr << "Aborting, got error: " << e.message() << endl;
      exit(1);
    }
    
    Header h = Header::read(dataBufffer);
    delete [] dataBufffer;
    switch(h.type)
    {
      case Header::Connect:
        handleConnect(h);
      break;
      case Header::ConnectResponse:
        handleConnectResponse(h);
      break;
      case Header::Data:
        handleData(h);
      break;
      case Header::Close:
        handleClose(h);
      break;
      default:
        cerr << "Unknown message: " << h.type << "!" << endl;
        exit(1);
    }
  }

protected:

  /* ====== Connect ====== */
  
  /** On connect, check if we have registered ip:port and try to connect */
  void handleConnect(Header h)
  {
    startReadHeader();
    
    h.type = Header::ConnectResponse;
    if(availablePorts.find(pair<unsigned int, unsigned short>(h.dstAddress, h.dstPort))==availablePorts.end())
    {
      handleConnectFailed(h);
      return;
    }
    
    tcp::endpoint target(address_v4(h.dstAddress), h.dstPort);
    tcp::socket * s = new tcp::socket(ioService);
    s->async_connect(target, HandleConnected(h, s, this));
  }
  
  void handleConnectFailed(Header h)
  {
    // indicate fail
    h.length = 1;
        
    char * buf = new char[h.getSize()];
    h.write(buf);
    async_write(*socket, buffer(buf, h.getSize()), transfer_all(), Bufferfreeer(buf));
    
  }
  
  /** Once connection requested by peer has been established or faled, this is called */
  struct HandleConnected
  {
    Header h;
    tcp::socket * s;
    PeerConnectionHandler * t;
    HandleConnected(Header header, tcp::socket * socket, PeerConnectionHandler * thiz) : h(header), s(socket), t(thiz){}
    void operator () (const error_code& e)
    {
      if(e)
      {
        t->handleConnectFailed(h);
        delete s;
        return;
      }
      
      // indicate success
      h.length = 0;
      
      char * buf = new char[h.getSize()];
      h.write(buf);
      async_write(*mainSocket, buffer(buf, h.getSize()), transfer_all(), Bufferfreeer(buf));
      Connection * c = new Connection(h, s);
      
    }
  };
  
  /* ====== ConnectResponse ====== */
  
  /** Informs proper Connection that it's remote end is / is not available */
  void handleConnectResponse(Header h)
  {
    startReadHeader();
    
    Identifier id(h);
    if(remoteConnections.find(id) == remoteConnections.end())
    {
      cerr << "Got response for unwanted connection!" << endl;
      return;
    }
    
    remoteConnections[id]->connectedRemote(h);
  }
  
  /* ====== Data ====== */
  
  void handleData(Header h)
  {
    char * data = new char[h.length];
    async_read(*socket, buffer(data, h.length), transfer_all(), * new DataForwarder(data, h, this));
  }
  
  /** Readsthe data and forewards it to proper local connction */
  struct DataForwarder
  {
    DataForwarder(char * d, Header h, PeerConnectionHandler * t) : data(d), header(h), thiz(t) {}
    char * data;
    Header header;
    PeerConnectionHandler * thiz; 
    void operator() (const error_code& e, size_t s){
      if(remoteConnections.find(Identifier(header)) == remoteConnections.end())
        cerr << "Sending data to unexisting dest?!" << endl;
      else
        remoteConnections[Identifier(header)]->remoteToLocal( data, header.length );
      thiz->startReadHeader();
      // data is deleted in remoteToLocal
    }
  };
  
  /* ====== Close ====== */
  
  void handleClose(Header h)
  {
    startReadHeader();
    
    if(remoteConnections.find(Identifier(h)) == remoteConnections.end())
      cerr << "Closing not established connection?!" << endl;
    else
      remoteConnections[Identifier(h)]->remoteClosed();
     
  }
  
  /* ================== */
  
  /** Deletes underlying data buffer once the operation completes */
  struct Bufferfreeer {
    Bufferfreeer(char*d) : data(d) {}
    char * data;
    void operator ()(const error_code& e, size_t)
    {
      delete data;
      if(e) {
        cerr << "Communication failed!" << endl;
        exit(1);
      };
    };
  };
};


/** On first call to this method, starts the IndoorAcceptHandler */
void onFirstConnectionToPeerEstablished()
{
  static int first_time = true;
  if (!first_time) return;
  first_time=false;
  
  tcp::acceptor * indoorAcceptor = new tcp::acceptor(ioService, indoorEndpoint);
  
  IndoorAcceptHandler * handler = new IndoorAcceptHandler(*indoorAcceptor);
  
  handler->start_accept();
}


/** Starts establishing connection with peer proxy */
void establishConnection()
{
  tcp::acceptor * peerAcceptor =  new tcp::acceptor(ioService, my_address);
  
  PeerConnectionHandler * acceptH = new PeerConnectionHandler(PeerConnectionHandler::Accept, peerAcceptor);
  peerAcceptor->async_accept(peerAccept, bind(&PeerConnectionHandler::ConnectionEstablished, acceptH, placeholders::error));
  
  PeerConnectionHandler * connectH = new PeerConnectionHandler(PeerConnectionHandler::Connect);
  peerConnect.async_connect(*peer_address, bind(&PeerConnectionHandler::ConnectionEstablished, connectH, placeholders::error));
}


int main(int argc, char **argv)
{
  if(!loadOptions(argc, argv))
    return 1;
  
  establishConnection();

  error_code e;
  
  // loop!
  while(!e) ioService.run(e);
  
  return 0;
}


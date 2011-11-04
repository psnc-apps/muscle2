#include <iostream>
#include <map>
#include <set>
#include <algorithm>

#include <boost/asio.hpp>
#include <boost/asio/ip/tcp.hpp>
#include <boost/bind.hpp>
#include <boost/unordered_set.hpp>
#include <boost/unordered_map.hpp>
#include <boost/foreach.hpp>

#include <unistd.h>
#include <signal.h>

#include "main.hpp"
#include "messages.hpp"
#include "options.hpp"
#include "connection.hpp"
#include "topology.hpp"
#include "peerconnectionhandler.hpp"

using namespace std;
using namespace boost;
using namespace boost::asio;
using namespace boost::asio::ip;
using namespace boost::system;

#define foreach         BOOST_FOREACH
#define reverse_foreach BOOST_REVERSE_FOREACH

// // // // //      Forward declarations      // // // // //

struct MtoPeer;

void startListeningForClients();

void startConnectingToPeers();

/** Starts establishing connection with a peer proxy */
void startConnectingToPeer(boost::asio::ip::tcp::endpoint where);

void writeHellos(tcp::socket * sock);

/** Parses the Hello from newly connected MTO and forwards it to all */
void parseHello(const MtoHello & hello, PeerConnectionHandler * handler);

PeerConnectionHandler * parseHellos(tcp::socket * sock, vector<MtoHello> & hellos);


// // // // //           Varialbles           // // // // //

io_service ioService;

const posix_time::time_duration peerReconnectTimeout = posix_time::seconds(10);

/** Ports described as 'listening' */
unordered_set<pair<unsigned int, unsigned short> > availablePorts;

/** Open connections tunneled via proxy */
unordered_map<Identifier, Connection*> remoteConnections;

map<string, mto_config> mtoConfigs;
// bimap<string, unsigned short> mtoIdToName;

/** Maps port range max to a peer connection */
map<unsigned short, MtoPeer> peers; 

// // // // //        Classes & Structs       // // // // //

/** Information about an MTO peer */
struct MtoPeer
{
  unsigned short min;                       ///< Lower bound for the port range
  PeerConnectionHandler * peerConnection;   ///< Where to send messages to the MTO
  MtoHello bestHello;                       ///< The Hello for this MTO
};

/** Helper to delete data on completion of the operation */
struct Bufferfreeer {
  Bufferfreeer(char * d) : data(d){}
  void operator ()(const error_code& e, size_t){delete [] data;}
private:
  char * data;
};


/**
 * This class waits on internal socket, i.e. socket accepting connections from inside of the cluster.
 */
class InternalAcceptor
{
private:

  /** Ptr to new socket */
  tcp::socket * s;
  
  /** Reference for accepting socket */
  tcp::acceptor & acceptor;
  
public:
  
  InternalAcceptor(tcp::acceptor & acc);
  
  void startAccepting();
  
  /** Triggered on accept */
  void operator()(const error_code& ec);
  
  /** Once accept succeded, reads the header and constructs a connection */
  void handle();
};

inline InternalAcceptor::InternalAcceptor(tcp::acceptor& acc) 
   : acceptor(acc)
{
  s = new tcp::socket(ioService); 
}

inline void InternalAcceptor::startAccepting()
{
  acceptor.async_accept(*s, *this);
}
void InternalAcceptor::operator()(const boost::system::error_code& ec)
{
  if (!ec)
  {
    handle();
    s = new tcp::socket(ioService);
    startAccepting();
  }
  else
  {
    Logger::error(Logger::MsgType_PeerConn, "Failed to listen on incoming internal connections on %s:%hu. Got error: %s. Aborting.",
                  acceptor.local_endpoint().address().to_string().c_str(),
                  acceptor.local_endpoint().port(),
                  ec.message().c_str()
            );
    exit(1);
  }
}

void InternalAcceptor::handle()
{
  char * reqBuf = new char[Request::getSize()];
  Connection * conn = new Connection(s, reqBuf);
  async_read(*s,
              buffer(reqBuf, Request::getSize()),
              transfer_all(),
              bind(&Connection::readRequest, conn, placeholders::error, placeholders::bytes_transferred)
            );
}

/**
 * Once a connection between MTO's is established, this class is ued to read all Hellos
 * before starting normal communication.
 */
struct HelloReader
{
private:
  tcp::socket * sock;
  char * buf;
  vector<MtoHello>& hellos;
  function< void(const error_code & ec) > callback;
  
public:
  HelloReader(tcp::socket * _sock, function< void(const error_code & ec) > callback_, vector<MtoHello>& hellos_);
  
  void operator()(const error_code & ec, size_t);
};

inline HelloReader::HelloReader(tcp::socket* _sock, boost::function< void(const error_code & ec) > callback_, vector< MtoHello >& hellos_)
   : sock(_sock), callback(callback_), hellos(hellos_)
{
  buf = new char[MtoHello::getSize()];
  async_read(*sock, buffer(buf, MtoHello::getSize()), *this);
}


void HelloReader::operator()(const boost::system::error_code& ec, size_t )
{
  if(ec)
  {
    delete [] buf;
    callback(ec);
    return;
  }
  MtoHello hello = MtoHello::deserialize(buf);
  hellos.push_back(hello);
  if(hello.isLastMtoHello)
  {
    delete [] buf;
    callback(error_code());
    return;
  }
  // Read next hello
  async_read(*sock, buffer(buf, MtoHello::getSize()), *this);
}


/**
 * Connects to a given peer until success. Retries every peerReconnectTimeout.
 * On successfull connection creates PeerConnectionHandler and finihses
 */
struct StubbornConnecter
{
private:
  tcp::endpoint where;
  deadline_timer * timeout;
  tcp::socket * sock;
  vector<MtoHello> hellos;
  void connectSucceeded();
  
public:
  StubbornConnecter(tcp::endpoint where_);
  
  /** Executed after no responce has been received (or at abort) */
  void timeoutFired(const error_code& ec);
  
  /** Executed when connecting state changed (new connection, or some error)  */
  void connectFired(const error_code& ec);  
  
  void allHellosRead(const error_code & ec);
};

StubbornConnecter::StubbornConnecter(tcp::endpoint where_)
   : timeout(new deadline_timer(ioService, peerReconnectTimeout)),
     where(where_), sock(new tcp::socket(ioService))
{
  timeout->async_wait(bind(&StubbornConnecter::timeoutFired, this, placeholders::error()));
  sock->async_connect(where, bind(&StubbornConnecter::connectFired, this, placeholders::error()));
}

void StubbornConnecter::timeoutFired(const boost::system::error_code& ec)
{
  if(ec == error::operation_aborted)
    return;
  sock->close();
  delete sock;
  sock = new tcp::socket(ioService);
  sock->async_connect(where, bind(&StubbornConnecter::connectFired, this, placeholders::error()));
  timeout->expires_from_now(peerReconnectTimeout);
  timeout->async_wait(bind(&StubbornConnecter::timeoutFired, this, placeholders::error()));
}

void StubbornConnecter::connectFired(const boost::system::error_code& ec)
{
  if(ec == error::operation_aborted)
    return;
  if(ec == error::connection_refused)
  {
    Logger::info(Logger::MsgType_PeerConn, "Connection refused to %s:%hu - will retry later",
      where.address().to_string().c_str(), where.port()
    );
    return;
  }
  timeout->cancel();
  if(ec)
  {
    Logger::error(Logger::MsgType_PeerConn, "While connecting to  %s:%hu encountered error %s. Aborting connection.",
      where.address().to_string().c_str(), where.port(), ec.message().c_str()
    );
    return;
  }
  connectSucceeded();  
}

void StubbornConnecter::connectSucceeded()
{
  Logger::trace(Logger::MsgType_PeerConn, "Connected to peer  %s:%hu, starting hello exchange",
      where.address().to_string().c_str(), where.port()
    );
  writeHellos(sock);
  HelloReader(sock,
              function<void(const error_code & ec)>(bind(&StubbornConnecter::allHellosRead, this, placeholders::error())),
              hellos
              );
}

void StubbornConnecter::allHellosRead(const boost::system::error_code& ec)
{
  PeerConnectionHandler * h = parseHellos(sock, hellos);
  h->setReconnect(true);
//     Logger::trace(Logger::MsgType_PeerConn, "Connection to peer  %s:%hu succeeded!",
//         where.address().to_string().c_str(), where.port()
//       );
  delete this;
}

/**
 * Once a connection is accepted, this class takes responsibility for creating a PeerConnectionHandler
 */
struct InitPeerConnection
{
private:
  tcp::socket * sock;
  char * buf;
  vector<MtoHello> hellos;
  
public:
  InitPeerConnection(tcp::socket * _sock);
  void allHellosReceived(error_code ec);
};

InitPeerConnection::InitPeerConnection(tcp::socket* _sock)
   : sock(_sock)
{
  buf = new char[MtoHello::getSize()];
  function< void(error_code ec) > callback( bind(&InitPeerConnection::allHellosReceived, this, placeholders::error()));
  HelloReader(sock, callback, hellos);
}

void InitPeerConnection::allHellosReceived(error_code ec)
{
  if(ec)
  {
    Logger::trace(Logger::MsgType_PeerConn, "Reading hellos from peer %s:%hu failed - occured error: %s",
                  sock->remote_endpoint().address().to_string().c_str(), sock->remote_endpoint().port(), ec.message().c_str()
            );
    sock->close();
    delete this;
    return;
  }
  writeHellos(sock);  
  parseHellos(sock, hellos);
  delete this;
}

struct ExternalAcceptor
{
private:
  tcp::socket * sock;
  tcp::acceptor * peerAcceptor;

public:
  ExternalAcceptor(tcp::acceptor * _peerAcceptor) : peerAcceptor(_peerAcceptor) {}
  void startAccepting();
  void operator()(const error_code & ec);
};

void ExternalAcceptor::startAccepting()
{
  sock = new tcp::socket(ioService);
  peerAcceptor->async_accept(*sock, *this);
}

void ExternalAcceptor::operator()(const boost::system::error_code& ec)
{
  if(ec)
  {
    Logger::error(Logger::MsgType_PeerConn, "Failed to listen on incoming external connections on %s:%hu. Got error: %s. Aborting.",
                peerAcceptor->local_endpoint().address().to_string().c_str(),
                peerAcceptor->local_endpoint().port(),
                ec.message().c_str()
          );
    exit(1);
  }
  
  Logger::trace(Logger::MsgType_PeerConn, "Accepted peer connection  %s:%hu, starting hello exchange",
      sock->remote_endpoint().address().to_string().c_str(), sock->remote_endpoint().port()
    );
  
  new InitPeerConnection(sock);
  startAccepting();
}

// // // // //         Free functions         // // // // //

PeerConnectionHandler * getPeer(unsigned short port)
{
  map< unsigned short, MtoPeer >::iterator it = peers.lower_bound(port);
  if(it == peers.end())
    return 0;
  MtoPeer & candidate = it->second;
  if(port < candidate.min)
    return 0;
  return candidate.peerConnection;
}

void peerDied(PeerConnectionHandler* handler, bool reconnect)
{
  for(map< unsigned short, MtoPeer >::iterator it = peers.begin(); it != peers.end(); ++it)
    if(it->second.peerConnection==handler)
      peers.erase(it);
  
  unordered_map< Identifier, Connection* > rcc(remoteConnections);
  for(unordered_map< Identifier, Connection* >::iterator it = rcc.begin(); it != rcc.end(); ++it)
    it->second->peerDied(handler);
    
  if(reconnect)
  {
    tcp::endpoint target = handler->getSocket()->remote_endpoint();
    startConnectingToPeer(target);
  }
}

void startListeningForClients()
{
  tcp::acceptor * indoorAcceptor = new tcp::acceptor(ioService, Options::getInstance().getInternalEndpoint());
  
  InternalAcceptor * handler = new InternalAcceptor(*indoorAcceptor);
  
  handler->startAccepting();
}

void writeHellos(tcp::socket * sock)
{
  typedef pair<unsigned short, MtoPeer > peerpair;
  foreach( peerpair peer , peers)
  {
    MtoHello hello = peer.second.bestHello;
    hello.distance++;
    hello.isLastMtoHello=false;
    char * data = new char[MtoHello::getSize()];
    hello.serialize(data);
    async_write(*sock, buffer(data, MtoHello::getSize()), Bufferfreeer(data));
  }
  
  MtoHello myHello;
  myHello.distance=0;
  myHello.isLastMtoHello=true;
  myHello.portLow=Options::getInstance().getLocalPortLow();
  myHello.portHigh=Options::getInstance().getLocalPortHigh();
  char * data = new char[MtoHello::getSize()];
  myHello.serialize(data);
  async_write(*sock, buffer(data, MtoHello::getSize()), Bufferfreeer(data));
}

void parseHello(const MtoHello & hello, PeerConnectionHandler * handler)
{
  Logger::debug(Logger::MsgType_PeerConn|Logger::MsgType_ClientConn,
                "Parsing hello: %s", hello.str().c_str()
               );
  if(peers.find(hello.portHigh)!=peers.end())
    { // already known
      MtoPeer & peer = peers[hello.portHigh];
      if(peer.min!=hello.portLow)
      {
        Logger::error(Logger::MsgType_PeerConn|Logger::MsgType_Config,
                      "Port ranges %s and %s overlap. Ignoring second.",
                      peer.bestHello.str().c_str(), hello.str().c_str()
                );
        return;
      }
      if(peer.bestHello.distance > hello.distance)
      {
        peer.bestHello = hello;
        peer.peerConnection = handler;
      }
    }
    else
    { // new
      if(peers.size())
      {
        map<unsigned short, MtoPeer >::iterator itH = peers.lower_bound(hello.portHigh), itL = peers.lower_bound(hello.portLow);
        if(itH != peers.end() && itH->second.min < hello.portLow)
        {
          Logger::error(Logger::MsgType_PeerConn|Logger::MsgType_Config,
                        "Port ranges %s and %s overlap. Ignoring second.",
                        itH->second.bestHello.str().c_str(), hello.str().c_str()
                  );
          return;
        }
        if ( itL != itH )
        {
          Logger::error(Logger::MsgType_PeerConn|Logger::MsgType_Config,
                        "Port ranges %s and %s overlap. Ignoring second.",
                        itL->second.bestHello.str().c_str(), hello.str().c_str()
                  );
          return;
        }
      }
      MtoPeer peer;
      peer.bestHello=hello;
      peer.min = hello.portLow;
      peer.peerConnection = handler;
      peers[hello.portHigh] = peer;
    }
}

PeerConnectionHandler * parseHellos(tcp::socket * sock, vector<MtoHello> & hellos) {
  PeerConnectionHandler * handler = new PeerConnectionHandler(sock);
  
  for(map<unsigned short, MtoPeer>::const_iterator it = peers.begin(); it!=peers.end(); ++it)
    it->second.peerConnection->propagateHellos(hellos);
  
  foreach(MtoHello hello, hellos)
    parseHello(hello, handler);
  
  handler->connectionEstablished();
  
  return handler;
}

void helloReceived(Header h, PeerConnectionHandler* receiver)
{
  MtoHello hello;
  hello.portLow = h.srcPort;
  hello.portHigh = h.dstPort;
  hello.distance=h.length;
  parseHello(hello, receiver);
  
  h.length++;
  
  set<PeerConnectionHandler* > toUpdate;
  for(map< unsigned short, MtoPeer >::iterator it = peers.begin(); it != peers.end(); ++it)
    toUpdate.insert(it->second.peerConnection);
  toUpdate.erase(receiver);
  
  foreach(PeerConnectionHandler * handler, toUpdate)
    handler->propagateHello(hello);
}


void startConnectingToPeer(tcp::endpoint where)
{
  new StubbornConnecter(where);
}

void startConnectingToPeers()
{
  const vector<string> & connects =  mtoConfigs[Options::getInstance().getMyName()].connectsTo;
  
  tcp::resolver rslvr(ioService);
  
  foreach(string mto, connects)
  {
    error_code e;
    tcp::resolver::iterator it = rslvr.resolve(tcp::resolver::query(mtoConfigs[mto].address, mtoConfigs[mto].port), e);
    if(e)
    {
      Logger::error(Logger::MsgType_PeerConn|Logger::MsgType_Config, 
                    "Cannot resolve %s:%s (error %s). Ignoring %s MTO.",
                    mtoConfigs[mto].address.c_str(), mtoConfigs[mto].port.c_str(),
                    e.message().c_str(), mto.c_str()
                   );
      continue;
    }
    
    tcp::endpoint where = *it;
    
    startConnectingToPeer(where);
  }
}

// Reaction on signal - currantyl sigint
void signalReceived(int signum)
{
  Logger::info(-1, "Received %s, (brute force) exiting...", (signum==SIGINT?"SIGINT":"unknown signal(?)"));
  
  ioService.stop();
  
  Logger::closeLogFile();
  
  exit(0);
}

int main(int argc, char **argv)
{
  Options & opts = Options::getInstance();
  
  if(!opts.load(argc, argv))
    return 1;
  
  if(!loadTopology(opts.getTopologyFilePath().c_str(), mtoConfigs))
    return 1;
  
  string myName = opts.getMyName();
  
  if(mtoConfigs.find(myName) == mtoConfigs.end()){
    Logger::error(-1, "The name of this MTO (%s) could not be found in the topology file!", myName.c_str());
    return 1;
  }
  
  error_code e;
  
  bool anyoneConnectsToMe = false;
  for(map<string, mto_config>::const_iterator it = mtoConfigs.begin(); it != mtoConfigs.end(); ++it)
    foreach(const string & s, it->second.connectsTo)
      if(s==myName)
      {
        anyoneConnectsToMe = true;
        goto doubleLoopBreak;
      }
  doubleLoopBreak:

  if(anyoneConnectsToMe) 
  {
    tcp::resolver::iterator it = tcp::resolver(ioService).resolve(tcp::resolver::query(mtoConfigs[myName].address, mtoConfigs[myName].port), e);
    if(e)
    {
      Logger::error(-1, "Address of this MTO (%s:%s) not understod!",
        mtoConfigs[myName].address.c_str(), mtoConfigs[myName].port.c_str()
      );
      return 1;
    }
    
    tcp::endpoint myAddress = *it;
  
    Logger::info(Logger::MsgType_Config|Logger::MsgType_PeerConn, "Starting external acceptor on %s:%hu",
                 myAddress.address().to_string().c_str(), myAddress.port()
            );
    tcp::acceptor * peerAcceptor = new tcp::acceptor(ioService, myAddress);
    ExternalAcceptor * exAcc = new ExternalAcceptor(peerAcceptor);
    exAcc->startAccepting();
  } else {
    Logger::info(Logger::MsgType_Config|Logger::MsgType_PeerConn, "External acceptor unnecessary, not starting one");
  }
  
  startConnectingToPeers();
  
  startListeningForClients();
  
  signal(SIGINT, signalReceived);
  
  if(opts.getDaemonize())
  {
    Logger::info(-1, "Daemonizing...");
    daemon(0,1);
  }
  
  while(!e) ioService.run(e);
  
  return 0;
}


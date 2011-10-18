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
// #include <boost/bimap.hpp>

#include "main.h"
#include "messages.h"
#include "options.h"
#include "connection.h"
#include "topology.h"
#include "peerconnectionhandler.h"

using namespace std;
using namespace boost;
using namespace boost::asio;
using namespace boost::asio::ip;
using namespace boost::system;

#define foreach         BOOST_FOREACH
#define reverse_foreach BOOST_REVERSE_FOREACH

struct MtoPeer;
void startListeningForClients();
void startConnectingToPeers();

io_service ioService;
 
/** Ports described as 'listening' */
unordered_set<pair<unsigned int, unsigned short> > availablePorts;

/** Open connections tunneled via proxy */
unordered_map<Identifier, Connection*> remoteConnections;

map<string, mto_config> mtoConfigs;
// bimap<string, unsigned short> mtoIdToName;

/** Maps port range max to a peer connection */
map<unsigned short, MtoPeer> peers; 

struct MtoPeer
{
  unsigned short min;
  PeerConnectionHandler * peerConnection;
  MtoHello bestHello;
};

struct Bufferfreeer {
  Bufferfreeer(char * d) : data(d){}
  char * data;
  void operator ()(const error_code& e, size_t){delete data;}
};

/** Gets MTO that handles this port. Returns 0 if no such MTO exists. */
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

/** Gets MTO that handles this port. Returns 0 if no such MTO exists. */
inline tcp::socket * getPeerSocket(unsigned short port)
{
  PeerConnectionHandler* peer = getPeer(port);
  if(peer)
    return peer->getSocket();
  return 0;
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


/**
 * This class waits on internal socket, i.e. socket accepting connections from inside of the cluster.
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
      cerr << "Failed to listen on incoming internal connections. Aborting, got error: " << ec.message() << endl;
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


/** starts the IndoorAcceptHandler */
void startListeningForClients()
{
  tcp::acceptor * indoorAcceptor = new tcp::acceptor(ioService, internalEndpoint);
  
  IndoorAcceptHandler * handler = new IndoorAcceptHandler(*indoorAcceptor);
  
  handler->start_accept();
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
    hello.write(data);
    async_write(*sock, buffer(data, MtoHello::getSize()), Bufferfreeer(data));
  }
  
  MtoHello myHello;
  myHello.distance=0;
  myHello.isLastMtoHello=true;
  myHello.portLow=localPortLow;
  myHello.portHigh=localPortHigh;
//   myHello.mtoId = mtoIdToName.left.at(myName);
  char * data = new char[MtoHello::getSize()];
  myHello.write(data);
  async_write(*sock, buffer(data, MtoHello::getSize()), Bufferfreeer(data));
}

void parseHello(const MtoHello & hello, PeerConnectionHandler * handler)
{
  clog << "Parsing hello: " << hello.portLow << ":" << hello.portHigh << endl;
  if(peers.find(hello.portHigh)!=peers.end())
    { // already known
      MtoPeer & peer = peers[hello.portHigh];
      if(peer.min!=hello.portLow)
      {
        cerr << "AAAghr! Port ranges overlap (1). Ignoring second." << endl;
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
          cerr << "AAAghr! Port ranges overlap (2). Ignoring second." << endl;
          return;
        }
        if ( itL != itH )
        {
          cerr << "AAAghr! Port ranges overlap (3). Ignoring second." << endl;
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

struct HelloReader
{
  tcp::socket * sock;
  char * buf;
  vector<MtoHello>& hellos;
  function< void(const error_code & ec) > hook;
  
  HelloReader(tcp::socket * _sock, function< void(const error_code & ec) > hook_, vector<MtoHello>& hellos_)
    : sock(_sock), hook(hook_), hellos(hellos_)
  {
    buf = new char[MtoHello::getSize()];
    async_read(*sock, buffer(buf, MtoHello::getSize()), *this);
  }
  
  void operator()(const error_code & ec, size_t)
  {
    if(ec)
    {
      hook(ec);
      return;
    }
    MtoHello hello = MtoHello::read(buf);
    hellos.push_back(hello);
    if(hello.isLastMtoHello)
    {
      hook(error_code());
      return;
    }
    // Read next hello
    async_read(*sock, buffer(buf, MtoHello::getSize()), *this);
  }
  
};

struct StubbornConnecter
{
  tcp::endpoint where;
  deadline_timer * timeout;
  tcp::socket * sock;
  vector<MtoHello> hellos;
  
  StubbornConnecter(tcp::endpoint where_)
    : timeout(new deadline_timer(ioService, posix_time::seconds(10))),
      where(where_), sock(new tcp::socket(ioService))
  {
    timeout->async_wait(bind(&StubbornConnecter::timeoutFired, this, placeholders::error()));
    sock->async_connect(where, bind(&StubbornConnecter::connectFired, this, placeholders::error()));
  }
  
  void timeoutFired(const error_code& ec){
    if(ec == error::operation_aborted)
      return;
    sock->close();
    delete sock;
    sock = new tcp::socket(ioService);
    sock->async_connect(where, bind(&StubbornConnecter::connectFired, this, placeholders::error()));
    timeout->expires_from_now(posix_time::seconds(10));
    timeout->async_wait(bind(&StubbornConnecter::timeoutFired, this, placeholders::error()));
  }
  
  void connectFired(const error_code& ec){
    if(ec == error::operation_aborted)
      return;
    if(ec == error::connection_refused)
    {
      cerr << "Connection refused to " << where.address().to_string() << ":" << where.port() << " - will retry later" << endl;
      return;
    }
    timeout->cancel();
    if(ec)
    {
      cerr << "Got error " << ec.message() << " by " << where.address().to_string() << ":" << where.port() << " - aborting connection" << endl;
      return;
    }
    connectSucceeded();  
  }
  
  void connectSucceeded(){
    writeHellos(sock);
    HelloReader(sock,
                function<void(const error_code & ec)>(bind(&StubbornConnecter::allHellosRead, this, placeholders::error())),
                hellos
               );
  }
  
  void allHellosRead(const error_code & ec){
    PeerConnectionHandler * h = parseHellos(sock, hellos);
    h->setReconnect(true);
    delete this;
  }
};

void startConnectingToPeer(tcp::endpoint where)
{
  new StubbornConnecter(where);
}

/** Starts establishing connection with peer proxy */
void startConnectingToPeers()
{
  const vector<string> & connects =  mtoConfigs[myName].connectsTo;
  
  tcp::resolver rslvr(ioService);
  
  foreach(string mto, connects)
  {
    error_code e;
    tcp::resolver_iterator it = rslvr.resolve(tcp::resolver_query(mtoConfigs[mto].address, mtoConfigs[mto].port), e);
    if(e)
    {
      cerr << "Cannot resolve " << mtoConfigs[mto].address << ":" << mtoConfigs[mto].port
           << " (error: " << e.message() << "). Ignoring " << mto << " MTO." << endl; 
      continue;
    }
    
    tcp::endpoint where = *it;
    
    startConnectingToPeer(where);
  }
}


struct InitPeerConnection
{
  tcp::socket * sock;
  char * buf;
  vector<MtoHello> hellos;
  
  InitPeerConnection(tcp::socket * _sock)
    : sock(_sock)
  {
    buf = new char[MtoHello::getSize()];
    function< void(error_code ec) > callback( bind(&InitPeerConnection::allHellosReceived, this, placeholders::error()));
    HelloReader(sock, callback, hellos);
  }
  
  // TODO: propagate this info.
  void allHellosReceived(error_code ec)
  {
    if(ec)
    {
      cerr << "Error occured by exchanging hellos: " << ec.message() << endl;
      sock->close();
      delete this;
      return;
    }
    writeHellos(sock);  
    parseHellos(sock, hellos);
    delete this;
  }
};

struct ExternalAcceptor
{
  tcp::socket * sock;
  tcp::acceptor * peerAcceptor;

  ExternalAcceptor(tcp::acceptor * _peerAcceptor) : peerAcceptor(_peerAcceptor) {}
  
  void startAccepting()
  {
    sock = new tcp::socket(ioService);
    peerAcceptor->async_accept(*sock, *this);
  }
  
  void operator()(const error_code & ec)
  {
    if(ec)
    {
      cerr << "External acceptor thrown an error: " << ec.message() << endl;
      exit(1);
    }
    
    new InitPeerConnection(sock);
    startAccepting();
  }
};

int main(int argc, char **argv)
{
  if(!loadOptions(argc, argv))
    return 1;
  
  if(!loadTopology(topologyFilePath.c_str(), mtoConfigs))
    return 1;
  
  if(mtoConfigs.find(myName) == mtoConfigs.end()){
    cerr << "The name of this MTO could not be found in the topology file!" << endl;
    return 1;
  }
  
/*  vector<string> names;
  
  typedef pair<string, mto_config> mtoCfgPair;
  foreach(mtoCfgPair p , mtoConfigs)
    names.push_back(p.first);
  sort(names.begin(), names.end());
  
  for ( int i = 0 ; i < names.size(); ++i)
    mtoIdToName.left.insert(pair<string,unsigned short>(names[i],i));*/
  
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
    tcp::resolver_iterator it = tcp::resolver(ioService).resolve(tcp::resolver_query(mtoConfigs[myName].address, mtoConfigs[myName].port), e);
    if(e)
    {
      cerr << "Address of this MTO not understod!" << endl;
      return 1;
    }
    
    tcp::endpoint myAddress = *it;
  
    tcp::acceptor * peerAcceptor = new tcp::acceptor(ioService, myAddress);
    ExternalAcceptor * exAcc = new ExternalAcceptor(peerAcceptor);
    exAcc->startAccepting();
  }
  
  startConnectingToPeers();
  
  startListeningForClients();
  
  while(!e) ioService.run(e);
  
  return 0;
}


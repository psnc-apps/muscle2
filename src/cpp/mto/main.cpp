/*
* Copyright 2010-2013 Multiscale Applications on European e-Infrastructures (MAPPER) project
*
* GNU Lesser General Public License
* 
* This file is part of MUSCLE (Multiscale Coupling Library and Environment).
* 
* MUSCLE is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* MUSCLE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public License
* along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/
#include "constants.hpp"
#include "manager/localmto.h"
#include "net/async_cservice.h"

#include <iostream>
#include <map>
#include <set>
#include <cstdlib>

#include <unistd.h>
#include <signal.h>

using namespace std;

// // // // //           Varialbles           // // // // //
muscle::async_service *asyncService;
LocalMto *localMto;

// Reaction on signal - currently sigint, sigquit and sigterm
void signalReceived(int signum)
{
  unsigned short min;                               ///< Lower bound for the port range
  vector<PeerConnectionHandler *> peerConnection;   ///< Where to send messages to the MTO
  MtoHello bestHello;                               ///< The Hello for this MTO
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
    return; /* XXX: cancelling timeout if refused? */
  }
  timeout->cancel();
  if(ec)
  {
    Logger::error(Logger::MsgType_PeerConn, "While connecting to  %s:%hu encountered error: %s. Aborting connection.",
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

  setSocketOptions(sock);
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
  
  unsigned short hiPort = hellos.back().portHigh;
  connectionsOutgoing[hiPort] = h;
  if(connectionsIncomming.find(hiPort)!=connectionsIncomming.end())
    newConnectionPairFormed(hiPort);
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
  setSocketOptions(_sock);

  buf = new char[MtoHello::getSize()];
  function< void(error_code ec) > callback( bind(&InitPeerConnection::allHellosReceived, this, placeholders::error()));
  HelloReader(sock, callback, hellos);
}

void InitPeerConnection::allHellosReceived(error_code ec)
{
  if(ec)
  {
    Logger::trace(Logger::MsgType_PeerConn, "Reading hellos from peer %s:%hu failed - occurred error: %s",
                  sock->remote_endpoint().address().to_string().c_str(), sock->remote_endpoint().port(), ec.message().c_str()
            );
    sock->close();
    delete this;
    return;
  }
  
  writeHellos(sock);  
  
  PeerConnectionHandler * h = parseHellos(sock, hellos);
  h->setReconnect(true);

  unsigned short hiPort = hellos.back().portHigh;
  connectionsIncomming[hiPort] = h;
  if(connectionsOutgoing.find(hiPort)!=connectionsOutgoing.end())
    newConnectionPairFormed(hiPort);
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

PeerConnectionHandler * getPeer(Header header)
{
  map< unsigned short, MtoPeer >::iterator it = peers.lower_bound(header.dstPort);
  if(it == peers.end())
    return 0;
  MtoPeer & candidate = it->second;
  if(header.dstPort < candidate.min)
    return 0;
  
  int pos = header.dstAddress % candidate.peerConnection.size();
  
  return getPeer(candidate.peerConnection[pos]);
}

PeerConnectionHandler * getPeer(PeerConnectionHandler * peer){
  if(connectionsIncToOut.find(peer)!=connectionsIncToOut.end())
    return connectionsIncToOut[peer];
  return peer;
}

void newConnectionPairFormed(unsigned short portHigh){
  PeerConnectionHandler* inc = connectionsIncomming[portHigh];
  PeerConnectionHandler* out = connectionsOutgoing[portHigh];
  
  for (map<Identifier, Connection*>::iterator it = remoteConnections.begin(); it != remoteConnections.end(); ++it) {
    it->second->replacePeer(inc, out);
  }
  
  set<PeerConnectionHandler*> allHandlers;
  for (map<unsigned short, MtoPeer>::iterator it = peers.begin(); it != peers.end(); ++it)
    foreach(PeerConnectionHandler * h , it->second.peerConnection)
      allHandlers.insert(h);
  
  allHandlers.erase(inc);
  allHandlers.erase(out);
  
  foreach(PeerConnectionHandler * h , allHandlers)
    h->replacePeer(inc, out);
  
  // new connections will use proper PeerConnectionHandler
  connectionsIncToOut[inc] = out;
  
  //out->enableAutoClose(true);
}

template <typename M, typename V> bool removeFirstKeyFromMap (M m, V v)
{
  for(typename M::iterator it = m.begin(); it != m.end(); ++it)
  {
    if(v == it->second){
      m.erase(it);
      return true;
    }
    else
    {
        const char *s;
        switch (signum) {
            case SIGINT:
                s = "SIGINT";
                break;
            case SIGTERM:
                s = "SIGTERM";
                break;
            default:
                s = "unknown signal(?)";
                break;
        }
        Logger::info(-1, "Received %s, exiting...", s);

        asyncService->done();
        delete localMto;
        
        Logger::closeLogFile();
        
        exit(0);
    }
}

int main(int argc, char **argv)
{
    try {
        Options opts(argc, argv);
        
        map<string, muscle::endpoint> mtoConfigs;

        if(!loadTopology(opts.getTopologyFilePath(), mtoConfigs))
            return 1;
        
        string myName = opts.getMyName();
        
        if(opts.getDaemonize())
        {
            Logger::info(-1, "Daemonizing...");
            daemon(0,1);
        }
        
        if(mtoConfigs.find(myName) == mtoConfigs.end()){
            Logger::error(-1, "The name of this MTO (%s) could not be found in the topology file", myName.c_str());
            return 1;
        }

bool parseHello(const MtoHello & hello, PeerConnectionHandler * handler)
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
      return false;
    }
    if(peer.bestHello.distance > hello.distance)
    {
      peer.peerConnection.clear();
      peer.bestHello=hello;
      peer.peerConnection.push_back(handler);
      return true;
    } 
    if(peer.bestHello.distance == hello.distance) {
      peer.peerConnection.push_back(handler);
    }
    return false;
  }
  
  string myName = Options::getInstance().getMyName();
 
  int myH = Options::getInstance().getLocalPortHigh();
  int myL = Options::getInstance().getLocalPortLow();
  
  // rebounce of mine hello?
  if(myH==hello.portHigh && myL==hello.portLow)
    return false;
  
  // overapping with myself?
  if(myH>hello.portHigh)
  {
    if(myL<=hello.portHigh)
    {
      Logger::error(Logger::MsgType_PeerConn|Logger::MsgType_Config,
                    "My port range and %s overlap. Ignoring second.",
                    hello.str().c_str()
              );
      return false;
    }
  }
  else
  {
    if(myH >= hello.portLow)
    {
      Logger::error(Logger::MsgType_PeerConn|Logger::MsgType_Config,
                    "My port range and %s overlap. Ignoring second.",
                    hello.str().c_str()
              );
      return false;
    } 
  }
  
  // new
  if(peers.size())
  {
    map<unsigned short, MtoPeer >::iterator itH = peers.lower_bound(hello.portHigh), itL = peers.lower_bound(hello.portLow);
    if(itH != peers.end() && itH->second.min < hello.portLow)
    {
      Logger::error(Logger::MsgType_PeerConn|Logger::MsgType_Config,
                    "Port ranges %s and %s overlap. Ignoring second.",
                    itH->second.bestHello.str().c_str(), hello.str().c_str()
              );
      return false;
    }
    if ( itL != itH )
    {
      Logger::error(Logger::MsgType_PeerConn|Logger::MsgType_Config,
                    "Port ranges %s and %s overlap. Ignoring second.",
                    itL->second.bestHello.str().c_str(), hello.str().c_str()
              );
      return false;
    }
  }
  MtoPeer peer;
  peer.min = hello.portLow;
  peer.bestHello=hello;
  peer.peerConnection.push_back(handler);
  peers[hello.portHigh] = peer;
  return true;
}

        if (externalAddress.port)
        {
            try {
                externalAddress.resolve();
            }
            catch (muscle::muscle_exception& ex)
            {
                Logger::error(-1, "Cannot resolve MTO external address (%s)!", externalAddress.str().c_str());
                return 1;
            }
        }

        asyncService = new muscle::async_cservice;
        localMto = new LocalMto(opts, asyncService, externalAddress);
        
        if(externalAddress.port)
            localMto->startListeningForPeers();
        else
            Logger::info(Logger::MsgType_Config|Logger::MsgType_PeerConn, "No external port provided, not starting external acceptor");

        localMto->startConnectingToPeers(mtoConfigs);
        localMto->startListeningForClients();
        
        signal(SIGINT, signalReceived);
        signal(SIGTERM, signalReceived);
        signal(SIGQUIT, signalReceived);
        
        asyncService->run();
        delete localMto;
        
        return 0;
    }
    catch (const muscle::muscle_exception& ex)
    {
        cerr << "Exited with exception: " << ex.what() << endl;
        return (ex.error_code ? ex.error_code : 1);
    }
    catch (int i)
    {
        return i;
    }
    
    tcp::endpoint myAddress = *it;
  
    Logger::info(Logger::MsgType_Config|Logger::MsgType_PeerConn, "Starting external acceptor on %s:%hu",
                 myAddress.address().to_string().c_str(), myAddress.port()
            );
    tcp::acceptor * peerAcceptor = new tcp::acceptor(ioService, myAddress);
    ExternalAcceptor * exAcc = new ExternalAcceptor(peerAcceptor); /* XXX -> memleak */
    exAcc->startAccepting();
  } else {
    Logger::info(Logger::MsgType_Config|Logger::MsgType_PeerConn, "No external port provided, not starting external acceptor");
  }
  
  startConnectingToPeers();
  
  startListeningForClients();
  
  signal(SIGINT, signalReceived);
  
  while(!e) ioService.run(e);
  
  return 0;
}


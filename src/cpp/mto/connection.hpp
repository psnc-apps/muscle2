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
#ifndef CONNECTION_H
#define CONNECTION_H

#include <iostream>
#include <boost/asio.hpp>
#include <boost/asio/ip/tcp.hpp>
#include <boost/bind.hpp>
#include <set>
#include <map>
#include <queue>

#include "main.hpp"
#include "options.hpp"
#include "messages.hpp"
#include "peerconnectionhandler.hpp"

/** Size of a receive data buffer for each connection */
#define CONNECTION_BUFFER_SIZE 65536

using namespace std;
using namespace boost;
using namespace boost::asio;
using namespace boost::asio::ip;
using namespace boost::system;

class Connection;

/* from main.cpp */
extern io_service ioService;
extern map<Identifier, Connection*> remoteConnections; 
extern set<pair<unsigned int, unsigned short> > availablePorts;


/**
 * Represents connection between two end points
 */ 
class Connection
{
protected:
  /** Local side of the connection */
  tcp::socket * sock;
  
  /** Address of the local side for this connection (needed for logging after connection closes) */
  tcp::endpoint sockEndp;
  
  /** Represents the remote end */
  PeerConnectionHandler * secondMto;
  
  /** Place for initial buffer with request as well as send buffer */
  char * reqBuf;
  
  /** Reusable header with proper adresses and ports */
  Header header;
  
  /** Buffer for transporting data */
  array< char, CONNECTION_BUFFER_SIZE > receiveBuffer;
  
  /** Indcates if there is a peer on the other side */
  bool hasRemotePeer;
  
  /** How many completion hooks still will call this class */
  int referenceCount;
  
  /** If the connections should close / is just closing */
  bool closing;
  
  /* send queue Remote to Local */
  queue< pair<char*, size_t> > sendQueue;
  bool currentlyWriting;

  /** Deletes underlying data buffer once the operation completes */
  struct Bufferfreeer {
  private:
    char * data;
    Connection * thiz;
  public:
    Bufferfreeer(char*d,Connection*c) : data(d), thiz(c) {}
    void operator ()(const error_code& e, size_t);
  };
  
  /** Saves sock remote endpoint to sockEndp */
  void cacheEndpoint();
  
public:
  /** Opening connection from LOCAL */
  Connection(tcp::socket * s, char * requestBuffer);
  
  /** Opening connection from REMOTE */
  Connection(Header h, tcp::socket * s, PeerConnectionHandler * toMto);
  
  /** Reaction on an error */
  void error(const error_code& e);
  
  /** Clean close for the Connection */
  void clean();
  
  /* ====== Local to remote  ===== */
  /** Reads the initial header and tries to estabish connection */
  void readRequest(const error_code& e, size_t);
  
  /** Fires when the response for connection request is received */
  void connectedRemote(Header h);
  
  /* ====== Local to remote or Remote to local  ===== */
  
  /* three methods for transporting data */
  
  void localToRemoteR(const error_code& e, size_t);
  
  void localToRemoteW(const error_code& e, size_t count);
  
  void remoteToLocal(char * data, int length );
  
  /** Once a peer becomes unavailable, this is called. If this connection uses the peer, it closes. */
  void peerDied(PeerConnectionHandler * handler);
  
  /** Requests replacing one PeerConnectionHandler to another */
  void replacePeer(PeerConnectionHandler* from, PeerConnectionHandler* to);
  
  /** Called when remote peer closed */
  void remoteClosed();
};


#endif // CONNECTION_H

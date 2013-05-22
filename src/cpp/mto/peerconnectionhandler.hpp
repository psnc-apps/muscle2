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
#ifndef PEERCONNECTIONHANDLER_H
#define PEERCONNECTIONHANDLER_H

#include <iostream>
#include <queue>
#include <boost/asio/ip/tcp.hpp>
#include <map>
#include <boost/function.hpp>
#include <boost/asio/buffer.hpp>
#include <boost/date_time.hpp>
#include <boost/asio/deadline_timer.hpp>

#include "messages.hpp"
#include "connection.hpp"
#include "main.hpp"

using namespace std;
using namespace boost;
using namespace boost::asio::ip;
using namespace boost::system;


/**
 * The part responsible for interconnection between two MTOs.
 */
class PeerConnectionHandler
{
  
public:
 
  PeerConnectionHandler(tcp::socket * _socket);
  
  /** Fires on accept / connect */
  void connectionEstablished();
  
  /** Starts reading new header from peer */
  void startReadHeader();
  
  /** Parses header */
  void readHeader(const error_code& e, size_t);
  
  /** If set, after connection loss the connection will be reestablished. */
  void setReconnect(bool ifReconnect);
  
  /** Informs peer about MTO reachable via me */
  void propagateHello(const MtoHello & hello);
  
  /** Informs peer about MTOs reachable via me */
  void propagateHellos(vector<MtoHello> & hellos);
  
  /** Hook for uniform error handling */
  void errorOcured(const error_code & ec, string message = "");
  
  /** Returns the remote endpoint for this connection*/
  const tcp::endpoint & remoteEndpoint() const;
  
  void send(char * data, size_t len) {sender.send(data,len);}
  
  void send(char * data, size_t len, function<void (const error_code &, size_t)> callback){sender.send(data,len,callback);}
  
  /** Once a peer becomes unavailable, this is called. If the peer is on fwd list, fwd list is updated */
  void peerDied(PeerConnectionHandler * handler);
  
  /** Requests replacing one peer to other in fwd tables */
  void replacePeer(PeerConnectionHandler* from, PeerConnectionHandler* to);
  
  void enableAutoClose(bool on);
  
protected:
  class Sender {
  public:
    /** Data to be sent and callback at completion */
    Sender(PeerConnectionHandler * parent);
    void send(char * data, size_t len);
    void send(char * data, size_t len, function<void (const error_code &, size_t)> callback);
    
    void dataSent(const error_code &, size_t);
  protected:
    typedef  pair<pair<char*, size_t>, function<void (const error_code &, size_t)> > sendPair;
    bool currentlyWriting;
    char * data;
    function<void (const error_code &, size_t)> currentCallback;
    queue<sendPair> sendQueue;
    PeerConnectionHandler * parent;
  };
  
  int pendingOperatons;
  bool closing;
  Sender sender;
  
  bool autoClose;
  bool ready; ///< indicates that socket is open and can be used w/o problems
  
  /** Keeps information about connections forwarded by this MTO */
  map<Identifier, PeerConnectionHandler*> fwdMap;
  
  /** Ptr to this inter-proxy socket */
  tcp::socket * socket;
  
  /** Remote endpoint for the socket, needed for loging after connection crash and sock recteation by autoClose*/
  tcp::endpoint socketEndpt;
  
  char * dataBufffer;
  
  /** If reconnect after connection loss */
  bool reconnect;

  /* ====== Connect ====== */
  
  /** On connect, check if we have registered ip:port and try to connect */
  void handleConnect(Header h);
  
  /** Answer for connection request */
  void handleConnectFailed(Header h);

  /** Other PeerConnectionHandler asks to forward data through this connection */
  void forward(const Header & h, int dataLen = 0, const char * data = 0);

  /** Propagate a MTO hello to main.h */
  void handleHello(Header h);
  
  /** Once connection requested by peer has been established or failed, this is called */
  struct HandleConnected
  {
    Header h;
    tcp::socket * s;
    PeerConnectionHandler * t;
    HandleConnected(Header header, tcp::socket * socket, PeerConnectionHandler * thiz) : h(header), s(socket), t(thiz){}
    void operator () (const error_code& e);
  };
  
  /* ====== ConnectResponse ====== */
  
  /** Informs proper Connection that it's remote end is / is not available */
  void handleConnectResponse(Header h);
  
  /* ====== Data ====== */
  
  void handleData(Header h);
  
  /** Reads the data and forwards it to proper local connection */
  struct DataForwarder
  {
    DataForwarder(char * d, Header h, PeerConnectionHandler * t) : data(d), header(h), thiz(t) {}
    char * data;
    Header header;
    PeerConnectionHandler * thiz; 
    void operator() (const error_code& e, size_t s);
  };
  
  /* ====== Close ====== */
  
  void handleClose(Header h);
  
  
  /* ====== Reconnect issues ====== */
  
  void recreateSocket(function<void(void)> func);
  
  vector<function<void(void)> > recreateSocketPending;
  
  void recreateSocketFired(const error_code& ec);
  void recreateSocketSentHello(const error_code& ec, char * data);
  void recreateSocketReadHello(const error_code& ec, char * data);
  void recreateSocketTimedOut(const error_code& ec, int retryCount);
  
  boost::asio::deadline_timer recreateSocketTimer;
  
  /* ====== Iddle connection handler ====== */
  
  boost::asio::deadline_timer iddleTimer;
  void iddleTimerFired(const error_code & ec);
  void iddleSocketClose();
  boost::posix_time::ptime lastOperation;
  void updateLastOperationTimer();
};

#endif // PEERCONNECTIONHANDLER_H

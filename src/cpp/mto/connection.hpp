#ifndef CONNECTION_H
#define CONNECTION_H

#include <iostream>
#include <boost/asio.hpp>
#include <boost/asio/ip/tcp.hpp>
#include <boost/bind.hpp>
#include <boost/unordered_set.hpp>
#include <boost/unordered_map.hpp>

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
extern unordered_map<Identifier, Connection*> remoteConnections; 
extern unordered_set<pair<unsigned int, unsigned short> > availablePorts;


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
  
  /** Called when remote peer closed */
  void remoteClosed();
};


#endif // CONNECTION_H

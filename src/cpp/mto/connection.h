#ifndef CONNECTION_H
#define CONNECTION_H

#include <iostream>
#include <boost/asio.hpp>
#include <boost/asio/ip/tcp.hpp>
#include <boost/bind.hpp>
#include <boost/unordered_set.hpp>
#include <boost/unordered_map.hpp>

#include "main.h"
#include "options.h"
#include "messages.h"
#include "peerconnectionhandler.h"

/** Size of a single-direction buffer for each connection */
#define CONNECTION_BUFFER_SIZE 65536

using namespace std;
using namespace boost;
using namespace boost::asio;
using namespace boost::asio::ip;
using namespace boost::system;

class Connection;
struct Identifier;

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
  /** Always non-zero, local side of the connection */
  tcp::socket * firstSocket;
  
  /** On local-local connections represents the second local one */
  tcp::socket * secondSocket;
  
  /** On local-remote / remote-local connections represents the remote one */
  PeerConnectionHandler * secondMto;
  
  char * reqBuf;
  
  /** Reusable header with proper adresses and ports */
  Header header;
  
  /** Buffers for transporting data */
  array< char, CONNECTION_BUFFER_SIZE > f_s, s_f;
  
  /** Indcates if the connection is remote AND if there is a peer on the other side */
  bool hasRemotePeer;
  
  /** How many completion hooks still will call this class */
  int referenceCount;
  
  /** If the connections should / is just closing */
  bool closing;
  
  /** Deletes underlying data buffer once the operation completes */
  struct Bufferfreeer {
    Bufferfreeer(char*d,Connection*c) : data(d), thiz(c) {}
    char * data;
    Connection * thiz;
    void operator ()(const error_code& e, size_t)
    {
      delete data;
      if(thiz){
        thiz->referenceCount--;
        if(thiz->closing) { thiz->clean(); return;}
        if(e)
          thiz->error(e);
      }
    };
  };
  
public:
  /** Opening connection from LOCAL */
  Connection(tcp::socket * s, char * requestBuffer)
    : firstSocket(s), reqBuf(requestBuffer), closing(false), secondSocket(0), hasRemotePeer(false), referenceCount(0), secondMto(0)
  {
  }
  
  /** Opening connection from REMOTE */
  Connection(Header h, tcp::socket * s, PeerConnectionHandler * toMto);
  
  /** Reaction on an error */
  void error(const error_code& e);
  
  /** Clean close for the Connection */
  void clean();
  
  /* ===== Local to local functions ===== */
  
  /** Reads the initila header and tries to estabish connection */
  void readRequest(const error_code& e, size_t);
  
  /** Reacts on finish of a connect */
  void connectedLocal(const error_code& e);
  
  /* four methods for data transport */
  
  void firstToSecondW(const error_code& e, size_t count);
  
  void firstToSecondR(const error_code& e, size_t);
  
  void secondToFirstW(const error_code& e, size_t count);
  
  void secondToFirstR(const error_code& e, size_t);
  
  /* ====== Local to remote  ===== */
  
  /** Checks if a problem occured when sending request to the peer proxy*/
  void connectRemoteRequestErrorMonitor(const error_code& e, size_t count);
  
  /** Fires when the response for connection request is received */
  void connectedRemote(Header h);
  
  /* ====== Local to remote or Remote to local  ===== */
  
  /* three methods for transporting data */
  
  void localToRemoteR(const error_code& e, size_t);
  
  void localToRemoteW(const error_code& e, size_t count);
  
  void remoteToLocal(char * data, int length );
  
  void peerDied(PeerConnectionHandler * handler);
  
  /** Called when remote peer closed */
  void remoteClosed();
};


#endif // CONNECTION_H

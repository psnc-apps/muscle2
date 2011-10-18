#ifndef PEERCONNECTIONHANDLER_H
#define PEERCONNECTIONHANDLER_H

#include <iostream>
#include <boost/asio/ip/tcp.hpp>
#include <boost/unordered_map.hpp>

#include "messages.h"
#include "connection.h"
#include "main.h"

using namespace std;
using namespace boost;
using namespace boost::asio::ip;
using namespace boost::system;


/**
 * The part resposible for interconnection between two proxies.
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
  
  void setReconnect(bool ifReconnect);

  tcp::socket * getSocket() { return socket; }
  
  void propagateHello(const MtoHello & hello);
  void propagateHellos(vector<MtoHello> & hellos);
  
protected:
  
  unordered_map<Identifier, PeerConnectionHandler*> fwdMap;
  
  /** Ptr to this inter-proxy socket */
  tcp::socket * socket;
  
  char * dataBufffer;
  bool reconnect;

  void errorOcured(const error_code & ec, string message);
  
  /* ====== Connect ====== */
  
  /** On connect, check if we have registered ip:port and try to connect */
  void handleConnect(Header h);
  
  void handleConnectFailed(Header h);

  void forward(const Header & h, int dataLen = 0, const char * data = 0);

  void handleHello(Header h);
  
  /** Once connection requested by peer has been established or faled, this is called */
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
  
  /** Reads the data and forewards it to proper local connction */
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
  
  /* ================== */
  
  /** Deletes underlying data buffer once the operation completes */
  struct Bufferfreeer {
    Bufferfreeer(char*d) : data(d) {}
    char * data;
    void operator ()(const error_code& e, size_t)
    {
      delete data;
      if(e) cerr << "Communication failed!" << e.message() << endl;
    };
  };
};

#endif // PEERCONNECTIONHANDLER_H

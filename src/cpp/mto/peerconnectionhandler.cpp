#include "peerconnectionhandler.h"

#include <boost/asio.hpp>
#include <boost/bind.hpp>
#include <boost/foreach.hpp>

#define foreach BOOST_FOREACH

using namespace boost;
using namespace boost::asio;

PeerConnectionHandler::PeerConnectionHandler(tcp::socket * _socket) 
  : socket(_socket) , reconnect(false)
{
}
 
void PeerConnectionHandler::connectionEstablished()
{
  // prints information about connection success
  cerr << "Established a connection with peer" << endl;
  
  // Start normal operation
  startReadHeader();
}
  

void PeerConnectionHandler::startReadHeader()
{
  dataBufffer = new char[Header::getSize()];
  async_read(*socket, buffer(dataBufffer, Header::getSize()), transfer_all(), 
              bind(&PeerConnectionHandler::readHeader, this, placeholders::error, placeholders::bytes_transferred));
}
  

void PeerConnectionHandler::readHeader(const error_code& e, size_t)
{
  if(e)
  {
    errorOcured(e, "reading header failed");
    return;
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
    case Header::PortRangeInfo:
      handleHello(h);
    break;
    default:
      errorOcured(error_code(), "Unknown message: " + Request::typeToString((Request::Type)h.type) + "!");
      return;
  }
}

void PeerConnectionHandler::handleConnect(Header h)
{
  startReadHeader();
  
  if(getPeer(h.dstPort))
  {
    PeerConnectionHandler * fwdTarget = getPeer(h.dstPort);
    Identifier id(h);
    fwdTarget->fwdMap.insert(pair<Identifier, PeerConnectionHandler*>(id, this));
    fwdTarget->forward(h);
    fwdMap.insert(pair<Identifier, PeerConnectionHandler*>(id, fwdTarget));
    return;
  }
  
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
  
void PeerConnectionHandler::handleConnectFailed(Header h)
{
  // indicate fail
  h.length = 1;
      
  char * buf = new char[h.getSize()];
  h.write(buf);
  async_write(*socket, buffer(buf, h.getSize()), transfer_all(), Bufferfreeer(buf));
  
}
  
void PeerConnectionHandler::HandleConnected::operator () (const error_code& e)
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
  async_write(*t->socket, buffer(buf, h.getSize()), transfer_all(), Bufferfreeer(buf));
  Connection * c = new Connection(h, s,  t);
  
}
  
void PeerConnectionHandler::handleConnectResponse(Header h)
{
  startReadHeader();
  
  Identifier id(h);
  if(remoteConnections.find(id) == remoteConnections.end())
  {
    if(fwdMap.find(id)!=fwdMap.end())
    {
      fwdMap[id]->forward(h);
      if(h.length)
        fwdMap.erase(id);  
    }
    else
      cerr << "Got response for unwanted connection!" << endl;
    return;
  }
  
  remoteConnections[id]->connectedRemote(h);
}
  
void PeerConnectionHandler::handleData(Header h)
{
  char * data = new char[h.length];
  async_read(*socket, buffer(data, h.length), transfer_all(), * new DataForwarder(data, h, this));
}
  
void PeerConnectionHandler::DataForwarder::operator() (const error_code& e, size_t s)
{
  Identifier id(header);
  if(remoteConnections.find(id) == remoteConnections.end())
  {
    if(thiz->fwdMap.find(id) != thiz->fwdMap.end())
    {
      thiz->fwdMap[id]->forward(header, header.length, data);
      delete data;
    }
    else
      cerr << "Sending data to unexisting dest?!" << endl;
  }
  else
    remoteConnections[id]->remoteToLocal( data, header.length );
  thiz->startReadHeader();
  // data is deleted in remoteToLocal
}
  
void PeerConnectionHandler::handleClose(Header h)
{
  startReadHeader();
  
  Identifier id(h);
  
  if(remoteConnections.find(id) == remoteConnections.end())
  {
    if(fwdMap.find(id) != fwdMap.end())
    {
      fwdMap[id]->forward(h);
      fwdMap[id]->fwdMap.erase(id);
      fwdMap.erase(id);
    }
    else
      cerr << "Closing not established connection?!" << endl;
    return;
  }
  
  remoteConnections[Identifier(h)]->remoteClosed();
}

/** 'data' is NOT deleted in this function */
void PeerConnectionHandler::forward(const Header & h, int dataLen, const char * data)
{
  int size = Header::getSize() + dataLen;
  char * newdata = new char[size];
  h.write(newdata);
  if(dataLen) memcpy(newdata+Header::getSize(), data, dataLen);
  async_write(*socket, buffer(newdata, size), Bufferfreeer(newdata));
}


void PeerConnectionHandler::setReconnect(bool ifReconnect)
{
  reconnect = ifReconnect;
}

void PeerConnectionHandler::propagateHello(const MtoHello& hello)
{
    Header h;
    h.type = Header::PortRangeInfo;
    h.srcPort=hello.portLow;
    h.dstPort=hello.portHigh;
    h.length = hello.distance+1;
    char * data = new char[Header::getSize()];
    h.write(data);
    async_write(*socket, buffer(data, Header::getSize()), Bufferfreeer(data));
}


void PeerConnectionHandler::propagateHellos(vector< MtoHello >& hellos)
{
  foreach(const MtoHello & hello, hellos)
    propagateHello(hello);
}

void PeerConnectionHandler::handleHello(Header h)
{
  startReadHeader();
  helloReceived(h, this);
}

void PeerConnectionHandler::errorOcured(const boost::system::error_code& ec, string message)
{
  cerr << "Peer connection error: " << message << " error msg: " << ec.message() << endl;
  peerDied(this, reconnect);
  
  for(unordered_map<Identifier, PeerConnectionHandler*>::const_iterator it =  fwdMap.begin(); it!=fwdMap.end(); ++it)
  {
    Header h;
    h.type = Header::Close;
    h.dstAddress = it->first.dstAddress;
    h.dstPort = it->first.dstPort;
    h.srcAddress = it->first.srcAddress;
    h.srcPort = it->first.srcPort;
    h.length = 1;
    it->second->forward(h);
    it->second->fwdMap.erase(it->first);
  }
  
  socket->close();
  delete socket;
  delete [] dataBufffer;
  delete this;
}








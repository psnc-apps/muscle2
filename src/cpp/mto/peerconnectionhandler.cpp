#include "peerconnectionhandler.hpp"

#include <cassert>

#include <boost/asio.hpp>
#include <boost/bind.hpp>
#include <boost/foreach.hpp>

#define foreach BOOST_FOREACH

using namespace boost;
using namespace boost::asio;

PeerConnectionHandler::PeerConnectionHandler(tcp::socket * _socket) 
  : socket(_socket) , reconnect(false), pendingOperatons(0), closing(false)
{
  socketEndpt = socket->remote_endpoint();
}
 
void PeerConnectionHandler::connectionEstablished()
{
  Logger::info(Logger::MsgType_PeerConn, "Established a connection with peer at %s:%hu",
               socketEndpt.address().to_string().c_str(),
               socketEndpt.port()
          );
  
  // Start normal operation
  startReadHeader();
}
  
const boost::asio::ip::tcp::endpoint& PeerConnectionHandler::remoteEndpoint() const
{
  return socketEndpt;
}
 

void PeerConnectionHandler::startReadHeader()
{
  dataBufffer = new char[Header::getSize()];
  
  pendingOperatons++;
  async_read(*socket, buffer(dataBufffer, Header::getSize()), transfer_all(), 
              bind(&PeerConnectionHandler::readHeader, this, placeholders::error, placeholders::bytes_transferred));
}
  

void PeerConnectionHandler::readHeader(const error_code& e, size_t len)
{
  pendingOperatons--;
  if(closing) errorOcured(e);
  if(e)
  {
    errorOcured(e, "Reading header failed");
    return;
  }
  
  assert( len == Header::getSize() );
  
  Header h = Header::deserialize(dataBufffer);
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
    
    Logger::trace(Logger::MsgType_ClientConn|Logger::MsgType_PeerConn, "Forwarding connection %s:%hu - %s:%hu from %s to %s",
                  ip::address_v4(h.srcAddress).to_string().c_str(), h.srcPort,
                  ip::address_v4(h.dstAddress).to_string().c_str(), h.dstPort,
                  socketEndpt.address().to_string().c_str(), fwdTarget->socketEndpt.address().to_string().c_str()
    );
    
    
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
  
  Logger::trace(Logger::MsgType_ClientConn|Logger::MsgType_PeerConn, "Trying to establish connection %s:%hu - %s:%hu for %s",
                  ip::address_v4(h.srcAddress).to_string().c_str(), h.srcPort,
                  ip::address_v4(h.dstAddress).to_string().c_str(), h.dstPort,
                  socketEndpt.address().to_string().c_str()
          );
  
  tcp::endpoint target(address_v4(h.dstAddress), h.dstPort);
  tcp::socket * s = new tcp::socket(ioService);
  pendingOperatons++;
  s->async_connect(target, HandleConnected(h, s, this));
}
  
void PeerConnectionHandler::handleConnectFailed(Header h)
{
  // indicate fail
  h.length = 1;
  
  Logger::debug(Logger::MsgType_ClientConn, "Rejected connection requested from peer (%s:%hu-%s:%hu)",
                ip::address_v4(h.srcAddress).to_string().c_str(), h.srcPort,
                ip::address_v4(h.dstAddress).to_string().c_str(), h.dstPort
  );
      
  char * buf = new char[h.getSize()];
  h.serialize(buf);
  pendingOperatons++;
  
  Logger::trace(Logger::MsgType_PeerConn, "Writing '%s' to %s", 
                Header::typeToString((Request::Type)h.type).c_str(), socketEndpt.address().to_string().c_str());
  
  async_write(*socket, buffer(buf, h.getSize()), transfer_all(), Bufferfreeer(buf, this));
  
}
  
void PeerConnectionHandler::HandleConnected::operator () (const error_code& e)
{
  t->pendingOperatons--;
  if(t->closing)
  {
    t->errorOcured(e);
    if(s->is_open())
      s->close();
    delete s;
    return;
  }
  if(e)
  {
    t->handleConnectFailed(h);
    delete s;
    return;
  }
  
  // indicate success
  h.length = 0;
  
  char * buf = new char[h.getSize()];
  h.serialize(buf);
  t->pendingOperatons++;
  
  Logger::trace(Logger::MsgType_PeerConn, "Writing '%s' to %s", 
                Header::typeToString((Request::Type)h.type).c_str(), t->socketEndpt.address().to_string().c_str());
  
  async_write(*t->socket, buffer(buf, h.getSize()), transfer_all(), Bufferfreeer(buf, t));
  Connection * c = new Connection(h, s,  t);
  
}
  
void PeerConnectionHandler::handleConnectResponse(Header h)
{
  startReadHeader();
  
  Logger::trace(Logger::MsgType_ClientConn|Logger::MsgType_PeerConn, "Got info about establish connection %s:%hu - %s:%hu by %s",
                  ip::address_v4(h.srcAddress).to_string().c_str(), h.srcPort,
                  ip::address_v4(h.dstAddress).to_string().c_str(), h.dstPort,
                  socketEndpt.address().to_string().c_str()
          );
  
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
      Logger::info(Logger::MsgType_PeerConn, "Got response for invalid connection");
    return;
  }
  
  remoteConnections[id]->connectedRemote(h);
}
  
void PeerConnectionHandler::handleData(Header h)
{
  Logger::trace(Logger::MsgType_ClientConn|Logger::MsgType_PeerConn, "Starting data transfer of length %u on %s:%hu - %s:%hu from %s",
                  h.length,
                  ip::address_v4(h.srcAddress).to_string().c_str(), h.srcPort,
                  ip::address_v4(h.dstAddress).to_string().c_str(), h.dstPort,
                  socketEndpt.address().to_string().c_str()
          );
  
  char * data = new char[h.length];
  pendingOperatons++;
  async_read(*socket, buffer(data, h.length), transfer_all(), DataForwarder(data, h, this));
}
  
void PeerConnectionHandler::DataForwarder::operator() (const error_code& e, size_t s)
{
  thiz->pendingOperatons--;
  if(thiz->closing || e)
  {
    thiz->errorOcured(e);
    delete [] data;
    return;
  }
  
  assert(header.length == s);
  
  Logger::trace(Logger::MsgType_ClientConn|Logger::MsgType_PeerConn, "Got data of length %u on %s:%hu - %s:%hu from %s",
                  header.length,
                  ip::address_v4(header.srcAddress).to_string().c_str(), header.srcPort,
                  ip::address_v4(header.dstAddress).to_string().c_str(), header.dstPort,
                  thiz->socketEndpt.address().to_string().c_str()
          );
  
  
  Identifier id(header);
  if(remoteConnections.find(id) == remoteConnections.end())
  {
    if(thiz->fwdMap.find(id) != thiz->fwdMap.end())
      thiz->fwdMap[id]->forward(header, header.length, data);
    else
      Logger::error(Logger::MsgType_PeerConn, "Received data for nonexistent destination");
    delete [] data;
  }
  else
    remoteConnections[id]->remoteToLocal( data, header.length );
  thiz->startReadHeader();
  // data is deleted in remoteToLocal
}
  
void PeerConnectionHandler::handleClose(Header h)
{
  startReadHeader();

  Logger::trace(Logger::MsgType_ClientConn|Logger::MsgType_PeerConn, "Got connection close reuqest for %s:%hu - %s:%hu from %s",
                  ip::address_v4(h.srcAddress).to_string().c_str(), h.srcPort,
                  ip::address_v4(h.dstAddress).to_string().c_str(), h.dstPort,
                  socketEndpt.address().to_string().c_str()
          );
  

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
      Logger::error(Logger::MsgType_PeerConn, "Closing not established connection");
    return;
  }
  
  remoteConnections[Identifier(h)]->remoteClosed();
}

/** 'data' is NOT deleted in this function */
void PeerConnectionHandler::forward(const Header & h, int dataLen, const char * data)
{
  int size = Header::getSize() + dataLen;
  char * newdata = new char[size];
  h.serialize(newdata);
  if(dataLen) memcpy(newdata+Header::getSize(), data, dataLen);
  pendingOperatons++;
  
  Logger::trace(Logger::MsgType_PeerConn, "Forwarding '%s' to %s", 
                Header::typeToString((Request::Type)h.type).c_str(), socketEndpt.address().to_string().c_str());
  
  async_write(*socket, buffer(newdata, size), Bufferfreeer(newdata,this));
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
    h.serialize(data);
    pendingOperatons++;
    
    Logger::trace(Logger::MsgType_PeerConn, "Writing '%s' to %s", 
                Header::typeToString((Request::Type)h.type).c_str(), socketEndpt.address().to_string().c_str());
    
    async_write(*socket, buffer(data, Header::getSize()), Bufferfreeer(data, this));
}


void PeerConnectionHandler::propagateHellos(vector< MtoHello >& hellos)
{
  foreach(const MtoHello & hello, hellos)
    propagateHello(hello);
}

void PeerConnectionHandler::handleHello(Header h)
{
  startReadHeader();
  
  Logger::trace(Logger::MsgType_ClientConn|Logger::MsgType_PeerConn, "Got hello from %s for %d-%d",
                  socketEndpt.address().to_string().c_str(),h.srcPort,h.dstPort
          );
  
  
  helloReceived(h, this);
}

void PeerConnectionHandler::errorOcured(const boost::system::error_code& ec, string message)
{
  if(closing)
  {
    if(pendingOperatons==0)
      delete this;
    return;
  }
  
  closing = true;
  
  Logger::error(Logger::MsgType_PeerConn, "Peer connection error: '%s' error msg: '%s'. Closing peer connection to %s:%hu",
                message.c_str(), ec.message().c_str(), 
               socketEndpt.address().to_string().c_str(),
               socketEndpt.port()
          );
  
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
  if(pendingOperatons==0)
    delete this;
}

void PeerConnectionHandler::Bufferfreeer::operator()(const boost::system::error_code& e, size_t )
{
  delete [] data;
  thiz->pendingOperatons--;
  if(thiz->closing)
  {
    thiz->errorOcured(e);
    return;
  }

  if(e)
    thiz->errorOcured(e, "Communication failed!");
};







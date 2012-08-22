#include "peerconnectionhandler.hpp"

#include <cassert>

#include <boost/asio.hpp>
#include <boost/bind.hpp>
#include <boost/foreach.hpp>

#define foreach BOOST_FOREACH

using namespace boost;
using namespace boost::asio;

PeerConnectionHandler::PeerConnectionHandler(tcp::socket * _socket) 
  : socket(_socket) , reconnect(false), pendingOperatons(0), closing(false), sender(Sender(this)), autoClose(false),
  recreateSocketTimer(ioService), iddleTimer(ioService), ready(true)
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
  
  // if iddle socket has been closed socket closed
  if(!ready && e == error::operation_aborted)
    return;
  
  if(e)
  {
    errorOcured(e, "Reading header failed");
    return;
  }
  
  updateLastOperationTimer();
  
  assert( len == Header::getSize() );
  
  Header h = Header::deserialize(dataBufffer);
  delete [] dataBufffer;
  dataBufffer = 0;
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
    case Header::PeerClose:
      errorOcured(error_code(), "PeerClose");
    break;
    default:
      errorOcured(error_code(), "Unknown message: " + Request::typeToString((Request::Type)h.type) + "!");
      return;
  }
}

void PeerConnectionHandler::handleConnect(Header h)
{
  startReadHeader();
  
  if(autoClose)
    Logger::debug(Logger::MsgType_PeerConn, "Peer %s is using the wrong connection handler!", socketEndpt.address().to_string().c_str());
  
  PeerConnectionHandler * fwdTarget = getPeer(h);
  
  if(fwdTarget)
  { 
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
  
  Logger::trace(Logger::MsgType_PeerConn, "Writing '%s' to %s", 
                Header::typeToString((Request::Type)h.type).c_str(), socketEndpt.address().to_string().c_str());
  
  send(buf, h.getSize());
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
  
  Logger::trace(Logger::MsgType_PeerConn, "Writing '%s' to %s", 
                Header::typeToString((Request::Type)h.type).c_str(), t->socketEndpt.address().to_string().c_str());
  
  
  t->send(buf, h.getSize());
  Connection * c = new Connection(h, s,  getPeer(t));
  
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

  if(autoClose)
    Logger::debug(Logger::MsgType_PeerConn, "Peer %s is using the wrong connection handler!", socketEndpt.address().to_string().c_str());
  
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
  
  if(autoClose)
    Logger::debug(Logger::MsgType_PeerConn, "Peer %s is using the wrong connection handler!", socketEndpt.address().to_string().c_str());
  

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

  updateLastOperationTimer();
  
  Logger::trace(Logger::MsgType_PeerConn, "Forwarding '%s' to %s", 
                Header::typeToString((Request::Type)h.type).c_str(), socketEndpt.address().to_string().c_str());
  
  
  send(newdata, size);
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
    
    Logger::trace(Logger::MsgType_PeerConn, "Writing '%s' to %s", 
                Header::typeToString((Request::Type)h.type).c_str(), socketEndpt.address().to_string().c_str());
    
    send(data, Header::getSize());
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
  
  if(message!="PeerClose")
  {
    Logger::error(Logger::MsgType_PeerConn, "Peer connection error: '%s' error msg: '%s'. Closing peer connection to %s:%hu",
                  message.c_str(), ec.message().c_str(), 
                  socketEndpt.address().to_string().c_str(),
                  socketEndpt.port()
                 );
  }
  else
  {
    Logger::info(Logger::MsgType_PeerConn, "Peer %s closed it's iddle connection",
                  socketEndpt.address().to_string().c_str()
                 );
  }
  
  ::peerDied(this, reconnect);
  
  for(map<Identifier, PeerConnectionHandler*>::const_iterator it =  fwdMap.begin(); it!=fwdMap.end(); ++it)
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
  
  iddleTimer.cancel();
  recreateSocketTimer.cancel();
  
  if(socket)
  {
    socket->close();
    delete socket;
  }
  delete [] dataBufffer;
  if(pendingOperatons==0)
    delete this;
}

PeerConnectionHandler::Sender::Sender(PeerConnectionHandler* _parent)
  : parent(_parent), currentlyWriting(false)
{
}

void PeerConnectionHandler::Sender::send(char * data, size_t len)
{
  send(data, len, function<void (const error_code &, size_t)>());
}

void PeerConnectionHandler::Sender::send(char * _data, size_t len, function< void (const error_code &, size_t) > callback)
{
  if(!parent->ready){
    parent->recreateSocket(bind(&PeerConnectionHandler::Sender::send, this, _data, len, callback));
    return;
  }
  
  parent->updateLastOperationTimer();
  
  if (currentlyWriting) {
    Logger::trace(Logger::MsgType_PeerConn, "Queuing %u bytes on %s. Already %d messages in queue", len,
	parent->remoteEndpoint().address().to_string().c_str(), 
	sendQueue.size());

    sendQueue.push(sendPair(pair<char*, size_t>(_data,len), callback));
  } else {
	Logger::trace(Logger::MsgType_PeerConn, "Sending directly %u bytes on %s", len, parent->remoteEndpoint().address().to_string().c_str());
  
	currentlyWriting = true;
	data = _data;
	currentCallback = callback;
	parent->pendingOperatons++;
	async_write(*(parent->socket), buffer(data,len), bind(&PeerConnectionHandler::Sender::dataSent, this, _1, _2));
  }
}


void PeerConnectionHandler::Sender::dataSent(const error_code& ec, size_t len)
{
  if(currentCallback)
    currentCallback(ec, len);
  delete [] data;
  
  if(ec || parent->closing)
  {
    Logger::trace(Logger::MsgType_PeerConn, "Error occured, cleaning send queue on %s", parent->remoteEndpoint().address().to_string().c_str());
    parent->pendingOperatons--;
    while(!sendQueue.empty())
    {
      delete [] sendQueue.front().first.first;
      sendQueue.pop();
    }
    parent->errorOcured(ec, "Send failed");
    return;
  }
  
  if(sendQueue.empty())
  {
    Logger::trace(Logger::MsgType_PeerConn, "Send queue empty on %s", parent->remoteEndpoint().address().to_string().c_str());
    currentlyWriting=false;
    parent->pendingOperatons--;
    return;
  }

  parent->updateLastOperationTimer();
  
  data = sendQueue.front().first.first;
  size_t newlen = sendQueue.front().first.second;
  currentCallback = sendQueue.front().second;
  sendQueue.pop();
  Logger::trace(Logger::MsgType_PeerConn, "Sending from queue %u bytes on %s", newlen, parent->remoteEndpoint().address().to_string().c_str());
  async_write(*(parent->socket), buffer(data,newlen), bind(&PeerConnectionHandler::Sender::dataSent, this, _1, _2));
}

void PeerConnectionHandler::replacePeer(PeerConnectionHandler* from, PeerConnectionHandler* to)
{
  for (map<Identifier, PeerConnectionHandler*>::iterator it = fwdMap.begin(); it != fwdMap.end(); ++it) {
    if(it->second == from)
      it->second = to;
  }
}

void PeerConnectionHandler::peerDied(PeerConnectionHandler* handler)
{
  map<Identifier, PeerConnectionHandler*>::iterator it = fwdMap.begin();
  while ( it != fwdMap.end() ) {
    if(it->second == handler){
      Header h;
      h.dstAddress=it->first.dstAddress;
      h.srcAddress=it->first.srcAddress;
      h.dstPort=it->first.dstPort;
      h.srcPort=it->first.srcPort;
      PeerConnectionHandler * newHandler = getPeer(h);
      if(newHandler) {
        it->second = newHandler;
      } else {
        fwdMap.erase(it++);
        continue;
      }
    }
    it++;
  }
}


void PeerConnectionHandler::enableAutoClose(bool on)
{
  assert( reconnect );
  
  Logger::trace(Logger::MsgType_PeerConn, "PeerConnHandler to %s set to auto close mode", socketEndpt.address().to_string().c_str());
  
  autoClose = on;
  updateLastOperationTimer();
  
  iddleTimer.expires_from_now(lastOperation + Options::getInstance().getSockAutoCloseTimeout() - boost::posix_time::second_clock::universal_time());
  iddleTimer.async_wait(bind(&PeerConnectionHandler::iddleTimerFired, this, _1));
}

void PeerConnectionHandler::updateLastOperationTimer()
{
  lastOperation = boost::posix_time::second_clock::universal_time();
}

void PeerConnectionHandler::iddleTimerFired(const error_code& ec)
{
  // closing - ignore.
  if(ec == error::operation_aborted)
    return;
  
  if(ec){
    errorOcured(ec);
    return;
  }
  
  ptime timeout = lastOperation + Options::getInstance().getSockAutoCloseTimeout();
  
  Logger::trace(Logger::MsgType_PeerConn, "Checking if connection to %s is iddle, last access at %s",
                socketEndpt.address().to_string().c_str(),
                to_simple_string(lastOperation.time_of_day()).c_str()
               );
  
  if(boost::posix_time::second_clock::universal_time() >= timeout)
  {
    Logger::info(Logger::MsgType_PeerConn, "Connection to %s is iddle, closing socket",
                 socketEndpt.address().to_string().c_str()
                );
    
    Header h;
    h.type=Header::PeerClose;
    char * buf = new char[Header::getSize()];
    h.serialize(buf);
    // To do: There is a slight chance that between the line below and iddleSocketClose something bad will happen
    send(buf, Header::getSize(), bind(&PeerConnectionHandler::iddleSocketClose, this));
  }
  else
  {
    iddleTimer.expires_from_now(timeout - boost::posix_time::second_clock::universal_time());
    iddleTimer.async_wait(bind(&PeerConnectionHandler::iddleTimerFired, this, _1));
  }
}

void PeerConnectionHandler::iddleSocketClose()
{
    ready = false;
    socket->cancel();
    socket->close();
    delete socket;
    socket = 0;

    Logger::trace(Logger::MsgType_PeerConn, "Connection to %s closed",
                  socketEndpt.address().to_string().c_str()
                 );
}


void PeerConnectionHandler::recreateSocket(function< void() > func)
{
  recreateSocketPending.push_back(func);
 
  // already recreating socket
  if(socket)
    return;
  
  Logger::info(Logger::MsgType_PeerConn, "Recreating socket to %s:%hu",
               socketEndpt.address().to_string().c_str(), socketEndpt.port()
              );
  
  socket = new tcp::socket(ioService);
  recreateSocketTimer.expires_from_now(seconds(10));
  
  recreateSocketTimer.async_wait(bind(&PeerConnectionHandler::recreateSocketTimedOut, this, _1, 0));
  socket->async_connect(socketEndpt, bind(&PeerConnectionHandler::recreateSocketFired, this, _1));
}

void PeerConnectionHandler::recreateSocketFired(const error_code& ec)
{
  // timeout or close
  if(ec == error::operation_aborted)
    return;
  
  recreateSocketTimer.cancel();
  
  if(ec == error::connection_refused)
  {
    // be unhappy and kill peerconnectionhandler
    Logger::error(Logger::MsgType_PeerConn, "Failed to recreating socket to %s:%hu - connection refused!",
                  socketEndpt.address().to_string().c_str(), socketEndpt.port()
                 );
    errorOcured(ec);
    return;
  }
  
  Logger::info(Logger::MsgType_PeerConn, "Connection successfuly recreated to %s:%hu",
                  socketEndpt.address().to_string().c_str(), socketEndpt.port()
                 );
  
  // hello xchange....
  
  char * data = new char[MtoHello::getSize()];
  MtoHello myHello;
  myHello.distance=0;
  myHello.isLastMtoHello=true;
  myHello.portLow=Options::getInstance().getLocalPortLow();
  myHello.portHigh=Options::getInstance().getLocalPortHigh();
  myHello.serialize(data);
  async_write(*socket, buffer(data, MtoHello::getSize()), bind(&PeerConnectionHandler::recreateSocketSentHello, this, _1, data));
}

void PeerConnectionHandler::recreateSocketSentHello(const error_code& ec, char* data)
{ 
  async_read(*socket, buffer(data, MtoHello::getSize()), bind(&PeerConnectionHandler::recreateSocketReadHello, this, _1, data));
}

void PeerConnectionHandler::recreateSocketReadHello(const error_code& ec, char* data)
{
  if(ec) {
    delete [] data;
    errorOcured(ec);
    return;
  }
  
  MtoHello hello = MtoHello::deserialize(data);
  
  if(!hello.isLastMtoHello) {
    async_read(*socket, buffer(data, MtoHello::getSize()), bind(&PeerConnectionHandler::recreateSocketReadHello, this, _1, data));
    return;
  }
  
  delete [] data;
  
  ready = true;
  startReadHeader();
  
  vector<function<void()> > ops(recreateSocketPending);
  recreateSocketPending.clear();
 
  for (int i = 0; i < ops.size(); ++i) {
    ops[i]();
  }
  
  updateLastOperationTimer();
  
  iddleTimer.expires_at(lastOperation + Options::getInstance().getSockAutoCloseTimeout());
  iddleTimer.async_wait(bind(&PeerConnectionHandler::iddleTimerFired, this, _1));
}


void PeerConnectionHandler::recreateSocketTimedOut(const error_code& ec, int retryCount)
{ 
  // connect fired or timeout
  if(ec == error::operation_aborted)
    return;
  
  if(retryCount>2){
    // be unhappy and kill peerconnectionhandler
    Logger::error(Logger::MsgType_PeerConn, "Failed to recreating socket to %s:%hu - timed out!",
                  socketEndpt.address().to_string().c_str(), socketEndpt.port()
                 );
    errorOcured(ec);
    return;
  }
  
  Logger::trace(Logger::MsgType_PeerConn, "Recreating socket to %s:%hu timed out, retrying...",
                  socketEndpt.address().to_string().c_str(), socketEndpt.port()
                 );
  
  socket->cancel();
  recreateSocketTimer.expires_from_now(seconds(20));
  recreateSocketTimer.async_wait(bind(&PeerConnectionHandler::recreateSocketTimedOut, this, _1, retryCount+1));
  socket->async_connect(socketEndpt, bind(&PeerConnectionHandler::recreateSocketFired, this, _1));
}

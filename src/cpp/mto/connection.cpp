#include "connection.h"

Connection::Connection(Header h, tcp::socket* s, PeerConnectionHandler * t)
  : firstSocket(s), header(h), closing(false), secondSocket(0), hasRemotePeer(true), referenceCount(0), reqBuf(0) ,
    secondMto(t)
{
  remoteConnections[Identifier(h)]=this;
  
  header.type=Header::Data;

  ++referenceCount;
  localToRemoteR(error_code(),0);
}

void Connection::readRequest(const boost::system::error_code& e, size_t )
{
  if(e)
  {
    Logger::error(Logger::MsgType_ClientConn, "Error reading request from %s:%d (%s)",
                  firstSocket->remote_endpoint().address().to_string().c_str(),
                  firstSocket->remote_endpoint().port(),
                  e.message().c_str()
                 );
    delete [] reqBuf;
    clean();
    return;
  }
  
  Request request = Request::read(reqBuf);
  header = Header(request);
  delete [] reqBuf;
  
  
  
  switch(request.type){
    case Request::Register:
    {
      if(request.srcPort<localPortLow || request.srcPort>localPortHigh)
      {
        Logger::error(Logger::MsgType_ClientConn, "Port %s:%hu out of range - registering port aborted (connection from %s:%hu)",
                      ip::address_v4(request.srcAddress).to_string().c_str(),
                      request.srcPort,
                      firstSocket->remote_endpoint().address().to_string().c_str(),
                      firstSocket->remote_endpoint().port()
                     );
        clean();
        return;
      }
      availablePorts.insert(pair<unsigned long, unsigned short>(request.srcAddress,request.srcPort));
      
      Logger::info(Logger::MsgType_ClientConn, "Listening port registered: %s:%hu",
                   ip::address_v4(request.srcAddress).to_string().c_str(),
                   request.srcPort
      );
      
      clean();
      break;
    }
    case Request::Connect:
    {
      if(request.dstPort>=localPortLow && request.dstPort<=localPortHigh)
      { // local to local
        if(availablePorts.find(pair<unsigned long, unsigned short>(request.dstAddress,request.dstPort))==availablePorts.end())
        {
          Logger::error(Logger::MsgType_ClientConn, "Requested connection to not registered %s:%hu from %s:%hu",
                   ip::address_v4(request.dstAddress).to_string().c_str(),
                   request.dstPort,
                   firstSocket->remote_endpoint().address().to_string().c_str(),
                   firstSocket->remote_endpoint().port()
          );
          clean();
          return;
        }
        else
        {
          Logger::trace(Logger::MsgType_ClientConn, "Trying to establish connection from %s:%hu to %s:%hu",
                   ip::address_v4(request.dstAddress).to_string().c_str(),
                   request.dstPort,
                   firstSocket->remote_endpoint().address().to_string().c_str(),
                   firstSocket->remote_endpoint().port()
          );
          secondSocket = new tcp::socket(ioService);
          secondSocket->async_connect(tcp::endpoint(ip::address_v4(request.dstAddress), request.dstPort),
                                      bind(&Connection::connectedLocal, this, placeholders::error));
        }
      }
      else
      { // local to remote
        secondMto = getPeer(request.dstPort);
        if(!secondMto)
        {
          Logger::error(Logger::MsgType_ClientConn, "Requested connection to port out of range %s:%hu from %s:%hu",
                   ip::address_v4(request.dstAddress).to_string().c_str(),
                   request.dstPort,
                   firstSocket->remote_endpoint().address().to_string().c_str(),
                   firstSocket->remote_endpoint().port()
          );
          clean();
          break;
        }
        
        Header h(request);
        h.write(s_f.c_array());
        
        remoteConnections[Identifier(h)]=this;
        
        ++referenceCount;
        async_write(*(secondMto->getSocket()), buffer(s_f, h.getSize()), transfer_all(), 
                    bind(&Connection::connectRemoteRequestErrorMonitor, this, placeholders::error, placeholders::bytes_transferred));
        
        Logger::trace(Logger::MsgType_ClientConn, "Requesting connection to host %s:%hu from peer %s:%hu",
                      ip::address_v4(request.dstAddress).to_string().c_str(),
                      request.dstPort,
                      secondMto->getSocket()->remote_endpoint().address().to_string().c_str(),
                      secondMto->getSocket()->remote_endpoint().port()
        );
      }
      break;
    }
    default:
    {
      Logger::error(Logger::MsgType_ClientConn, "Client from %s:%hu sent unknown message (type %hhd)",
                    firstSocket->remote_endpoint().address().to_string().c_str(),
                    firstSocket->remote_endpoint().port(),
                    request.type
      );
      clean();
    }
  }
}

void Connection::clean()
{
  if(!closing)
  {
    Logger::trace(Logger::MsgType_ClientConn, "Closing connection between %s:%hu to %s:%hu",
                  ip::address_v4(header.dstAddress).to_string().c_str(),
                  header.dstPort,
                  ip::address_v4(header.srcAddress).to_string().c_str(),
                  header.srcPort
    );
  }
  closing = true;
  if(hasRemotePeer)
  {
   hasRemotePeer=false;
   header.type=Header::Close;
   char * data = new char[Header::getSize()];
   header.write(data);
   if(secondMto)
     async_write(*(secondMto->getSocket()), buffer(data, Header::getSize()), transfer_all(), Bufferfreeer(data, 0));
  }
  
  remoteConnections.erase(Identifier(header));
  if(firstSocket->is_open())
    firstSocket->close();
  if(!referenceCount)
    delete firstSocket;
  if(secondSocket)
  {
    if(secondSocket->is_open())
      secondSocket->close();
    if(!referenceCount)
      delete secondSocket;
  }
  if(!referenceCount)
  {
    Logger::debug(Logger::MsgType_ClientConn, "Closed connection between %s:%hu to %s:%hu",
                  ip::address_v4(header.dstAddress).to_string().c_str(),
                  header.dstPort,
                  ip::address_v4(header.srcAddress).to_string().c_str(),
                  header.srcPort
    );
    delete this;
  }
    
}

void Connection::remoteClosed()
{
  hasRemotePeer = false;
  clean();
}


void Connection::error(const boost::system::error_code& e)
{
  if(closing)
  { 
    clean(); 
    return;
  }

  if(e!=asio::error::eof)
    Logger::error(Logger::MsgType_ClientConn, "Error ocurred between %s:%hu to %s:%hu (%s)",
                  ip::address_v4(header.dstAddress).to_string().c_str(),
                  header.dstPort,
                  ip::address_v4(header.srcAddress).to_string().c_str(),
                  header.srcPort,
                  e.message().c_str()
    );
  
  
  firstSocket->shutdown(boost::asio::socket_base::shutdown_both);
  
  if(secondSocket && secondSocket->is_open())
    secondSocket->shutdown(boost::asio::socket_base::shutdown_both);
  
  clean();
}

/*    _____      
     |_____|     
  \__|_____|__/  
    /       \    
  (|  [] []  |)  
   |    ^    |   
    \ \___/ /    
     \_____/     */


void Connection::firstToSecondW(const error_code& e, size_t count)
{
  if(!e)
    async_write(*secondSocket, buffer(f_s, count), transfer_all(), bind(&Connection::firstToSecondR, this, placeholders::error, placeholders::bytes_transferred));
  else
  {
    --referenceCount;
    error(e);
  }
}

void Connection::firstToSecondR(const error_code& e, size_t)
{
  if(!e)
    firstSocket->async_read_some(buffer(f_s), bind(&Connection::firstToSecondW, this, placeholders::error, placeholders::bytes_transferred));
  else
  {
    referenceCount--;
    error(e);
  }
}

void Connection::secondToFirstW(const error_code& e, size_t count)
{
  if(!e)
    async_write(*firstSocket, buffer(s_f, count), transfer_all(), bind(&Connection::secondToFirstR, this, placeholders::error, placeholders::bytes_transferred));
  else
  {
    referenceCount--;
    error(e);
  }
}

void Connection::secondToFirstR(const error_code& e, size_t)
{
  if(!e)
    secondSocket->async_read_some(buffer(s_f), bind(&Connection::secondToFirstW, this, placeholders::error, placeholders::bytes_transferred));
  else
  {
    referenceCount--;
    error(e);
  }
}

void Connection::connectedLocal(const error_code& e)
{
  Header h(header);
  h.type=Header::ConnectResponse;
  if(e)
  {
    Logger::error(Logger::MsgType_ClientConn, "Error connecting to local side (%s:%hu -- %s:%hu) - %s", 
                  ip::address_v4(header.dstAddress).to_string().c_str(),
                  header.dstPort,
                  ip::address_v4(header.srcAddress).to_string().c_str(),
                  header.srcPort,
                  e.message().c_str()
                 );
    h.length = 1;
    reqBuf = new char[Header::getSize()];
    h.write(reqBuf);
    async_write(*firstSocket, buffer(reqBuf,Header::getSize()), transfer_all(), Bufferfreeer(reqBuf,0));
    clean();
    return;
  }
  
  h.length=0;
  reqBuf = new char[Header::getSize()];
  h.write(reqBuf);
  
  referenceCount++;
  async_write(*firstSocket, buffer(reqBuf,Header::getSize()), transfer_all(), Bufferfreeer(reqBuf,this));
  
  referenceCount+=2;
  firstToSecondR(error_code(), 0);
  secondToFirstR(error_code(), 0);
}

void Connection::connectRemoteRequestErrorMonitor(const error_code& e, size_t count)
{
  referenceCount--;
  if(closing) { clean(); return;}
  
  if(e)
  {
    secondMto->errorOcured(e, "Write failed");
    clean();
  }
}

void Connection::localToRemoteR(const error_code& e, size_t)
{
  referenceCount--;
  if(closing) { clean(); return;};
  if(e)
  {
    secondMto->errorOcured(e, "Write failed");
    clean();
  }
  delete [] reqBuf;
  
  referenceCount++;
  firstSocket->async_read_some(buffer(f_s), bind(&Connection::localToRemoteW, this, placeholders::error, placeholders::bytes_transferred));
}

void Connection::localToRemoteW(const error_code& e, size_t count)
{
  if(e)
  {
    referenceCount--;
    error(e);
    return;
  }
  reqBuf = new char[count + Header::getSize()];
  header.length=count;
  header.write(reqBuf);
  memcpy(reqBuf+Header::getSize(), f_s.c_array(), count);
  async_write(*(secondMto->getSocket()), buffer(reqBuf, count + Header::getSize()), transfer_all(),
              bind(&Connection::localToRemoteR, this, placeholders::error, placeholders::bytes_transferred));
  
}

void Connection::remoteToLocal(char * data, int length )
{
  referenceCount++;
  async_write(*firstSocket, buffer(data, length), Bufferfreeer(data, this));
}

void Connection::connectedRemote(Header h)
{
  reqBuf = new char[Header::getSize()];
  h.write(reqBuf);
  referenceCount++;
  async_write(*firstSocket, buffer(reqBuf,Header::getSize()), transfer_all(), Bufferfreeer(reqBuf,this));
  reqBuf = 0;
  
  if(h.length)
  { // Fail
    Logger::debug(Logger::MsgType_ClientConn, "Got negative response for connect request (%s:%hu -- %s:%hu)", 
                  ip::address_v4(header.dstAddress).to_string().c_str(),
                  header.dstPort,
                  ip::address_v4(header.srcAddress).to_string().c_str(),
                  header.srcPort
                 );
    clean();
  }
  else
  { // Success
    Logger::debug(Logger::MsgType_ClientConn, "Remote connection succeeded (%s:%hu -- %s:%hu)", 
                  ip::address_v4(header.dstAddress).to_string().c_str(),
                  header.dstPort,
                  ip::address_v4(header.srcAddress).to_string().c_str(),
                  header.srcPort
                 );
    header.type=Header::Data;
    hasRemotePeer = true;
    referenceCount++;
    localToRemoteR(error_code(), 0);
  }
}

void Connection::peerDied(PeerConnectionHandler* handler)
{
  if(secondMto != handler)
    return;
  secondMto = 0;
  clean();
}



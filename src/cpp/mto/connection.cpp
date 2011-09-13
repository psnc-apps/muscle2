#include "connection.h"

std::size_t hash_value(const Identifier& b)
{
    boost::hash<int> h;
    return h(b.dstAddress)+h(b.srcAddress)+h(b.dstPort)+h(b.srcPort);
}

Connection::Connection(Header h, tcp::socket* s)
  : firstSocket(s), header(h), closing(false), secondSocket(0), hasRemotePeer(true), referenceCount(0), reqBuf(0)
{
  remoteConnections[Identifier(h)]=this;
 
  hasRemotePeer = true;
  
  header.type=Header::Data;

  ++referenceCount;
  localToRemoteR(error_code(),0);
}


void Connection::readRequest(const boost::system::error_code& e, size_t )
{
  if(e)
  {
    cerr << "Error reading request: " << e.message() << endl;
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
        cerr << "Local port out of range: " << request.srcPort << endl;
        clean();
        return;
      }
      availablePorts.insert(pair<unsigned long, unsigned short>(request.srcAddress,request.srcPort));
      cerr << "New listening port registered: " 
            << ip::address_v4(request.srcAddress).to_string()
            << ":"
            << request.srcPort
            << endl;
      clean();
      break;
    }
    case Request::Connect:
    {
      if(request.dstPort>=localPortLow && request.dstPort<=localPortHigh)
      { // local to local
        if(availablePorts.find(pair<unsigned long, unsigned short>(request.dstAddress,request.dstPort))==availablePorts.end())
        {
          cerr << "Requested connection to not registered  "
                << ip::address_v4(request.dstAddress).to_string()
                << ":"
                << request.dstPort
                << endl;
                clean();
                return;
        }
        else
        {
          secondSocket = new tcp::socket(ioService);
          secondSocket->async_connect(tcp::endpoint(ip::address_v4(request.dstAddress), request.dstPort),
                                      bind(&Connection::connectedLocal, this, placeholders::error));
        }
      }
      else if (request.dstPort>=remotePortLow && request.dstPort<=remotePortHigh)
      { // local to remote
        
        Header h(request);
        h.write(s_f.c_array());
        
        remoteConnections[Identifier(h)]=this;
        
        ++referenceCount;
        async_write(*mainSocket, buffer(s_f, h.getSize()), transfer_all(), 
                    bind(&Connection::connectRemoteRequestErrorMonitor, this, placeholders::error, placeholders::bytes_transferred));
      }
      else
      {
        cerr << "Remote port out of range: " << request.dstPort << endl;
        clean();
      }
      break;
    }
    default:
    {
      cerr << "Unknown request: " << request.type << endl;
      clean();
    }
  }
}

void Connection::clean()
{
  closing = true;
  if(hasRemotePeer)
  {
   hasRemotePeer=false;
   header.type=Header::Close;
   char * data = new char[Header::getSize()];
   header.write(data);
   async_write(*mainSocket, buffer(data, Header::getSize()), transfer_all(), Bufferfreeer(data, 0));
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
    delete this;
    cerr << "Cleaned" << endl;
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

  // TODO: EOF per socket
  if(e!=asio::error::eof)
    cerr << "Error ocured: " << e.message() << endl;
  
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
    cerr << "Error establishing connection!" << endl;
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
    cerr << "Communication with peer failed: " << e.message() << endl;
    clean();
    exit(1);
  }
}

void Connection::localToRemoteR(const error_code& e, size_t)
{
  referenceCount--;
  if(closing) { clean(); return;};
  if(e)
  {
    cerr << "Communication with peer failed: " << e.message() << endl;
    clean();
    exit(1);
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
  async_write(*mainSocket, buffer(reqBuf, count + Header::getSize()), transfer_all(),
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
     clean();
  }
  else
  { // Success
    header.type=Header::Data;
    hasRemotePeer = true;
    referenceCount++;
    localToRemoteR(error_code(), 0);
  }
}


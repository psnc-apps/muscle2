#include "messages.h"
#include <boost/detail/endian.hpp>

template <typename T> T swapBytes(T & value)
{
  unsigned char * ptr = (unsigned char *) &value;
  unsigned char temp;
  for(int i = 0; i < sizeof(T)/2; ++i)
    temp = ptr[i], ptr[i] = ptr[sizeof(T)-i], ptr[sizeof(T)-i] = temp;
}

unsigned Request::getSize()
{
  return sizeof(/*type*/ char)+sizeof(/*srcAddress*/ unsigned int)+sizeof(/*srcPort*/ unsigned short)+sizeof(/*dstAddress*/ unsigned int)+sizeof(/*dstPort*/ unsigned short)+sizeof(/*sessionId*/ int);
}

Request Request::read(char * buf)
{
  Request r;
  r.type       =           *(char*)buf;      buf+=sizeof(r.type);
  r.srcAddress =   *(unsigned int*)buf;      buf+=sizeof(r.srcAddress);
  r.srcPort    = *(unsigned short*)buf;      buf+=sizeof(r.srcPort);
  r.dstAddress =   *(unsigned int*)buf;      buf+=sizeof(r.dstAddress);
  r.dstPort    = *(unsigned short*)buf;      buf+=sizeof(r.dstPort);
  r.sessionId  =            *(int*)buf;      buf+=sizeof(r.sessionId);
  
#ifdef BOOST_BIG_ENDIAN
  swapBytes<unsigned int>(r.srcAddress);
  swapBytes<unsigned short>(r.srcPort);
  swapBytes<unsigned int>(r.dstAddress);
  swapBytes<unsigned short>(r.dstPort);
  swapBytes<int>(r.sessionId);
#endif
  
  return r;
}

void Request::write(char* buf)
{
  unsigned int _srcAddress = srcAddress;
  unsigned short _srcPort = srcPort;
  unsigned int _dstAddress = dstAddress;
  unsigned short _dstPort = dstPort;
  int _sessionId = sessionId;
  
#ifdef BOOST_BIG_ENDIAN
  swapBytes<unsigned int>(_srcAddress);
  swapBytes<unsigned short>(_srcPort);
  swapBytes<unsigned int>(_dstAddress);
  swapBytes<unsigned short>(_dstPort);
  swapBytes<int>(_sessionId);
#endif

            *(char*)buf = type;          buf+=sizeof(char);
    *(unsigned int*)buf = _srcAddress;   buf+=sizeof(unsigned int);
  *(unsigned short*)buf = _srcPort;      buf+=sizeof(unsigned short);
    *(unsigned int*)buf = _dstAddress;   buf+=sizeof(unsigned int);
  *(unsigned short*)buf = _dstPort;      buf+=sizeof(unsigned short);
             *(int*)buf = _sessionId;    buf+=sizeof(int);  
}


unsigned Header::getSize()
{
  return Request::getSize()+sizeof(/*length*/ unsigned int);
}

Header Header::read(char * buf)
{
  
  Header h(Request::read(buf));
  buf+=Request::getSize();
  h.length     =   *(unsigned int*)buf;      buf+=sizeof(h.length);
  return h;
}

Header::Header(const Request & r): Request(r)
{
}

void Header::write(char* buf)
{
  Request::write(buf);
  buf+=Request::getSize();
  *(unsigned int*)buf = length;       buf+=sizeof(unsigned int);
}

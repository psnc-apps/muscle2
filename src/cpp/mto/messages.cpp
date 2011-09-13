#include "messages.h"

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
  return r;
}

void Request::write(char* buf)
{
            *(char*)buf = type;         buf+=sizeof(char);
    *(unsigned int*)buf = srcAddress;   buf+=sizeof(unsigned int);
  *(unsigned short*)buf = srcPort;      buf+=sizeof(unsigned short);
    *(unsigned int*)buf = dstAddress;   buf+=sizeof(unsigned int);
  *(unsigned short*)buf = dstPort;      buf+=sizeof(unsigned short);
             *(int*)buf = sessionId;    buf+=sizeof(int);
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

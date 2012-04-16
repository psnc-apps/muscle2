#include "messages.hpp"
#include <cstring>
#include <cstdio>
#include <sstream>
#include <map>

// Helpers so that endianess will not affect serialisation

/** false if 256 is 0x00 0x01, true if 256 is 0x01 0x00 */
static bool getEndianess(){
  static union {unsigned short t; char c[2]; } x;
  x.t = 0xff00;
  return x.c[0];
}

template <typename T> char *& writeToBuffer( char*& buffer,T value)
{
  unsigned char * ptr = (unsigned char*) &value;
  for(int i = 0 ; i < sizeof(value) ; ++i)
    *(buffer++) = getEndianess() ? *(ptr+sizeof(value)-i-1) : *(ptr+i);
  return buffer;
}

template <typename T> T readFromBuffer( char*& buffer, /*out*/ T * valuePtr = 0)
{
  T value;
  unsigned char * ptr = (unsigned char*) &value;
  for(int i = 0 ; i < sizeof(value) ; ++i)
    (getEndianess() ? *(ptr+sizeof(value)-i-1) : *(ptr+i)) = *(buffer++);
  if(valuePtr) *valuePtr = value;
  return value;
}

std::string Request::typeToString(Request::Type t)
{
  switch(t){
    case Register:
      return "Register";
    case Connect:
      return "Connect";
    case ConnectResponse:
      return "ConnectResponse";
    case Data:
      return "Data";
    case Close:
      return "Close";
    case PortRangeInfo:
      return "PortRangeInfo";
  }
  char name[22];
  sprintf(name, "Unknown (%d)", t);
  return name;
}

unsigned Request::getSize()
{
  return sizeof(/*type*/ char)+sizeof(/*srcAddress*/ unsigned int)+sizeof(/*srcPort*/ unsigned short)+sizeof(/*dstAddress*/ unsigned int)+sizeof(/*dstPort*/ unsigned short)+sizeof(/*sessionId*/ int);
}

Request Request::deserialize(char * buf)
{
  Request r;
  readFromBuffer(buf, & r.type);
  readFromBuffer(buf, & r.srcAddress);
  readFromBuffer(buf, & r.srcPort);
  readFromBuffer(buf, & r.dstAddress);
  readFromBuffer(buf, & r.dstPort);
  readFromBuffer(buf, & r.sessionId);
  return r;
}

void Request::serialize(char* buf) const
{
  writeToBuffer(buf, type);
  writeToBuffer(buf, srcAddress);
  writeToBuffer(buf, srcPort);
  writeToBuffer(buf, dstAddress);
  writeToBuffer(buf, dstPort);
  writeToBuffer(buf, sessionId);
}


unsigned Header::getSize()
{
  return Request::getSize()+sizeof(/*length*/ unsigned int);
}

Header Header::deserialize(char * buf)
{
  
  Header h(Request::deserialize(buf));
  buf+=Request::getSize();
  readFromBuffer(buf, & h.length);
  return h;
}

Header::Header(const Request & r): Request(r)
{
}

void Header::serialize(char* buf) const
{
  Request::serialize(buf);
  buf+=Request::getSize();
  writeToBuffer(buf, length);
}

bool Identifier::operator==(const Identifier& other) const
{
  if(srcAddress!=other.srcAddress)
    return false;
  if(dstAddress!=other.dstAddress)
    return false;
  if(srcPort!=other.srcPort)
    return false;
  if(dstPort!=other.dstPort)
    return false;
  return true;
}

bool Identifier::operator<(const Identifier& other) const
{
  if(srcAddress<other.srcAddress)
    return true;
  if(dstAddress<other.dstAddress)
    return true;
  if(srcPort<other.srcPort)
    return true;
  if(dstPort<other.dstPort)
    return true;
  return false;
}

#define USE_TEXT_FOR_HELLO true

unsigned MtoHello::getSize()
{
#ifdef USE_TEXT_FOR_HELLO
  return 20;
#else
   return sizeof(/* portLow */ unsigned short) + sizeof(/* portHigh */ unsigned short)
          + sizeof(/* distance */ unsigned short)
          + sizeof( /* isLastMtoHello as char */ char );
#endif
}

MtoHello MtoHello::deserialize(char * buf)
{
  MtoHello hello;
#ifdef USE_TEXT_FOR_HELLO
  int isLastMtoHello;
  sscanf(buf,"%6hu%6hu%6hu %d", &hello.portLow, &hello.portHigh, &hello.distance, &isLastMtoHello);
  hello.isLastMtoHello = isLastMtoHello;
#else
  readFromBuffer(buf, &hello.portLow);
  readFromBuffer(buf, &hello.portHigh);
  readFromBuffer(buf, &hello.distance);
  readFromBuffer(buf, ((char*)&hello.isLastMtoHello));
#endif
  return hello;
}

void MtoHello::serialize(char * buf) const
{
#ifdef USE_TEXT_FOR_HELLO
  sprintf(buf,"%6hu%6hu%6hu %d", portLow, portHigh, distance, isLastMtoHello?1:0);
#else
  writeToBuffer(buf, portLow);
  writeToBuffer(buf, portHigh);
  writeToBuffer(buf, distance);
  writeToBuffer(buf, (char)isLastMtoHello);
#endif
}


bool MtoHello::operator==(const MtoHello& o)
{
  if(o.portHigh!=portHigh)
    return false;
  if(o.distance!=distance)
    return false;
  if(o.portLow!=portLow)
    return false;
  return o.isLastMtoHello==isLastMtoHello;
}


std::string MtoHello::str() const
{
    std::stringstream ss;
    ss << portLow << "-" << portHigh;
    return ss.str();
}
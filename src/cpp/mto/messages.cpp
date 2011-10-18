#include "messages.h"
#include <cstring>
#include <cstdio>
#include <boost/unordered_map.hpp>

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

// char *& writeAddressToBuffer(char *& buffer, unsigned int address)
// {
//   memcpy(buffer, (const unsigned char*) &address, 4);
//   buffer+=4;
//   return buffer;
// }
// 
// unsigned int readAddressFromBuffer(char *& buffer, unsigned int * addressPtr = 0)
// {
//   unsigned int address;
//   memcpy((unsigned char*) &address, buffer, 4);
//   buffer+=4;
//   if(addressPtr) *addressPtr = address;
//   return address;
// }


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



// The messages.cpp file

unsigned Request::getSize()
{
  return sizeof(/*type*/ char)+sizeof(/*srcAddress*/ unsigned int)+sizeof(/*srcPort*/ unsigned short)+sizeof(/*dstAddress*/ unsigned int)+sizeof(/*dstPort*/ unsigned short)+sizeof(/*sessionId*/ int);
}

Request Request::read(char * buf)
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

void Request::write(char* buf) const
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

Header Header::read(char * buf)
{
  
  Header h(Request::read(buf));
  buf+=Request::getSize();
  readFromBuffer(buf, & h.length);
  return h;
}

Header::Header(const Request & r): Request(r)
{
}

void Header::write(char* buf) const
{
  Request::write(buf);
  buf+=Request::getSize();
  writeToBuffer(buf, length);
}

std::size_t hash_value(const Identifier& b)
{
    boost::hash<int> h;
    return h(b.dstAddress)-h(b.srcAddress)+h(b.dstPort)-h(b.srcPort);
}

unsigned MtoHello::getSize()
{
  return sizeof(/* portLow */ unsigned short) + sizeof(/* portHigh */ unsigned short)
         + sizeof(/* distance */ unsigned short)
//          + sizeof(/* mtoId */ unsigned short)
         + sizeof( /* isLastMtoHello as char */ char );
}

MtoHello MtoHello::read(char * buf)
{
  MtoHello hello;
  readFromBuffer(buf, &hello.portLow);
  readFromBuffer(buf, &hello.portHigh);
//   readFromBuffer(buf, &hello.mtoId);
  readFromBuffer(buf, &hello.distance);
  readFromBuffer(buf, ((char*)&hello.isLastMtoHello));
  return hello;
}

void MtoHello::write(char * buf) const
{
  writeToBuffer(buf, portLow);
  writeToBuffer(buf, portHigh);
//   writeToBuffer(buf, mtoId);
  writeToBuffer(buf, distance);
  writeToBuffer(buf, (char)isLastMtoHello);
}

#include "messages.h"
#include <cstring>

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

char *& writeAddressToBuffer(char *& buffer, unsigned int address)
{
  memcpy(buffer, (const unsigned char*) &address, 4);
  buffer+=4;
  return buffer;
}

unsigned int readAddressFromBuffer(char *& buffer, unsigned int * addressPtr = 0)
{
  unsigned int address;
  memcpy((unsigned char*) &address, buffer, 4);
  buffer+=4;
  if(addressPtr) *addressPtr = address;
  return address;
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
  readAddressFromBuffer(buf, & r.srcAddress);
  readFromBuffer(buf, & r.srcPort);
  readAddressFromBuffer(buf, & r.dstAddress);
  readFromBuffer(buf, & r.dstPort);
  readFromBuffer(buf, & r.sessionId);
  return r;
}

void Request::write(char* buf)
{
  writeToBuffer(buf, type);
  writeAddressToBuffer(buf, srcAddress);
  writeToBuffer(buf, srcPort);
  writeAddressToBuffer(buf, dstAddress);
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

void Header::write(char* buf)
{
  Request::write(buf);
  buf+=Request::getSize();
  writeToBuffer(buf, length);
}

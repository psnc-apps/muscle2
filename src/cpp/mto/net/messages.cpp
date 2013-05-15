
#include "messages.hpp"
#include "../util/exception.hpp"

#include <cstdio>
#include <sstream>
#include <cstring>

// Helpers so that endianess will not affect serialisation

/** false if 256 is 0x00 0x01, true if 256 is 0x01 0x00 */
static bool isLittleEndian(){
    static union {unsigned short t; char c[2]; } x;
    x.t = 0xff00;
    return x.c[0];
}

static bool littleEndian = isLittleEndian();

template <typename T> char *& writeToBuffer( char*& buffer,T value)
{
    unsigned char * ptr = (unsigned char*) &value;
    
    if (littleEndian)
        for(int i = sizeof(value) - 1; i >= 0; --i)
            *(buffer++) = *(ptr+i);
    else
        for(int i = 0; i < sizeof(value); ++i)
            *(buffer++) = *(ptr++);
    
    return buffer;
}

template <typename T> T readFromBuffer( char*& buffer, /*out*/ T * valuePtr = 0)
{
    T value;
    unsigned char * ptr = (unsigned char*) &value;
    
    if (littleEndian)
        for(int i = sizeof(value) - 1; i >= 0; --i)
            *(ptr+i) = *(buffer++);
    else
        for(int i = 0; i < sizeof(value); ++i)
            *(ptr++) = *(buffer++);
    
    if(valuePtr) *valuePtr = value;
    return value;
}


std::string Request::type_str() const
{
    switch((Request::Type)type){
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
        case PeerClose:
            return "PeerClose";
        default:
        {
            std::stringstream ss;
            ss << "Type " << type << " is not well-specified";
            throw muscle::muscle_exception(ss.str());
        }
    }
}

size_t Request::getSize()
{
    return sizeof(/*type*/ char)+2*muscle::endpoint::getSize()+sizeof(/*sessionId*/ int);
}

Request::Request(char * buf) : type(*buf), src(1+buf), dst(1+buf+muscle::endpoint::getSize())
{
    buf += 1+2*muscle::endpoint::getSize();
    readFromBuffer(buf, &sessionId);
}

char *Request::serialize(char* buf) const
{
    writeToBuffer(buf, type);
    buf = src.serialize(buf);
    buf = dst.serialize(buf);
    writeToBuffer(buf, sessionId);
    return buf;
}

size_t Header::getSize()
{
    return Request::getSize()+sizeof(/*length*/ size_t);
}

Header::Header(char * buf) : Request(buf)
{
    buf += Request::getSize();
    readFromBuffer(buf, &length);
}

char *Header::serialize(char* buf) const
{
    buf = Request::serialize(buf);
    writeToBuffer(buf, length);
    return buf;
}

size_t Header::makePacket(char **packet, const void *data, size_t len)
{
    length = len;
    size_t sz = len + getSize();
    *packet = new char[sz];
    serialize(*packet);
    
    if (len > 0)
        memcpy(*packet+getSize(), data, len);
    
    return sz;
}

size_t Header::makePacket(char **packet, size_t value)
{
    length = value;
    size_t sz = getSize();
    *packet = new char[sz];
    serialize(*packet);
    return sz;
}

#define USE_TEXT_FOR_HELLO true

size_t MtoHello::getSize()
{
#ifdef USE_TEXT_FOR_HELLO
    return 21;
#else
    return sizeof(/* portLow */ unsigned short) + sizeof(/* portHigh */ unsigned short)
    + sizeof(/* distance */ unsigned short)
    + sizeof( /* isLastMtoHello as char */ char );
#endif
}

MtoHello::MtoHello(char * buf)
{
#ifdef USE_TEXT_FOR_HELLO
    int iLastMtoHello;
    sscanf(buf,"%6hu%6hu%6hu %d", &portLow, &portHigh, &distance, &iLastMtoHello);
    isLastMtoHello = (bool)iLastMtoHello;
#else
    char cLastMtoHello;
    readFromBuffer(buf, &portLow);
    readFromBuffer(buf, &portHigh);
    readFromBuffer(buf, &distance);
    readFromBuffer(buf, &cLastMtoHello);
    isLastMtoHello = (bool)cLastMtoHello;
#endif
}

void MtoHello::serialize(char * buf) const
{
#ifdef USE_TEXT_FOR_HELLO
    int iLastMtoHello = (int)isLastMtoHello;
    sprintf(buf,"%6hu%6hu%6hu %d", portLow, portHigh, distance, iLastMtoHello);
#else
    writeToBuffer(buf, portLow);
    writeToBuffer(buf, portHigh);
    writeToBuffer(buf, distance);
    writeToBuffer(buf, (char)isLastMtoHello);
#endif
}

bool MtoHello::operator==(const MtoHello& o) const
{
    return o.portHigh == portHigh
        && o.distance == distance
        && o.portLow == portLow
        && o.isLastMtoHello == isLastMtoHello;
}

bool MtoHello::overlaps(const MtoHello &o) const
{
    return o.portHigh >= portLow && o.portLow <= portHigh;
}

bool MtoHello::matches(const MtoHello &o) const
{
    return o.portHigh == portHigh && o.portLow == portLow;
}

bool MtoHello::matches(const muscle::endpoint& ep)
{
    return ep.port >= portLow && ep.port <= portHigh;
}

std::string MtoHello::str() const
{
    std::stringstream ss;
    ss << portLow << "-" << portHigh;
    return ss.str();
}

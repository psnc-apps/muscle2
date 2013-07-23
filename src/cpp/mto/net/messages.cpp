
#include "messages.hpp"
#include "../../muscle2/exception.hpp"

#include <cstdio>
#include <sstream>
#include <cstring>
#include <cassert>

template <typename T> void writeToBuffer(char *&buffer, const T value)
{
    unsigned char *buffer_ptr = (unsigned char *)buffer;
    switch (sizeof(T)) {
        case 8:
            *buffer_ptr++ = (value >> 56) & 0xff;
            *buffer_ptr++ = (value >> 48) & 0xff;
            *buffer_ptr++ = (value >> 40) & 0xff;
            *buffer_ptr++ = (value >> 32) & 0xff;
            // no break
        case 4:
            *buffer_ptr++ = (value >> 24) & 0xff;
            *buffer_ptr++ = (value >> 16) & 0xff;
            // no break
        case 2:
            *buffer_ptr++ = (value >>  8) & 0xff;
            // no break
        case 1:
            *buffer_ptr++ =  value        & 0xff;
            // no break
            break;
        default:
            assert(false);
    }
    
    buffer = (char *)buffer_ptr;
}

template <typename T> T readFromBuffer(char *&buffer, /*out*/ T * valuePtr = 0)
{
    unsigned char *buffer_ptr = (unsigned char *)buffer;
    T value = 0;
    
    switch (sizeof(T)) {
        case 8:
            value |= *buffer_ptr++ << 56;
            value |= *buffer_ptr++ << 48;
            value |= *buffer_ptr++ << 40;
            value |= *buffer_ptr++ << 32;
            // no break
        case 4:
            value |= *buffer_ptr++ << 24;
            value |= *buffer_ptr++ << 16;
            // no break
        case 2:
            value |= *buffer_ptr++ <<  8;
            // no break
        case 1:
            value |= *buffer_ptr++;
            // no break
            break;
        default:
            assert(false);
    }
    
    buffer = (char *)buffer_ptr;
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
            ss << "Type " << (int)type << " is not well-specified";
            throw muscle::muscle_exception(ss.str());
        }
    }
}

size_t Request::getSize()
{
    return sizeof(/*type*/ char)+2*muscle::endpoint::getSize()+sizeof(/*sessionId*/ int);
}

Request::Request(char *buf) : type(*buf), src(1+buf), dst(1+buf+muscle::endpoint::getSize())
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

Header::Header(char *buf) : Request(buf)
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

size_t Header::makePacket(char **packet, size_t value)
{
    length = value;
    size_t sz = getSize();
    *packet = new char[sz];
    serialize(*packet);
    return sz;
}

size_t MtoHello::getSize()
{
    return sizeof(/* portLow */ unsigned short) + sizeof(/* portHigh */ unsigned short)
    + sizeof(/* distance */ unsigned short)
    + sizeof( /* isLastMtoHello as char */ char );
}

MtoHello::MtoHello(char * buf)
{
    readFromBuffer(buf, &portLow);
    readFromBuffer(buf, &portHigh);
    readFromBuffer(buf, &distance);
    isLastMtoHello = (bool)readFromBuffer<char>(buf);
}

void MtoHello::serialize(char * buf) const
{
    writeToBuffer(buf, portLow);
    writeToBuffer(buf, portHigh);
    writeToBuffer(buf, distance);
    writeToBuffer(buf, (char)isLastMtoHello);
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

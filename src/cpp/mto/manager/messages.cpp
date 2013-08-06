
#include "messages.hpp"
#include "muscle2/util/exception.hpp"
#include "muscle2/util/serialization.h"

#include <cstdio>
#include <sstream>
#include <cstring>
#include <cassert>

using namespace muscle;
using namespace std;

string Request::type_str() const
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
		case DataInLength:
			return "DataInLength";
        default:
        {
            stringstream ss;
            ss << "Type " << (int)type << " is not well-specified";
            throw muscle_exception(ss.str());
        }
    }
}

size_t Request::getSize()
{
    return sizeof(/*type*/ char)+2*endpoint::getSize()+sizeof(/*sessionId*/ int32_t);
}

Request::Request(char *buf) : type(*buf), src(1+buf), dst(1+buf+muscle::endpoint::getSize())
{
    buf += 1+2*endpoint::getSize();
    sessionId = readFromBuffer<int32_t>(buf);
	src.resolve();
	dst.resolve();
}

char *Request::serialize(char* buf) const
{
    writeToBuffer(buf, type);
    buf = src.serialize(buf);
    buf = dst.serialize(buf);
    writeToBuffer<int32_t>(buf, sessionId);
    return buf;
}

size_t Header::getSize()
{
    return Request::getSize()+sizeof(/*length*/ int64_t);
}

Header::Header(char *buf) : Request(buf)
{
    buf += Request::getSize();
    length = readFromBuffer<int64_t>(buf);
}

char *Header::serialize(char* buf) const
{
    buf = Request::serialize(buf);
    writeToBuffer<int64_t>(buf, length);
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
    return 3*sizeof(/* portLow, portHigh, distance */ uint16_t)
           + sizeof( /* isLastMtoHello as char */ char );
}

MtoHello::MtoHello(char * buf)
{
    portLow =  readFromBuffer<uint16_t>(buf);
    portHigh = readFromBuffer<uint16_t>(buf);
    distance = readFromBuffer<uint16_t>(buf);
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

string MtoHello::str() const
{
    stringstream ss;
    ss << portLow << "-" << portHigh;
    return ss.str();
}

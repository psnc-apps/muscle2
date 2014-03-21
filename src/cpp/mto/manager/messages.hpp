#ifndef MESSAGES_H
#define MESSAGES_H

#include "muscle2/util/endpoint.h"

/** Message from client to MTO */
class Request
{
public:
    /** Type of message or header */
    enum Type {
        Register = 1,         ///< Client informs that it listens on the given srcAddress:srcPort
        Connect = 2,          ///< Client wants to conect from src to dest OR one proxy forward to another the client request for it
        ConnectResponse = 3,  ///< Proxy responds if the Connect suceeded
        Data = 4,             ///< One proxy sends client data to the other proxy
        Close = 5,            ///< A client closed the connection on one end
        PortRangeInfo = 6,    ///< Proxy tells what ports it owns
		DataInLength = 7
    };
    
    /** Converts the Type value ot it's textual representation */
    virtual std::string type_str() const;
    
    /** Type of message or header */
    char type;
    
    /**
     * The source and destination stay unchanged,
     * and are always seen from the 'Connect' point of view
     * (i.e. these are identical on both ends)
     */
    
    muscle::net::endpoint src, ///< Source address for the connection
                     dst; ///< Destination address for the connection
    
    /** Serializes the Request to an existing char* of size at least of getSize */
    virtual char *serialize(char* buf) const;
    
    virtual std::string str() const {
        std::stringstream ss;
        ss << *this;
        return ss.str();
    }
    
    /** Size, in bytes, of the serialized request */
    static size_t getSize();

    Request() : type(0), sessionId(0) {}
    Request(char type, const muscle::net::endpoint &src, const muscle::net::endpoint &dst) : type(type), src(src), dst(dst), sessionId(0) {}
    Request(char *buf);
protected:
    /** Some unused int field */
    int sessionId;
private:
    friend std::ostream& operator<<(std::ostream &os, const Request& r)
	{ return os << r.src << " - " << r.dst; }
};

/**
 * All that's needed to identify a connection. Implements all mandatory methods for (tree and hash) map keys.
 */
class Identifier
{
public:
    Identifier() {}
    Identifier(const Request & r) : src(r.src),dst(r.dst) {}
    
    bool operator==(const Identifier & other) const
    { return src == other.src && dst == other.dst; }
    bool operator<(const Identifier & other) const
    { return src < other.src || (src == other.src && dst < other.dst); }
    
    muscle::net::endpoint src, dst;

    std::string str() const {
        std::stringstream ss;
        ss << *this;
        return ss.str();
    }
private:
    friend std::ostream& operator<<(std::ostream &os, const Identifier& r)
	{ return os << r.src << " - " << r.dst; }
};

/** Hash for storing the Identifiers in a hash map */
std::size_t hash_value(const Identifier & b);

/**
 * Header exchnaged between MTO's and resonse to client for connect
 */
class Header : public Request
{
public:
    /** Size, in bytes, of the serialized header */
    static size_t getSize();
    
    /** Serializes the Header to an existing char* of size at least of getSize */
    char *serialize(char* buf) const;
    
    /** Receiver must delete newly created chars. */
    size_t makePacket(char **packet, size_t value);
    
    Header(): length(0) {};
    
    /** Constructs the header basing on the given request */
    Header(const Request & r) : Request(r), length(0) {}
    Header(char type, const Identifier &id) : Request(type, id.src, id.dst), length(0) {}
    Header(char type, const muscle::net::endpoint& src, const muscle::net::endpoint& dst, size_t length) : Request(type, src, dst), length(length) {}
    Header(char *buf);

    /** Length is the length of data */
    size_t length;
};

struct MtoHello
{
    /** Port range */
    uint16_t portLow, portHigh;
    
    /** Hop count */
    uint16_t distance;
    
    /** Indicates if other hellos follow */
    bool isLastMtoHello;
    
    MtoHello() : portLow(0), portHigh(0), distance(0), isLastMtoHello(false) {};
    MtoHello(uint16_t portLow, uint16_t portHigh, uint16_t distance, bool isLastMtoHello = false) : portLow(portLow), portHigh(portHigh), distance(distance), isLastMtoHello(isLastMtoHello) {};
    /** Deserializes the MtoHello from the given buffer */
    MtoHello(char *buf);
    
    /** Size, in bytes, of the serialized MtoHello */
    static size_t getSize();
    
    /** Serializes the MtoHello to an existing char* of size at least of getSize */
    void serialize(char* buf) const;
    
    std::string str() const;
    
    bool operator==(const MtoHello & o) const;
    
    bool overlaps(const MtoHello& o) const;
    bool matches(const MtoHello& o) const;
    bool matches(const muscle::net::endpoint& ep);
};

#endif // MESSAGES_H

#ifndef MESSAGES_H
#define MESSAGES_H

#include <cstdlib>
#include <iostream>

/** Message from client to MTO */
struct Request
{
  /** Type of message or header */
  enum Type { 
    Register = 1,         ///< Client informs that it listens on the given srcAddress:srcPort
    Connect = 2,          ///< Client wants to conect from src to dest OR one proxy forward to another the client request for it
    ConnectResponse = 3,  ///< Proxy responds if the Connect suceeded
    Data = 4,             ///< One proxy sends client data to the other proxy 
    Close = 5,            ///< A client closed the connection on one end
    PortRangeInfo = 6     ///< Proxy tells what ports it owns
  };
  
  static std::string typeToString(Type t);
  
  /** Type of message or header */
  char type; 
  
  /**
   * The source and destination stay unchanged,
   * and are always seen from the 'Connect' point of view
   * (i.e. these are identical on both ends)
   */
  
  unsigned int srcAddress, ///< Source address for the connection
               dstAddress; ///< Destination address for the connection
  unsigned short srcPort,  ///< Source port for the connection
                 dstPort;  ///< Destination port for the connection
                 
  /** Some unused int filed */
  int sessionId;
  
  /** Serializes the Request to an existing char* of size at least of getSize */
  virtual void write(char* buf) const;
  
  /** Size, in bytes, of the serialized request */
  static unsigned getSize();
  
  /** Deserializes request from the given buffer */
  static Request read(char * buf);
};


/** 
 * Header exchnaged between MTO's and resonse to client for connect
 */
struct Header : Request
{
  /** Length is the length of data */
  unsigned int length;
  
  /** Size, in bytes, of the serialized header */
  static unsigned getSize();
  
  /** Deserializes the header from the given buffer */
  static Header read(char * buf);
  
  /** Serializes the Header to an existing char* of size at least of getSize */
  void write(char* buf) const;
  
  Header(){};
  
  /** Constructs the header basing on the given request */
  Header(const Request & r);
};


/**
 * All that's needed to identify a connection
 */ 
struct Identifier
{
  unsigned int srcAddress, dstAddress;
  unsigned short srcPort, dstPort;
  
  Identifier(){};
  Identifier(const Request & r) : srcAddress(r.srcAddress),dstAddress(r.dstAddress), srcPort(r.srcPort), dstPort(r.dstPort){};
  
  bool operator==(const Identifier & other) const
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
  
  bool operator<(const Identifier & other) const{
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
};

/** Hash for storing the Identifiers in a hash map */
std::size_t hash_value(const Identifier & b);


struct MtoHello
{
  /** Port range */
  unsigned short portLow, portHigh;
  
//   /** Owner of this port range */
//   unsigned short mtoId;
  
  /** Hop count */
  unsigned short distance;
  
  /** Indicates if other hellos follow */
  bool isLastMtoHello;
  
  /** Size, in bytes, of the serialized MtoHello */
  static unsigned getSize();
  
  /** Deserializes the MtoHello from the given buffer */
  static MtoHello read(char * buf);
  
  /** Serializes the MtoHello to an existing char* of size at least of getSize */
  void write(char* buf) const;
};

#endif // MESSAGES_H

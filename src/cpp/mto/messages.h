#ifndef MESSAGES_H
#define MESSAGES_H

/** Message from client to MTO */
struct Request
{
  /** Type of message or header */
  enum Type { 
    Register = 1,         ///< Client informs that it listens on the given srcAddress:srcPort
    Connect = 2,          ///< Client wants to conect from src to dest OR one proxy forward to another the client request for it
    ConnectResponse = 3,  ///< Proxy responds if the Connect suceeded
    Data = 4,             ///< One proxy sends client data to the other proxy 
    Close = 5             ///< A client closed the connection on one end
  };
  
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
  virtual void write(char* buf);
  
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
  void write(char* buf);
  
  Header(){};
  
  /** Constructs the header basing on the given request */
  Header(const Request & r);
};


#endif // MESSAGES_H

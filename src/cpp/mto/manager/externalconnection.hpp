#ifndef EXTERNAL_CONNECTION_H
#define EXTERNAL_CONNECTION_H

#include "messages.hpp"
#include "muscle2/util/async_service.h"

#include <set>
#include <map>
#include <queue>
#include <vector>
#include <exception>

/** Size of a receive data buffer for each connection */
#define EXT_MTO_CONNECTION_BUFFER_SIZE 1024

class PeerCollection;
class PeerConnectionHandler;
class LocalMto;

/**
 * Represents a one-time connection between a MUSCLE instance and an external source
 */
class ExternalConnection : public muscle::net::async_recvlistener, public muscle::net::async_sendlistener, public muscle::net::async_acceptlistener
{
private:
	static const muscle::util::duration recvTimeout;
	
    /** Local side of the connection */
    muscle::net::ClientSocket *sock;
    
    /** Represents the remote end */
    muscle::net::ClientSocket *remoteSock;
    muscle::net::socket_opts *sockOpts;
    
    /** Reusable header with proper adresses and ports */
    Header header;
    
    /** How many completion hooks still will call this class */
    int pendingOperations;
    
	void receive(muscle::net::ClientSocket *sock, int user_flag, void *buffer, size_t sz);
    void send(muscle::net::ClientSocket *sock, int user_flag, void *data, size_t length);
    bool forward(muscle::net::ClientSocket *fromSock, muscle::net::ClientSocket *toSock, char *buffer, const size_t count, bool is_eof, int recvFlag, int sendFlag);
    
    void tryClose();

	enum {
        EXT_CONNECT = 1,
        INT_CONNECT_RESPONSE = 2,
        INT_RECEIVE = 3,
        EXT_SEND = 4,
        EXT_RECEIVE = 5,
        INT_SEND = 6
	};
    /** Fires when the response for connection request is received */
    void remoteConnected(bool success);

public:
    ExternalConnection(muscle::net::ClientSocket* s, Header h, muscle::net::SocketFactory *factory);
    
    virtual ~ExternalConnection();
    
    /* Connect */
    virtual void async_accept(size_t code, int user_flag, muscle::net::ClientSocket *newSocket);

    /* Receive */
    virtual bool async_received(size_t code, int user_flag, void *data, void *last_data_ptr, size_t count, int is_final);

    /* Send */
    virtual void async_sent(size_t code, int user_flag, void *data, size_t len, int is_final);
    
    virtual void async_report_error(size_t code, int user_flag, const muscle::muscle_exception& ex);
    virtual void async_done(size_t code, int flag);
};


#endif // EXTERNAL_CONNECTION_H

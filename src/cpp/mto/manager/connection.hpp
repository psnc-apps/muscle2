#ifndef CONNECTION_H
#define CONNECTION_H

#include "../net/messages.hpp"
#include "../net/async_service.h"

#include <set>
#include <map>
#include <queue>
#include <vector>
#include <exception>

/** Size of a receive data buffer for each connection */
#define CONNECTION_BUFFER_SIZE 65536

class PeerCollection;
class PeerConnectionHandler;
class LocalMto;

/**
 * Represents connection between two end points
 */
class Connection : public muscle::async_recvlistener, public muscle::async_sendlistener, public muscle::async_function
{
protected:
    /** Local side of the connection */
    muscle::ClientSocket *sock;
    
    /** Represents the remote end */
    PeerConnectionHandler *secondMto;
    
    /** Reusable header with proper adresses and ports */
    Header header;
    
    /** Buffer for transporting data */
    char *receiveBuffer;
    
    /** Indicates whether there is a peer on the other side */
    bool hasRemotePeer;
    
    /** How many completion hooks still will call this class */
    int referenceCount;
    
    /** If the connections should close / is just closing */
    bool closing;
    
    LocalMto *mto;
    
    void receive();
    void tryClose();
    
    size_t closing_timer;
public:
    Connection() : sock(0), closing(false), hasRemotePeer(false), referenceCount(0), secondMto(0), mto(0)
	{
		receiveBuffer = new char[CONNECTION_BUFFER_SIZE];
	}
    /** Opening connection from REMOTE */
    Connection(Header h, muscle::ClientSocket* s, PeerConnectionHandler * toMto, LocalMto *mto, bool remotePeerConnected);
    
    virtual ~Connection();
    
    /** Close the Connection */
    void close();
    
    virtual void async_execute(size_t code, int flag, void *user_data);
    virtual bool async_received(size_t code, int user_flag, void *data, void *last_data_ptr, size_t count, int is_final);

    /* ====== Local to remote or Remote to local  ===== */
    void send(void *data, size_t length );
    virtual void async_sent(size_t code, int user_flag, void *data, size_t len, int is_final);
    
    virtual void async_report_error(size_t code, int user_flag, const muscle::muscle_exception& ex);
    
    virtual void async_done(size_t code, int flag);
    
    /** Fires when the response for connection request is received */
    void remoteConnected(Header h);

    /** Called when remote peer closed */
    void remoteClosed();
    
    /** Once a peer becomes unavailable, this is called. If this connection uses the peer, it closes. */
    void peerDied(PeerConnectionHandler * handler);
    
    /** Requests replacing one PeerConnectionHandler to another */
    void replacePeer(PeerConnectionHandler* from, PeerConnectionHandler* to);
};


#endif // CONNECTION_H

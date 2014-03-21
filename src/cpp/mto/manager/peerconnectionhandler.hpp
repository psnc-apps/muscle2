#ifndef PEERCONNECTIONHANDLER_H
#define PEERCONNECTIONHANDLER_H

#include "../constants.hpp"
#include "muscle2/util/async_service.h"
#include "messages.hpp"

#include <vector>
#include <string>
#include <map>
#include <cassert>

class StubbornConnecter;
class LocalMto;

/**
 * The part responsible for interconnection between two MTOs.
 */
class PeerConnectionHandler : public muscle::net::async_recvlistener, public muscle::net::async_sendlistener
{
public:
    PeerConnectionHandler(muscle::net::ClientSocket * _socket, LocalMto *mto);
    virtual ~PeerConnectionHandler();
    
    /** Informs peer about MTO reachable via me */
    void propagateHello(const MtoHello & hello);
    
    /** Informs peer about MTOs reachable via me */
    void propagateHellos(std::vector<MtoHello> & hellos);
    
    /** Hook for uniform error handling */
    virtual void async_report_error(size_t code, int user_flag, const muscle::muscle_exception& ex);
    virtual bool async_received(size_t code, int user_flag, void *data, void *last_data_ptr, size_t len, int is_final);
	virtual void async_sent(size_t code, int user_flag, void *data, size_t len, int is_final);

    virtual void async_done(size_t code, int user_flag) { decrementPending(); }
    
    /** Returns the remote endpoint for this connection*/
    const muscle::net::endpoint & address() const;
    
    void send(Header& h, void *data, size_t len);
    void sendHeader(Header& h, size_t value = 0);
    
    /** Once a peer becomes unavailable, this is called. If the peer is on fwd list, fwd list is updated */
    void peerDied(PeerConnectionHandler * handler);
    
    /** Requests replacing one peer to other in fwd tables */
    void replacePeer(PeerConnectionHandler* from, PeerConnectionHandler* to);
    
    void done();
	
	inline std::string str() { return socket->getAddress().str(); }

private:
    bool closing;
	muscle::net::async_service *service;
    Header latestHeader;
    muscle::net::ClientSocket *socket;
    LocalMto *mto;
    char *headerBuffer;
    int pendingOperations;

	enum {
		RECV_HEADER = 1,
		RECV_DATA = 2,
		LOCAL_CONNECT = 3,
		SEND_DATA = 4,
		SEND_HEADER = 5
	};
	
	/** Keeps information about connections forwarded by this MTO */
    std::map<Identifier, PeerConnectionHandler*> fwdMap;

	/** Starts reading new header from peer */
    void readHeader();
    
	/** forward data to closest peer */
	bool forwardToPeer(Header &h, bool erase, void *data = 0, size_t dataLen = 0);
    
    /** On connect, check if we have registered ip:port and try to connect */
    void handleConnect(Header &h);
    /** Answer for connection request */
    void handleConnectFailed(Header &h);
    /** Propagate a MTO hello to main.h */
    void handleHello(Header &h);
    /** Informs proper Connection that it's remote end is or is not available */
    void handleConnectResponse(Header &h);
    void handleData(const Header &h);
    void handleDataInLength(const Header &h, size_t code);
    void handleClose(Header &h);

    /** Once connection requested by peer has been established or failed, this is called */
    struct HandleConnected : public muscle::net::async_acceptlistener
    {
        Header h;
        PeerConnectionHandler *t;
		muscle::net::socket_opts opts;
        HandleConnected(Header& header, PeerConnectionHandler *thiz);
        virtual ~HandleConnected() {}
        virtual void async_accept(size_t code, int user_flag, muscle::net::ClientSocket *newSocket);
        virtual void async_report_error(size_t code, int user_flag, const muscle::muscle_exception& ex);
        virtual void async_done(size_t code, int user_flag);
    };

	/* Closing */
	void tryClose();
    void errorOccurred(std::string msg);
    
	inline void incrementPending() {
        assert(!closing);
		
        ++pendingOperations;
    }
    inline void decrementPending() {
        --pendingOperations;
        if (closing) tryClose();
	}
};

#endif // PEERCONNECTIONHANDLER_H

#ifndef PEERCONNECTIONHANDLER_H
#define PEERCONNECTIONHANDLER_H

#include "../constants.hpp"
#include "../net/async_service.h"
#include "../net/messages.hpp"

#include <vector>
#include <string>
#include <map>

class StubbornConnecter;
class LocalMto;

/**
 * The part responsible for interconnection between two MTOs.
 */
class PeerConnectionHandler : public muscle::async_recvlistener
{
    /** Starts reading new header from peer */
    void readHeader();
    
public:
    PeerConnectionHandler(muscle::ClientSocket * _socket, LocalMto *mto);
    virtual ~PeerConnectionHandler();
    
    inline std::string str() { return socket->getAddress().str(); }

    /** Fires on accept / connect */
    muscle::async_service *service;
    
    /** Informs peer about MTO reachable via me */
    void propagateHello(const MtoHello & hello);
    
    /** Informs peer about MTOs reachable via me */
    void propagateHellos(std::vector<MtoHello> & hellos);
    
    /** Hook for uniform error handling */
    virtual void async_report_error(size_t code, int user_flag, const muscle::muscle_exception& ex);
    virtual bool async_received(size_t code, int user_flag, void *data, size_t len, int is_final);

    virtual void async_done(size_t code, int user_flag) { decrementPending(); }
    
    /** Returns the remote endpoint for this connection*/
    const muscle::endpoint & remoteEndpoint() const;
    
    void send(Header& h, void *data = 0, size_t len = 0, muscle::async_sendlistener *listener = 0);
    void send(Header& h, size_t value, muscle::async_sendlistener *listener = 0);
    void send(void *data, size_t len, muscle::async_sendlistener *listener, int opts);
    
    /** Once a peer becomes unavailable, this is called. If the peer is on fwd list, fwd list is updated */
    void peerDied(PeerConnectionHandler * handler);
    
    /** Requests replacing one peer to other in fwd tables */
    void replacePeer(PeerConnectionHandler* from, PeerConnectionHandler* to);
    
    void done();
protected:
    int pendingOperatons;
    bool closing;
    
    void incrementPending() {
        if (closing)
            throw muscle::muscle_exception("Can not start new operation on closing connection");
        
        ++pendingOperatons;
    }
    void decrementPending() {
        --pendingOperatons;
        tryClean();
    }
    
    bool tryClean();
    
    Header latestHeader;
    
    void errorOccurred(std::string msg);
    
    /** Keeps information about connections forwarded by this MTO */
    std::map<Identifier, PeerConnectionHandler*> fwdMap;
    
    /** Ptr to this inter-proxy socket */
    muscle::ClientSocket *socket;
    
    LocalMto *mto;
    char *dataBufffer;
    
    /* ====== Connect ====== */
    
    /** On connect, check if we have registered ip:port and try to connect */
    void handleConnect(Header h);
    
    /** Answer for connection request */
    void handleConnectFailed(Header h);
    
    /** Other PeerConnectionHandler asks to forward data through this connection */
    void forward(Header & h, size_t dataLen = 0, void *data = 0);
    
    /** Propagate a MTO hello to main.h */
    void handleHello(Header h);
    
    struct Sender : public muscle::async_sendlistener
    {
        muscle::async_sendlistener *listener;
        PeerConnectionHandler *t;

        Sender(muscle::async_sendlistener *listener, PeerConnectionHandler *t, void *data, size_t len, int opts);
        
        virtual void async_sent(size_t code, int user_flag, void *data, size_t len, int is_final);
        virtual void async_report_error(size_t code, int user_flag, const muscle::muscle_exception& ex);
        virtual void async_done(size_t code, int user_flag);
    };
    
    /** Once connection requested by peer has been established or failed, this is called */
    struct HandleConnected : public muscle::async_acceptlistener
    {
        Header h;
        PeerConnectionHandler *t;
		muscle::socket_opts opts;
        HandleConnected(Header& header, PeerConnectionHandler *thiz);
        
        virtual void async_accept(size_t code, int user_flag, muscle::ClientSocket *newSocket);
        virtual void async_report_error(size_t code, int user_flag, const muscle::muscle_exception& ex);
        virtual void async_done(size_t code, int user_flag);
    };
    
    /* ====== ConnectResponse ====== */
    
    /** Informs proper Connection that it's remote end is / is not available */
    void handleConnectResponse(Header h);
    
    /* ====== Data ====== */
    
    void handleData(Header h);
    
    /* ====== Close ====== */
    
    void handleClose(Header h);
};

#endif // PEERCONNECTIONHANDLER_H

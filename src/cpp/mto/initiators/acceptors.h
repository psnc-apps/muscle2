//
//  acceptors.h
//  CMuscle
//
//  Created by Joris Borgdorff on 5/6/13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#ifndef __CMuscle__acceptors__
#define __CMuscle__acceptors__

#include "helloreader.h"

#define ACCEPT_EXTERNAL 25
#define ACCEPT_INTERNAL 26

class LocalMto;

class Acceptor : public muscle::net::async_acceptlistener
{
protected:
    /** Ptr to new socket */
    muscle::net::ServerSocket * const ss;
    LocalMto * const mto;
    
public:
    Acceptor(muscle::net::ServerSocket *sock, LocalMto *mto);
    virtual ~Acceptor() { delete ss; }
    
    virtual void async_report_error(size_t code, int flag, const muscle::muscle_exception& ex);
};

/**
 * This class waits on internal socket, i.e. socket accepting connections from inside of the cluster.
 */
class InternalAcceptor : public Acceptor
{
public:
    InternalAcceptor(muscle::net::ServerSocket *_ss, LocalMto *mto, muscle::net::socket_opts *client_opts) : Acceptor(_ss, mto) {
	    _ss->async_accept(ACCEPT_INTERNAL, this, client_opts);
	}
    
    /** Triggered on accept; reads the header and constructs a connection */
    virtual void async_accept(size_t code, int flag, muscle::net::ClientSocket *sock);
};

struct ExternalAcceptor : public Acceptor
{
public:
    ExternalAcceptor(muscle::net::ServerSocket * _ss, LocalMto *mto, muscle::net::socket_opts *client_opts) : Acceptor(_ss, mto) {
		_ss->async_accept(ACCEPT_EXTERNAL, this, client_opts);
	}
    
    /** Triggered on accept; reads the header and constructs a connection */
    virtual void async_accept(size_t code, int flag, muscle::net::ClientSocket *sock);
};

/**
 * Once a connection is accepted, this class takes responsibility for creating a PeerConnectionHandler
 */
class InitPeerConnection : public Initiator
{
private:
    muscle::net::ClientSocket * const sock;
    std::vector<MtoHello> hellos;
    LocalMto * const mto;
    
public:
    InitPeerConnection(muscle::net::ClientSocket *_sock, LocalMto *mto);
    virtual void allHellosRead();
    virtual void allHellosFailed(const muscle::muscle_exception& ex);
};

class InitConnection : public muscle::net::async_recvlistener, public muscle::net::async_sendlistener
{
private:
    // Set to null if it needs to be preserved for another object
    muscle::net::ClientSocket *sock;
    LocalMto * const mto;
    int refs;
    
    void registerAddress(const Request &request);
    void connect(const Request &request);
public:
    InitConnection(muscle::net::ClientSocket *sock, LocalMto *mto);
    virtual ~InitConnection() { delete sock; }
    
    virtual void async_report_error(size_t code, int flag, const muscle::muscle_exception& ex);
    virtual bool async_received(size_t code, int flag, void *data, void *last_data_ptr, size_t len, int final);
    virtual void async_sent(size_t code, int flag, void *data, size_t len, int final);
    virtual void async_done(size_t code, int flag) { if (--refs == 0) delete this; }
};

#endif /* defined(__CMuscle__acceptors__) */

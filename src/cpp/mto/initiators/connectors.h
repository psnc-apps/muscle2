//
//  connectors.h
//  CMuscle
//
//  Created by Joris Borgdorff on 04-05-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#ifndef __CMuscle__connectors__
#define __CMuscle__connectors__

#include "helloreader.h"

#include <vector>

class LocalMto;

/**
 * Connects to a given peer until success. Retries every peerReconnectTimeout.
 * On successfull connection creates PeerConnectionHandler and finihses
 */
struct StubbornConnecter : public Initiator, public muscle::async_function, public muscle::async_acceptlistener
{
private:
    muscle::endpoint where;
    muscle::async_service *service;
    muscle::socket_opts opts;
    const muscle::duration timeout;
    muscle::ClientSocket *sock;
    muscle::SocketFactory *sockFactory;
    size_t timer;
    size_t sockId;
    std::vector<MtoHello> hellos;
    LocalMto *mto;
    
public:
    StubbornConnecter(muscle::endpoint& ep, muscle::async_service *service, muscle::SocketFactory *sockFactory, muscle::socket_opts& opts, const muscle::duration& timeout, LocalMto *mto);
    virtual ~StubbornConnecter() { if (sock) delete sock; }
    
    /** Executed after no responce has been received (or at abort) */
    virtual void async_execute(size_t code, int flag, void *user_data);
    
    /** Executed when connecting state changed (new connection, or some error)  */
    virtual void async_accept(size_t code, int flag, muscle::ClientSocket *sock);
    
    virtual void async_report_error(size_t code, int flag, const muscle::muscle_exception& ex);
    
    virtual void allHellosRead();
    virtual void allHellosFailed(const muscle::muscle_exception& ex);
};

#endif /* defined(__CMuscle__connectors__) */

//
//  connectors.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 04-05-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include "connectors.h"

#include "../constants.hpp"
#include "../net/async_service.h"
#include "../manager/localmto.h"

using namespace std;

StubbornConnecter::StubbornConnecter(muscle::endpoint& where_, muscle::async_service *service, muscle::socket_opts& opts, const muscle::duration& timeout, LocalMto *mto)
: where(where_), service(service), opts(opts), timeout(timeout), mto(mto), sock(NULL)
{
    muscle::time t = timeout.time_after();
    timer = service->timer(1, t, this, (void *)0);
    sockId = muscle::ClientSocket::async_connect(service, 1, where, opts, this);
}

void StubbornConnecter::async_execute(size_t code, int flag, void *user_data)
{
    service->erase_connect(sockId);
    
    sockId = muscle::ClientSocket::async_connect(service, 1, where, opts, this);

    muscle::time t = timeout.time_after();
    service->update_timer(timer, t, (void *)0);
}

void StubbornConnecter::async_accept(size_t code, int flag, muscle::ClientSocket *sock_)
{
    service->erase_timer(timer);
    
    Logger::trace(Logger::MsgType_PeerConn, "Connected to peer %s, starting hello exchange", where.str().c_str());
    
    sock = sock_;
    // TODO install error listener
    mto->peers.introduce(sock);
    HelloReader *rd = new HelloReader(sock, this, hellos);
}

void StubbornConnecter::async_report_error(size_t code, int flag, const muscle::muscle_exception& ex)
{
    if (ex.error_code == ECONNREFUSED)
    {
        Logger::info(Logger::MsgType_PeerConn, "Connection refused to %s - will retry later", where.str().c_str());
    }
    else if (ex.error_code != ECONNABORTED)
    {
        service->erase_timer(timer);
        Logger::error(Logger::MsgType_PeerConn, "Encountered error while connecting to %s: %s. Aborting connection.",
                      where.str().c_str(), ex.what());
    }
    service->erase_connect(sockId);
}

void StubbornConnecter::allHellosRead()
{
    mto->peers.create(sock, hellos);
    sock = NULL; // don't delete
}

void StubbornConnecter::allHellosFailed(const muscle::muscle_exception &ex)
{
    Logger::trace(Logger::MsgType_PeerConn, "Reading hellos from peer %s failed - occurred error: %s",
                  sock->str().c_str(), ex.what());
    StubbornConnecter *sc = new StubbornConnecter(where, service, opts, timeout, mto);
}

//
//  connectors.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 04-05-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include "connectors.h"

#include "../constants.hpp"
#include "muscle2/util/async_service.h"
#include "../manager/localmto.h"

using namespace std;
using namespace muscle;
using namespace muscle::net;
using namespace muscle::util;

StubbornConnecter::StubbornConnecter(const endpoint& where_, async_service *service, SocketFactory *sockFactory, socket_opts& newOpts, const duration& timeout, LocalMto *mto)
: where(where_), service(service), opts(newOpts), timeout(timeout), mto(mto), sock(NULL), sockFactory(sockFactory)
{
    muscle::util::mtime t = timeout.time_after();
    timer = service->timer(1, t, this, (void *)0);
    sockId = sockFactory->async_connect(1, where, &opts, this);
}

StubbornConnecter::~StubbornConnecter()
{
	if (sockId != 0) service->erase_connect(sockId);
	else delete sock;
}

void StubbornConnecter::async_execute(size_t code, int flag, void *user_data)
{
	// Only do a reconnect if the previous connect already failed
	if (sockId == 0) {
		sockId = sockFactory->async_connect(1, where, &opts, this);
		
		muscle::util::mtime t = timeout.time_after();
		service->update_timer(timer, t, (void *)0);
	}
}

void StubbornConnecter::async_accept(size_t code, int flag, ClientSocket *sock_)
{
	// Connected; don't do another connect
    service->erase_timer(timer);
    sockId = 0;
	
    logger::finest("Connected to peer %s, starting hello exchange", where.str().c_str());
    
    sock = sock_;
    mto->peers.introduce(sock);
	// Self-destruct
    new HelloReader(sock, this, hellos);
}

void StubbornConnecter::async_report_error(size_t code, int flag, const muscle_exception& ex)
{
    if (ex.error_code == ECONNREFUSED) {
        logger::info("Connection refused to %s - will retry later", where.str().c_str());
    } else if (ex.error_code != ECONNABORTED) {
        service->erase_timer(timer);
        logger::severe("Encountered error while connecting to %s: %s. Aborting connection.",
                      where.str().c_str(), ex.what());
    }
    service->erase_connect(sockId);
	sockId = 0;
}

void StubbornConnecter::allHellosRead()
{
    mto->peers.create(sock, hellos);
    sock = NULL; // don't delete
	delete this;
}

void StubbornConnecter::allHellosFailed(const muscle_exception &ex)
{
    logger::finest("Reading hellos from peer %s failed - occurred error: %s",
                  sock->str().c_str(), ex.what());
	// Self-destruct
    new StubbornConnecter(where, service, sockFactory, opts, timeout, mto);
	delete this;
}

//
//  msocket.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 4/15/13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include "msocket.h"
#include "async_service.h"

using namespace muscle;
using namespace muscle::net;
using namespace std;

msocket::msocket(endpoint& ep, async_service *service) : address(ep), hasAddress(true), server(service)
{
    if (!address.isValid())
        throw muscle_exception("endpoint " + address.str() + " cannot be resolved");
}

msocket::msocket(async_service *service) : address("", 0), hasAddress(false), server(service) {}

msocket::msocket() : address("", 0), hasAddress(false), server(NULL) {}

msocket::msocket(const msocket& other) : address(other.address), server(other.server), hasAddress(other.hasAddress) {}

const endpoint& msocket::getAddress() const
{
    return address;
}

async_service *msocket::getServer() const
{
    return server;
}

string msocket::str() const
{
    stringstream ss;
    ss << "socket[" << address << "]";
    return ss.str();
}
string msocket::str()
{
    stringstream ss;
    ss << "socket[" << address << "]";
    return ss.str();
}

/** CLIENT SIDE **/
ssize_t ClientSocket::async_recv(int user_flag, void* s, size_t size, async_recvlistener *receiver)
{
    return server->receive(user_flag, this, s, size, receiver);
}

ssize_t ClientSocket::async_send(int user_flag, const void* s, size_t size, async_sendlistener *send, int opts)
{
    return server->send(user_flag, this, s, size, send, opts);
}

/** SERVER SIDE **/

ServerSocket::ServerSocket(const socket_opts& opts)
{
    listen(opts.max_connections);
}

void ServerSocket::listen(int max_connections)
{}

size_t ServerSocket::async_accept(int user_flag, async_acceptlistener *accept, socket_opts *opts)
{
	return server->listen(user_flag, this, opts, accept);
}


size_t SocketFactory::async_connect(int user_flag, endpoint &ep, socket_opts *opts, async_acceptlistener *accept)
{
    return service->connect(user_flag, this, ep, opts, accept);
}


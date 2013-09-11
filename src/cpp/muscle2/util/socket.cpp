//
//  socket.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 4/15/13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include "socket.h"
#include "async_service.h"

using namespace muscle;
using namespace std;

namespace muscle {

socket::socket(endpoint& ep, async_service *service) : address(ep), hasAddress(true), server(service)
{
    if (!address.isValid())
        throw muscle_exception("endpoint " + address.str() + " cannot be resolved");
}

socket::socket(async_service *service) : address("", 0), hasAddress(false), server(service) {}

socket::socket() : address("", 0), hasAddress(false), server(NULL) {}

socket::socket(const socket& other) : address(other.address), server(other.server), hasAddress(other.hasAddress) {}

const endpoint& socket::getAddress() const
{
    return address;
}

async_service *socket::getServer() const
{
    return server;
}

string socket::str() const
{
    stringstream ss;
    ss << "socket[" << address << "]";
    return ss.str();
}
string socket::str()
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

size_t SocketFactory::async_connect(int user_flag, muscle::endpoint &ep, muscle::socket_opts *opts, muscle::async_acceptlistener *accept)
{
    return service->connect(user_flag, this, ep, opts, accept);
}

}


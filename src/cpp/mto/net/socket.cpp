//
//  socket.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 4/15/13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include "socket.h"
#include "async_service.h"

using namespace std;

namespace muscle {
    socket::socket(endpoint& ep, async_service *service) : address(ep), hasAddress(true), server(service)
    {
        if (!address.isValid())
            throw muscle_exception("endpoint " + address.str() + " can not be resolved");
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
    
    std::string socket::str() const
    {
        return "socket[" + address.str() + "]";
    }
    std::string socket::str()
    {
        return "socket[" + address.str() + "]";
    }

    /** CLIENT SIDE **/
    void ClientSocket::async_cancel()
    {
        if (server)
            server->erase(this);
    }
    
    /** SERVER SIDE **/
    
    ServerSocket::ServerSocket(const socket_opts& opts)
    {
        listen(opts.max_connections);
    }
    
    void ServerSocket::listen(int max_connections)
    {}
    
    void ServerSocket::async_cancel()
    {
        if (server)
            server->erase(this);
    }
    
    size_t SocketFactory::async_connect(int user_flag, muscle::endpoint &ep, muscle::socket_opts *opts, muscle::async_acceptlistener *accept)
    {
        return service->connect(user_flag, this, ep, opts, accept);
    }
}

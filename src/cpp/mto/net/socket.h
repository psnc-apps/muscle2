//
//  socket.h
//  CMuscle
//
//  Created by Joris Borgdorff on 4/15/13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#ifndef __CMuscle__socket__
#define __CMuscle__socket__

#ifndef MUSCLE_SOCKET_TIMEOUT
#define MUSCLE_SOCKET_TIMEOUT (muscle::time(10,0))
#endif

#define MUSCLE_SOCKET_NONE 0
#define MUSCLE_SOCKET_R 1
#define MUSCLE_SOCKET_W 2
#define MUSCLE_SOCKET_RW 3
#define MUSCLE_SOCKET_ERR 4

#define PLUG_CORK 1
#define UNPLUG_CORK 2

#include "endpoint.h"
#include "async_description.h"
#include "../util/time.h"

namespace muscle {

class async_service;
    
struct socket_opts
{
    int keep_alive;
    int blocking_connect;
    int max_connections;
    ssize_t send_buffer_size;
    ssize_t recv_buffer_size;
    socket_opts() : keep_alive(-1), blocking_connect(-1), max_connections(-1), send_buffer_size(-1), recv_buffer_size(-1) {}
    socket_opts(int max_connections) : keep_alive(-1), blocking_connect(-1), max_connections(max_connections), send_buffer_size(-1), recv_buffer_size(-1) {}
    socket_opts(ssize_t r_bufsize, ssize_t s_bufsize) : keep_alive(-1), blocking_connect(-1), max_connections(-1), send_buffer_size(s_bufsize), recv_buffer_size(r_bufsize) {}
};

class ServerSocket;

class socket
{
public:
    virtual ~socket() {}

    virtual std::string str() const;
    virtual std::string str();
    const endpoint& getAddress() const;
    const bool hasAddress;
    virtual int getWriteSock() const { return sockfd; }
    virtual int getReadSock() const { return sockfd; }
    virtual bool operator < (const socket & s1) const
    { return getReadSock() < s1.getReadSock(); }
    virtual bool operator == (const socket & s1) const
    { return getReadSock() == s1.getReadSock(); }
    async_service *getServer() const;
    virtual void async_cancel() = 0;
	
    // To accomodate for pipes used in mpsocket
	virtual bool selectWriteFdIsReadable() const { return false; }
protected:
    socket(endpoint& ep, async_service *service);
    socket(async_service *service);
    socket(const socket& other);
    socket();
    
    int sockfd;
    endpoint address;
    async_service *server;
}; // end class socket

class ClientSocket : virtual public socket
{
public:    
    virtual int hasError() { return 0; }

	virtual void setCork(bool) {};
	virtual void setDelay(bool) {};
	
    // Light-weight, non-blocking
    virtual ssize_t send (const void* s, size_t size) = 0;
    virtual ssize_t recv (void* s, size_t size) = 0;

    // asynchronous, light-weight, non-blocking
    virtual ssize_t async_send (int user_flag, const void* s, size_t size, async_sendlistener *send, int opts);
    virtual ssize_t async_recv (int user_flag, void* s, size_t size, async_recvlistener *recv);

    virtual void async_cancel();
    
    virtual ~ClientSocket() { async_cancel(); }
};

class ServerSocket : virtual public socket
{
public:
    virtual ClientSocket *accept(const socket_opts& opts) = 0;
    virtual size_t async_accept(int user_flag, async_acceptlistener *accept, socket_opts *opts) = 0;
    virtual void async_cancel();
    virtual ~ServerSocket() { async_cancel(); }
protected:
    ServerSocket(const socket_opts& opts);
    virtual void listen(int max_connections);
};

class SocketFactory
{
protected:
    async_service *service;
public:
    SocketFactory(async_service *service) : service(service) {}
    virtual ~SocketFactory() {}
    size_t async_connect(int user_flag, muscle::endpoint &ep, muscle::socket_opts *opts, muscle::async_acceptlistener *accept);
    virtual ClientSocket *connect(endpoint& ep, const socket_opts& opts) = 0;
    virtual ServerSocket *listen(endpoint& ep, const socket_opts& opts) = 0;
};

} // end namespace muscle

#endif /* defined(__CMuscle__socket__) */

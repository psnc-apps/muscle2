//
//  msocket.h
//  CMuscle
//
//  Created by Joris Borgdorff on 4/15/13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#ifndef __CMuscle__msocket__
#define __CMuscle__msocket__

#define MUSCLE_SOCKET_NONE 0
#define MUSCLE_SOCKET_R 1
#define MUSCLE_SOCKET_W 2
#define MUSCLE_SOCKET_RW 3
#define MUSCLE_SOCKET_ERR 4

#include "endpoint.h"
#include "async_description.h"

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

class msocket
{
public:
	virtual ~msocket() {}

	virtual void setBlocking(bool) = 0;
    virtual std::string str() const;
    virtual std::string str();
    const endpoint& getAddress() const;
    const bool hasAddress;
    virtual int getWriteSock() const { return sockfd; }
    virtual int getReadSock() const { return sockfd; }
    virtual bool operator < (const msocket & s1) const
    { return getReadSock() < s1.getReadSock(); }
    virtual bool operator == (const msocket & s1) const
    { return getReadSock() == s1.getReadSock(); }
    async_service *getServer() const;
    virtual void async_cancel() = 0;
	
    // To accomodate for pipes used in mpsocket
	virtual bool selectWriteFdIsReadable() const { return false; }
protected:
    msocket(endpoint& ep, async_service *service);
    msocket(async_service *service);
    msocket(const msocket& other);
    msocket();
	
    int sockfd;
    endpoint address;
    async_service *server;
}; // end class socket

class ClientSocket : virtual public msocket
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
};

class ServerSocket : virtual public msocket
{
public:
    virtual ClientSocket *accept(const socket_opts& opts) = 0;
    virtual size_t async_accept(int user_flag, async_acceptlistener *accept, socket_opts *opts) = 0;
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

#endif /* defined(__CMuscle__msocket__) */

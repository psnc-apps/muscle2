//
//  mpsocket.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 04-06-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include "mpsocket.h"
#include "async_service.h"
#include "../mpwide/MPWide.h"

#include <strings.h>
#include <cstring>
#include <set>
#include <sys/types.h>
#include <sys/socket.h>
#include <netdb.h>
#include <errno.h>
#include <fcntl.h>
#include <cassert>
#include <signal.h>
#include <sys/select.h>
#include <string>
#include <pthread.h>

#ifndef MSG_NOSIGNAL
#ifdef SO_NOSIGPIPE
#define MSG_NOSIGNAL SO_NOSIGPIPE
#else
#define MSG_NOSIGNAL msg_nosignal_is_not_defined
#endif
#endif

using namespace std;

namespace muscle {    
    mpsocket::mpsocket() : socket((async_service *)0) {}
    mpsocket::mpsocket(int pathid) : socket((async_service *)0), pathid(pathid) {}
    
    void mpsocket::setWin(const ssize_t size)
    {
        assert(size > 0);
        MPW_setPathWin(pathid, (int)size);
    }
    
    void *mpsocket_thread::run()
    {
        if (send)
            MPW_Send(data, sz, sock->pathid);
        else
            MPW_Recv(data, sz, sock->pathid);
        
        return data;
    }
    
    /** CLIENT SIDE **/
    MPClientSocket::MPClientSocket(const ServerSocket& parent, int pathid, const socket_opts& opts) : socket(parent), mpsocket(pathid), commThread(0), connectThread(0)
    {}
    
    MPClientSocket::MPClientSocket(endpoint& ep, async_service *service, const socket_opts& opts) : socket(ep, service), commThread(0), connectThread(0)
    {
        if (opts.blocking_connect)
        {
            pathid = MPW_CreatePath(address.getHost(), address.port, opts.max_connections);
            if (opts.recv_buffer_size != -1)
                setWin(opts.recv_buffer_size);
            else if (opts.send_buffer_size != -1)
                setWin(opts.send_buffer_size);
        }
        else
        {
            connectThread = new mpsocket_connect_thread(ep, opts);
        }        
    }
    
    MPClientSocket::~MPClientSocket()
    {
        if (commThread)
            delete commThread;
        
        MPW_DestroyPath(pathid);
    }
    
    ssize_t MPClientSocket::send(const void * const s, const size_t size) const
    {
        MPW_Send((char *)s, size, pathid);
        return size;
    }
    
    
    ssize_t MPClientSocket::recv (void * const s, const size_t size) const
    {
        MPW_Recv((char *)s, size, pathid);
        return size;
    }
    
    ssize_t MPClientSocket::irecv(void* s, size_t size)
    {
        return runInThread(false, s, size);
    }
    
    ssize_t MPClientSocket::isend(const void* s, size_t size)
    {
        return runInThread(true, (void *)s, size);
    }
    
    ssize_t MPClientSocket::runInThread(bool send, void *s, size_t sz)
    {
        if (commThread)
        {
            // wait until the previous message is sent/received
            commThread->getResult();
            delete commThread;
        }
        
        commThread = new mpsocket_thread(send, s, sz, this);
        return sz;
    }
    
    ssize_t MPClientSocket::async_recv(int user_flag, void* s, size_t size, async_recvlistener *receiver)
    {
        return server->receive(user_flag, this, s, size, receiver);
    }
    
    ssize_t MPClientSocket::async_send(int user_flag, const void* s, size_t size, async_sendlistener *send)
    {
        return server->send(user_flag, this, s, size, send);
    }
    
    /** SERVER SIDE **/
    MPServerSocket::MPServerSocket(endpoint& ep, async_service *service, const socket_opts& opts) : socket(ep, service), ServerSocket(opts)
    {
        max_connections = opts.max_connections;
        listener = new mpsocket_connect_thread(endpoint("0.0.0.0", address.port), opts);
    }
    
    ClientSocket *MPServerSocket::accept(const socket_opts &opts)
    {
        int *res = (int *)listener->getResult();
        ClientSocket *sock = new MPClientSocket(*this, *res, opts);
        delete res;
        delete listener;
        listener = new mpsocket_connect_thread(endpoint("0.0.0.0", address.port), opts);
        return sock;
    }

    size_t MPServerSocket::async_accept(int user_flag, async_acceptlistener *accept)
    {
        socket_opts *opts = new socket_opts(max_connections);
        // TODO check if we need to pass options
        return server->listen(user_flag, this, opts, accept);
    }
    
    void *mpsocket_connect_thread::run()
    {
        int *res = new int;
        *res = MPW_CreatePath(ep.getHost(), ep.port, opts.max_connections);
        return res;
    }
}

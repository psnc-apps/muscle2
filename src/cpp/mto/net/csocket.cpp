//
//  csocket.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 17-04-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include "csocket.h"
#include "async_service.h"

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

#ifndef MSG_NOSIGNAL
#ifdef SO_NOSIGPIPE
#define MSG_NOSIGNAL SO_NOSIGPIPE
#else
#define MSG_NOSIGNAL msg_nosignal_is_not_defined
#endif
#endif

using namespace std;

namespace muscle {
    csocket::csocket() : socket((async_service *)0) {}
    csocket::csocket(int sockfd) : socket((async_service *)0) { this->sockfd = sockfd; }
    
    void csocket::setBlocking (const bool blocking)
    {
        int opts = fcntl(sockfd, F_GETFL);
        
        if (opts < 0)
            throw muscle_exception("Can not set the blocking status", errno);
        
        if (blocking == ((opts|O_NONBLOCK) == O_NONBLOCK))
        {
            if (blocking)
                opts &= ~O_NONBLOCK;
            else
                opts |= O_NONBLOCK;
            
            fcntl(sockfd, F_SETFL, opts);
        }
    }
    
    void csocket::setOpts(const socket_opts &opts)
    {
        if (opts.recv_buffer_size != -1)
            setsockopt(sockfd, SOL_SOCKET, SO_RCVBUF, &opts.recv_buffer_size, sizeof(opts.recv_buffer_size));
        if (opts.send_buffer_size != -1)
            setsockopt(sockfd, SOL_SOCKET, SO_SNDBUF, &opts.send_buffer_size, sizeof(opts.send_buffer_size));
        
        bool keep_alive = opts.keep_alive == -1 ? 1 : opts.keep_alive;
        setsockopt(sockfd, SOL_SOCKET, SO_KEEPALIVE, &keep_alive, sizeof(bool));
    }
    
    void csocket::setWin(int size)
    {
        assert(size > 0);
        setsockopt(sockfd, SOL_SOCKET, SO_SNDBUF, (char *) &size, sizeof(int));
        setsockopt(sockfd, SOL_SOCKET, SO_RCVBUF, (char *) &size, sizeof(int));
    }
    
    void csocket::create()
    {
        int proto = address.isIPv6() ? PF_INET6 : PF_INET;
        
        sockfd = ::socket(proto, SOCK_STREAM, 0);
        if (sockfd < 0) throw muscle_exception("can not create socket", errno);
        bool reuse = true;
        setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, &reuse, sizeof(bool));
    }
    
    int csocket::select(int mask) const
    { return select(mask, MUSCLE_SOCKET_TIMEOUT); }
    
    int csocket::select(int mask, time time) const
    {
        /* args: FD_SETSIZE,writeset,readset,out-of-band sent, timeout*/
        int access = 0;
        
        fd_set rsock, wsock, esock;
        fd_set *prsock = NULL, *pwsock = NULL, *pesock = NULL;
        if((mask&MUSCLE_SOCKET_R) == MUSCLE_SOCKET_R)
        {
            prsock = &rsock;
            FD_ZERO(prsock);
            FD_SET(sockfd,prsock);
        }
        if((mask&MUSCLE_SOCKET_W) == MUSCLE_SOCKET_W)
        {
            pwsock = &wsock;
            FD_ZERO(pwsock);
            FD_SET(sockfd,pwsock);
        }
        if((mask&MUSCLE_SOCKET_ERR) == MUSCLE_SOCKET_ERR)
        {
            pesock = &esock;
            FD_ZERO(pesock);
            FD_SET(sockfd,pesock);
        }
        
        struct timeval timeout = time.timeval();
        
        int res = ::select(sockfd+1, prsock, pwsock, pesock, &timeout);
        
        if (res == 0) return MUSCLE_SOCKET_NONE;
        if (res < 0) throw muscle_exception("Could not select socket to " + address.str());
        
        if((mask&MUSCLE_SOCKET_R) == MUSCLE_SOCKET_R && FD_ISSET(sockfd,prsock))
            access |= MUSCLE_SOCKET_R;
        if((mask&MUSCLE_SOCKET_W) == MUSCLE_SOCKET_W && FD_ISSET(sockfd,pwsock))
            access |= MUSCLE_SOCKET_W;
        if((mask&MUSCLE_SOCKET_ERR) == MUSCLE_SOCKET_ERR && FD_ISSET(sockfd,pesock))
            access |= MUSCLE_SOCKET_ERR;
        
        return access;
    }
    
    /** CLIENT SIDE **/
    
    CClientSocket::CClientSocket(endpoint& ep, async_service *service) : socket(ep, service)
    {
        create();
        socket_opts opts;
        connect(true);
    }

    CClientSocket::CClientSocket(const ServerSocket& parent, int sockfd, const socket_opts& opts) : socket(parent), csocket(sockfd)
    {
        setOpts(opts);
    }
        
    CClientSocket::CClientSocket(endpoint& ep, async_service *service, const socket_opts& opts) : csocket(sockfd), socket(ep, service)
    {
        create();
        setOpts(opts);
        connect(opts.blocking_connect != 0);
    }
    
    void CClientSocket::connect(bool blocking)
    {
        setBlocking(blocking);
        
        struct sockaddr saddr;
        address.getSockAddr(saddr);
        int res = ::connect(sockfd, &saddr, sizeof(struct sockaddr));
        if (res < 0 && (blocking || errno != EINPROGRESS))
            throw muscle_exception("can not connect to " + address.str(), errno);
        
        if (blocking)
            setBlocking(false);
    }
    
    
    ssize_t CClientSocket::send(const void * const s, const size_t size) const
    {
        const unsigned char *s_ptr = (const unsigned char *)s;
        const unsigned char * const end_ptr = s_ptr + size;
        
        while (s_ptr != end_ptr) {
            const int mask = select(MUSCLE_SOCKET_W|MUSCLE_SOCKET_ERR);

            if ((mask&MUSCLE_SOCKET_ERR) == MUSCLE_SOCKET_ERR) return -1;
            if ((mask&MUSCLE_SOCKET_W)   != MUSCLE_SOCKET_W)   break;

            const ssize_t status = ::send(sockfd, s_ptr, end_ptr - s_ptr, MSG_NOSIGNAL);
            if (status == -1) return -1;

            s_ptr += status;
        }
        
        return s_ptr - (const unsigned char *)s;
    }
    
    
    ssize_t CClientSocket::recv (void * const s, const size_t size) const
    {
        unsigned char *s_ptr = (unsigned char *)s;
        const unsigned char * const end_ptr = s_ptr + size;
        
        for (int count = 0; s_ptr != end_ptr && count < 100; count++)
        {
            const int mask = select(MUSCLE_SOCKET_R|MUSCLE_SOCKET_ERR);

            if ((mask&MUSCLE_SOCKET_ERR) == MUSCLE_SOCKET_ERR) return -1;
            if ((mask&MUSCLE_SOCKET_R)   != MUSCLE_SOCKET_R)   break;

            const ssize_t status = ::recv(sockfd, s_ptr, end_ptr - s_ptr, 0);
            if (status == -1) return -1;

            s_ptr += status;
        }
        
        return s_ptr - (unsigned char *)s;
    }
    
    ssize_t CClientSocket::irecv(void* s, size_t size)
    {
        ssize_t ret = ::recv(sockfd, s, size, 0);
        // 0 can be considered an error code, meaning connection closed
        return (ret == 0 ? -1 : ret);
    }
    
    ssize_t CClientSocket::isend(const void* s, size_t size)
    {
        return ::send(sockfd, s, size, MSG_NOSIGNAL);
    }
    
    ssize_t CClientSocket::async_recv(int user_flag, void* s, size_t size, async_recvlistener *receiver)
    {
        return server->receive(user_flag, this, s, size, receiver);
    }
    
    ssize_t CClientSocket::async_send(int user_flag, const void* s, size_t size, async_sendlistener *send) 
    {
        return server->send(user_flag, this, s, size, send);
    }
    
    int CClientSocket::hasError()
    {
        int so_error;
        socklen_t slen = sizeof so_error;
        getsockopt(sockfd, SOL_SOCKET, SO_ERROR, &so_error, &slen);
        return so_error;
    }
    
    /** SERVER SIDE **/
    CServerSocket::CServerSocket(endpoint& ep, async_service *service, const socket_opts& opts) : socket(ep, service), ServerSocket(opts)
    {
        create();
        setOpts(opts);
        init();
        listen(opts.max_connections);
    }
    
    ClientSocket *CServerSocket::accept(const socket_opts &opts)
    {
        struct sockaddr addr;
        address.getSockAddr(addr);
        socklen_t socklen = sizeof(struct sockaddr);
        int childfd = ::accept(sockfd, &addr, &socklen);
        
        if (childfd < 0)
            throw muscle_exception("Failed to accept socket", errno);
        
        return new CClientSocket(*this, childfd, opts);
    }
    
    void CServerSocket::listen(int max_connections)
    {
        int res = ::listen(sockfd, max_connections);
        if (res == -1) throw muscle_exception("failed to listen");
    }
    
    void CServerSocket::init()
    {
        struct sockaddr saddr;
        address.getSockAddr(saddr);
        int res = ::bind(sockfd, &saddr, sizeof(struct sockaddr));
        if (res < 0) throw muscle_exception("can not bind to " + address.str(), errno);
    }
    
    size_t CServerSocket::async_accept(int user_flag, async_acceptlistener *accept)
    {
        socket_opts *opts = new socket_opts;
        // TODO check if we need to pass options
        return server->listen(user_flag, this, opts, accept);
    }
    
    ClientSocket *CSocketFactory::connect(muscle::endpoint &ep, const muscle::socket_opts &opts)
    {
        return new CClientSocket(ep, service, opts);
    }

    ServerSocket *CSocketFactory::listen(muscle::endpoint &ep, const muscle::socket_opts &opts)
    {
        return new CServerSocket(ep, service, opts);
    }
}

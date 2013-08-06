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
#include <netinet/in.h>
#include <netdb.h>
#include <errno.h>
#include <fcntl.h>
#include <cassert>
#include <signal.h>
#include <sys/select.h>
#include <string>
#include <netinet/tcp.h>

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
        
        if (blocking == ((opts&O_NONBLOCK) == O_NONBLOCK))
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
        setsockopt(sockfd, SOL_SOCKET, SO_KEEPALIVE, &keep_alive, sizeof(keep_alive));
    }
    
    void csocket::setWin(int size)
    {
        assert(size > 0);
        setsockopt(sockfd, SOL_SOCKET, SO_SNDBUF, (char *) &size, sizeof(size));
        setsockopt(sockfd, SOL_SOCKET, SO_RCVBUF, (char *) &size, sizeof(size));
    }
    
    void csocket::create()
    {
        int proto = address.isIPv6() ? PF_INET6 : PF_INET;
        
        sockfd = ::socket(proto, SOCK_STREAM, 0);
        if (sockfd < 0) throw muscle_exception("can not create socket", errno);
        int set = 1;
        setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, &set, sizeof(set));
		setsockopt(sockfd, SOL_SOCKET, MSG_NOSIGNAL, &set, sizeof(set));
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
    CClientSocket::CClientSocket(const ServerSocket& parent, int sockfd, const socket_opts& opts) : socket(parent), csocket(sockfd), has_delay(true), has_cork(false)
    {
        setOpts(opts);
    }
        
    CClientSocket::CClientSocket(endpoint& ep, async_service *service, const socket_opts& opts) : csocket(sockfd), socket(ep, service), has_delay(true), has_cork(false)
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
        if (res < 0 && (blocking || errno != EINPROGRESS)) {
			sockfd = -1;
            throw muscle_exception("can not connect to " + address.str(), errno);
		}
        
        if (blocking)
            setBlocking(false);
		
		setDelay(false);
    }
    
	void CClientSocket::setDelay(const bool delay)
	{
#ifdef TCP_NODELAY
		if (delay != has_delay) {
			int nodelay = delay ? 0 : 1;
		
			setsockopt(sockfd, IPPROTO_TCP, TCP_NODELAY, &nodelay, sizeof(nodelay));

			has_delay = delay;
		}
#endif
	}
	
	void CClientSocket::setCork(const bool plug)
	{
#ifdef TCP_CORK
		if (has_cork != plug) {
			setsockopt(sockfd, IPPROTO_TCP, TCP_CORK, &plug, sizeof(plug));
			has_cork = plug;
		}
#endif
	}
	
    ssize_t CClientSocket::recv(void* s, size_t size)
    {
        const ssize_t ret = ::recv(sockfd, s, size, 0);
        // 0 can be considered an error code, meaning connection closed
        return (ret == 0 ? -1 : ret);
    }
    
    ssize_t CClientSocket::send(const void* s, size_t size)
    {
        return ::send(sockfd, s, size, MSG_NOSIGNAL);
    }
        
    int CClientSocket::hasError()
    {
        int so_error;
        socklen_t slen = sizeof so_error;
        getsockopt(sockfd, SOL_SOCKET, SO_ERROR, &so_error, &slen);
		
		if (!so_error)
			setDelay(false);
		
        return so_error;
    }
	
	void CClientSocket::async_cancel()
	{
		if (server != NULL)
			server->erase_socket(sockfd, sockfd, -1);
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
    
    size_t CServerSocket::async_accept(int user_flag, async_acceptlistener *accept, socket_opts *opts)
    {
        return server->listen(user_flag, this, opts, accept);
    }
	
	void CServerSocket::async_cancel()
	{
		if (server != NULL)
			server->erase_listen(sockfd);
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

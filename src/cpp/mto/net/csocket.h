//
//  csocket.h
//  CMuscle
//
//  Created by Joris Borgdorff on 17-04-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#ifndef __CMuscle__csocket__
#define __CMuscle__csocket__

#include "socket.h"

#include <string>
#include <unistd.h>
#include <sys/socket.h>

namespace muscle {
    
    class CServerSocket;
    
    class csocket : virtual public socket
    {
    public:
        virtual ~csocket() { shutdown(sockfd, SHUT_RDWR); close(sockfd); }
        
		virtual void setBlocking(const bool);

        // Check if the socket is readable / writable. Timeout is MUSCLE_SOCKET_TIMEOUT seconds.
        // Override MUSCLE_SOCKET_TIMEOUT to choose a different number of seconds
        virtual int select(int mask) const;
        virtual int select(int mask, time timeout) const;

        virtual bool operator < (const csocket & s1) const { return sockfd < s1.sockfd; }
        virtual bool operator == (const csocket & s1) const { return sockfd == s1.sockfd; }
    protected:
        csocket();
        csocket(int sockfd);
        
        void create();
            
        virtual void setOpts(const socket_opts& opts);
        
        void setWin(int size);
    }; // end class socket
    
    class CClientSocket : public ClientSocket, public csocket
    {
    public:
//        CClientSocket(const ServerSocket& parent);
        CClientSocket(const ServerSocket& parent, int sockfd, const socket_opts& opts);
        CClientSocket(endpoint& ep, async_service *service);
        CClientSocket(endpoint& ep, async_service *service, const socket_opts& opts);
        
        // Data Transmission
        virtual ssize_t send (const void* s, size_t size) const;
        virtual ssize_t recv (void* s, size_t size) const;
        
        // Light-weight, non-blocking
        virtual ssize_t isend (const void* s, size_t size);
        virtual ssize_t irecv (void* s, size_t size);
        // asynchronous, light-weight, non-blocking
        virtual ssize_t async_send (int user_flag, const void* s, size_t size, async_sendlistener *send);
        virtual ssize_t async_recv(int user_flag, void* s, size_t size, async_recvlistener *receiver);
        virtual int hasError();
    protected:
        virtual void connect(bool blocking);
    // Disallowed - is problematic for destructor
    private:
        CClientSocket(const CClientSocket& other) {}
    };
    
    class CServerSocket : public ServerSocket, public csocket
    {
    public:
        CServerSocket(endpoint& ep, async_service *service, const socket_opts& opts);
        
        virtual size_t async_accept(int user_flag, async_acceptlistener *accept, socket_opts *opts);
        virtual ClientSocket *accept(const socket_opts& opts);
    protected:
        virtual void init();
        virtual void listen(int max_connections);
    };
    
    class CSocketFactory : public SocketFactory
    {
    public:
        CSocketFactory(async_service *service) : SocketFactory(service) {}
        virtual ClientSocket *connect(endpoint& ep, const socket_opts& opts);
        virtual ServerSocket *listen(endpoint& ep, const socket_opts& opts);
    };
} // end namespace muscle

#endif /* defined(__CMuscle__csocket__) */

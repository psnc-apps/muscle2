//
//  mpsocket.h
//  CMuscle
//
//  Created by Joris Borgdorff on 04-06-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#ifndef __CMuscle__mpsocket__
#define __CMuscle__mpsocket__
#include "socket.h"
#include "../util/thread.h"
#include "../util/rwmutex.h"
#include "../util/mutex.h"

#include <string>
#include <unistd.h>
#include <pthread.h>
#include <sys/socket.h>
#include <stack>

namespace muscle {
	
    class MPServerSocket;
    class MPClientSocket;
    class mpsocket;
    
    //// Threads for asynchronous connections ////
    
    class mpsocket_thread : public thread
    {
    private:
        const size_t sz;
        const MPClientSocket *sock;
        const bool send;
    public:
        mpsocket_thread(bool send, void *data, size_t sz, const MPClientSocket *sock) : send(send), data((char *)data), sz(sz), sock(sock) { start(); }
        
        virtual void *run();
		char * const data;
    };
    
    class mpsocket_connect_thread : public thread
    {
    private:
        const endpoint ep;
        const socket_opts opts;
        const mpsocket *sock;
        const bool asServer;
    public:
        mpsocket_connect_thread(endpoint ep, const socket_opts& opts, const mpsocket *sock, bool asServer) : ep(ep), opts(opts), sock(sock), asServer(asServer) { start(); }
        virtual void *run();
    };
    
    class mpsocket_destroy_thread : public thread
    {
        mutex mut;
        std::stack<int> paths;
    public:
        mpsocket_destroy_thread() { start(); }
        virtual ~mpsocket_destroy_thread() {}
        
        void destroyPath(int pathid);
        virtual void *run();
        virtual void cancel();
    };
    
    ///// Actual socket implementation ////
    
    class mpsocket : virtual public socket
    {
    public:
        virtual ~mpsocket();
        virtual void setReadReady() const;
        virtual void unsetReadReady() const;
        virtual bool isReadReady() const;
		virtual void setWriteReady() const;
		virtual void unsetWriteReady() const;
		virtual bool isWriteReady() const;
		virtual int getWriteSock() const;
		
        static rwmutex path_mutex;
        static mutex create_path_mutex;
		
		virtual bool selectWriteFdIsReadable() const { return true; }
    protected:
        mpsocket();
        int writableReadFd, readableWriteFd, writableWriteFd;
    }; // end class socket

    class MPClientSocket : public ClientSocket, public mpsocket
    {
    public:
        int pathid;

        MPClientSocket(const ServerSocket& parent, int pathid, const socket_opts& opts);
        MPClientSocket(endpoint& ep, async_service *service, const socket_opts& opts);
        virtual ~MPClientSocket();
        
        // Data Transmission
        virtual ssize_t send (const void* s, size_t size) const;
        virtual ssize_t recv (void* s, size_t size) const;
        
        // Light-weight, non-blocking
        virtual ssize_t isend (const void* s, size_t size);
        virtual ssize_t irecv (void* s, size_t size);
                
        // asynchronous, light-weight, non-blocking
        virtual ssize_t async_send (int user_flag, const void* s, size_t size, async_sendlistener *send);
        virtual ssize_t async_recv(int user_flag, void* s, size_t size, async_recvlistener *receiver);
        
        virtual bool operator < (const MPClientSocket & s1) const { return pathid < s1.pathid; }
        virtual bool operator == (const MPClientSocket & s1) const { return pathid == s1.pathid; }

        virtual int hasError();
        void setWin(ssize_t size);
	private:
        // Disallowed - is problematic for destructor
        MPClientSocket(const MPClientSocket& other) {}
        
        ssize_t runInThread(bool send, void *s, size_t sz, mpsocket_thread *&last_thread);
        bool isConnecting();

        mpsocket_thread *sendThread, *recvThread;
        mpsocket_connect_thread *connectThread;
        void *last_send, *last_recv;
    };
    
    class MPServerSocket : public ServerSocket, public mpsocket
    {
    public:
        MPServerSocket(endpoint& ep, async_service *service, const socket_opts& opts);
        virtual ~MPServerSocket() { delete listener; }
        
        virtual ClientSocket *accept(const socket_opts& opts);
        virtual size_t async_accept(int user_flag, async_acceptlistener *accept);
    protected:
        int max_connections;
        mpsocket_connect_thread *listener;
    };

    class MPSocketFactory : public SocketFactory
    {
    private:
        static int num_mpsocket_factories;
    public:
        MPSocketFactory(async_service *service);
        virtual ~MPSocketFactory();
        virtual ClientSocket *connect(endpoint& ep, const socket_opts& opts);
        virtual ServerSocket *listen(endpoint& ep, const socket_opts& opts);

        static mpsocket_destroy_thread *destroyer;
    };
} // end namespace muscle

#endif /* defined(__CMuscle__mpsocket__) */

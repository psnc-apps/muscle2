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

#include <string>
#include <unistd.h>
#include <pthread.h>
#include <sys/socket.h>

namespace muscle {
    
    class MPServerSocket;
    class MPClientSocket;
    
    //// Threads for asynchronous connections ////
    
    class mpsocket_thread : public thread
    {
    private:
        char *data;
        size_t sz;
        const MPClientSocket *sock;
        bool send;
    public:
        mpsocket_thread(bool send, void *data, size_t sz, const MPClientSocket *sock) : send(send), data((char *)data), sz(sz), sock(sock) {}
        
        virtual void *run();
    };
    
    class mpsocket_connect_thread : public thread
    {
    private:
        endpoint ep;
        socket_opts opts;
    public:
        mpsocket_connect_thread(endpoint ep, const socket_opts& opts) : ep(ep), opts(opts) {}
        virtual void *run();
    };
    
    
    ///// Actual socket implementation ////
    
    class mpsocket : virtual public socket
    {
    public:
        int pathid;
        
        virtual int getSock() const { return pathid; }
        virtual bool operator < (const mpsocket & s1) const { return pathid < s1.pathid; }
        virtual bool operator == (const mpsocket & s1) const { return pathid == s1.pathid; }
    protected:
        mpsocket();
        mpsocket(int pathid);
        
        void setWin(ssize_t size);
    }; // end class socket

    class MPClientSocket : public ClientSocket, public mpsocket
    {
    public:
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
        
        virtual bool isBusy()
        {
            if (commThread && !commThread->isDone()) return true;
            if (connectThread)
            {
                if (connectThread->isDone()) {
                    int *res = (int *)connectThread->getResult();
                    pathid = *res;
                    delete res;
                    delete connectThread;
                    connectThread = NULL;
                }
                else return true;
            }
            return false;
        }
    private:
        // Disallowed - is problematic for destructor
        MPClientSocket(const MPClientSocket& other) {}
        
        ssize_t runInThread(bool send, void *s, size_t sz);

        mpsocket_thread *commThread;
        mpsocket_connect_thread *connectThread;
    };
    
    class MPServerSocket : public ServerSocket, public mpsocket
    {
    public:
        MPServerSocket(endpoint& ep, async_service *service, const socket_opts& opts);
        virtual ~MPServerSocket() { delete listener; }
        
        virtual ClientSocket *accept(const socket_opts& opts);
        virtual size_t async_accept(int user_flag, async_acceptlistener *accept);

        virtual bool isBusy() const { return !listener->isDone(); }
    protected:
        int max_connections;
        mpsocket_connect_thread *listener;
    };
} // end namespace muscle

#endif /* defined(__CMuscle__mpsocket__) */

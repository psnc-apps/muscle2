//
//  meta_socket.h
//  CMuscle
//
//  Created by Joris Borgdorff on 04-05-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#ifndef __CMuscle__meta_socket__
#define __CMuscle__meta_socket__

#include "socket.h"
#include "endpoint.h"
#include "async_service.h"

namespace muscle {
    class meta_socket_protocol
    {
    public:
        virtual ~meta_socket_protocol() {}
        virtual void initiateSocket(const net::ClientSocket *sock) = 0;
        virtual bool socketFailed(int times) = 0;
    };
    
    class meta_socket : public net::ClientSocket
    {
    protected:
        ClientSocket *sock;
        bool locked;
        const util::duration timeout;

        virtual void recreate() = 0;
        virtual void invalidate();
    public:
        meta_socket(net::endpoint& ep, net::async_service *service, util::duration& timeout);
        virtual ~meta_socket();
        
        /* ====== Reconnect issues ====== */
        
        void recreateSocket(muscle::net::async_function *func);
        
        std::vector< muscle::net::async_function * > recreateSocketPending;
        
        void recreateSocketFired();
        void recreateSocketSentHello(char * data);
        void recreateSocketReadHello(char * data);
        void recreateSocketTimedOut(size_t timer, int user_flag, void *user_data);
        
        size_t recreateSocketTimer;
        
        /* ====== Iddle connection handler ====== */
        
        size_t iddleTimer;
        //  void iddleTimerFired();
        void iddleSocketClose();
        util::mtime lastOperation;
        void updateLastOperationTimer();
    };
}
#endif /* defined(__CMuscle__meta_socket__) */

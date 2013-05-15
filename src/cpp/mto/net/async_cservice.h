//
//  async_cservice.h
//  CMuscle
//
//  Created by Joris Borgdorff on 18-04-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#ifndef __CMuscle__async_cservice__
#define __CMuscle__async_cservice__

#include "async_service.h"

#ifndef MUSCLE_ASYNC_CSERVICE_TIMEOUT
#define MUSCLE_ASYNC_CSERVICE_TIMEOUT 10
#endif

namespace muscle {
    class async_cservice : public async_service
    {
    public:
        virtual size_t connect(int user_flag, muscle::endpoint& ep, socket_opts *opts, async_acceptlistener* accept);
        virtual void erase_connect(size_t);
    protected:
        virtual int select(const ClientSocket **sender, const ClientSocket **receiver, const ServerSocket **listener, const ClientSocket **connect, duration& timeout) const;
        virtual void run_accept(const ServerSocket *sock, bool hasErr);
        virtual void run_connect(const ClientSocket *connect, bool hasErr);
    };
}

#endif /* defined(__CMuscle__async_cservice__) */

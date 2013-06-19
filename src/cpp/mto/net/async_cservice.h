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
    protected:
        virtual int select(ClientSocket **sender, ClientSocket **receiver, ServerSocket **listener, ClientSocket **connect, duration& timeout) const;
    };
}

#endif /* defined(__CMuscle__async_cservice__) */

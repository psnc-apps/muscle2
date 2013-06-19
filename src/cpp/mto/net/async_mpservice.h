//
//  async_mpservice.h
//  CMuscle
//
//  Created by Joris Borgdorff on 19-06-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#ifndef __CMuscle__async_mpservice__
#define __CMuscle__async_mpservice__

#include "async_service.h"

namespace muscle {
    class async_mpservice : public async_service
    {
    public:
        virtual size_t connect(int user_flag, muscle::endpoint& ep, socket_opts *opts, async_acceptlistener* accept);
    protected:
        virtual int select(ClientSocket **sender, ClientSocket **receiver, ServerSocket **listener, ClientSocket **connect, duration& timeout);
    };
}

#endif /* defined(__CMuscle__async_mpservice__) */

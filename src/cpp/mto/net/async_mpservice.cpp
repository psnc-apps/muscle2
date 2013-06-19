//
//  async_mpservice.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 19-06-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include "async_mpservice.h"
#include "mpsocket.h"
#include <set>

using namespace std;

namespace muscle {

int async_mpservice::select(ClientSocket **sender, ClientSocket **receiver, ServerSocket **listener, ClientSocket **connect, duration& timeout)
{
    if (sendSockets.empty() && recvSockets.empty() && listenSockets.empty()) return 0;
    
    /* args: FD_SETSIZE,writeset,readset,out-of-band sent, timeout*/
    for (set<ClientSocket *>::const_iterator s = sendSockets.begin(); s != sendSockets.end(); s++)
    {
        if (!(*s)->isBusy())
        {
            *sender = *s;
            break;
        }
    }

    for (set<ClientSocket *>::const_iterator s = recvSockets.begin(); s != recvSockets.end(); s++)
    {
        if (!(*s)->isBusy())
        {
            *receiver = *s;
            break;
        }
    }
    
    for (map<ServerSocket *,async_description>::const_iterator s = listenSockets.begin(); s != listenSockets.end(); s++)
    {
        if (!s->first->isBusy())
        {
            *listener = s->first;
            break;
        }
    }
    
    for (map<ClientSocket *,async_description>::const_iterator s = connectSockets.begin(); s != connectSockets.end(); s++)
    {
        if (!s->first->isBusy())
        {
            *connect = s->first;
            break;
        }
    }
    
    return 0;
}

size_t async_mpservice::connect(int user_flag, muscle::endpoint& ep, socket_opts *opts, async_acceptlistener* accept)
{
    if (!accept)
        throw muscle_exception("Accept listener must not be empty");
    
    size_t code = getNextCode();
    async_description desc(code, user_flag, (void *)opts, 0, accept);
    
    bool new_opts = (opts == NULL);
    if (new_opts)
        opts = new socket_opts;
    
    opts->blocking_connect = false;
    
    MPClientSocket *sock = new MPClientSocket(ep, this, *opts);
    
    if (new_opts)
        delete opts;
    
    connectSockets[sock] = desc;
    return code;
}

}

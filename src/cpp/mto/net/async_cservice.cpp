//
//  async_cservice.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 18-04-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include "async_cservice.h"
#include "csocket.h"
#include "../util/exception.hpp"

#include <errno.h>
#include <cstring>
#include <set>

using namespace std;

namespace muscle {
    int async_cservice::select(ClientSocket **sender, ClientSocket **receiver, ServerSocket **listener, ClientSocket **connect, duration& timeout) const
    {
        if (sendSockets.empty() && recvSockets.empty() && listenSockets.empty()) return 0;
        
        /* args: FD_SETSIZE,writeset,readset,out-of-band sent, timeout*/
        fd_set rsock, wsock, esock;
        FD_ZERO(&rsock);
        FD_ZERO(&wsock);
        FD_ZERO(&esock);
        int maxfd = 0, sockfd;
        for (set<ClientSocket *>::const_iterator s = sendSockets.begin(); s != sendSockets.end(); s++) {
            sockfd = (*s)->getSock();
            FD_SET(sockfd,&wsock);
            FD_SET(sockfd,&esock);
            if (sockfd > maxfd) maxfd = sockfd;
        }
        for (set<ClientSocket *>::const_iterator s = recvSockets.begin(); s != recvSockets.end(); s++) {
            sockfd = (*s)->getSock();
            FD_SET(sockfd,&rsock);
            FD_SET(sockfd,&esock);
            if (sockfd > maxfd) maxfd = sockfd;
        }
        for (map<ServerSocket *,async_description>::const_iterator s = listenSockets.begin(); s != listenSockets.end(); s++) {
            sockfd = s->first->getSock();
            FD_SET(sockfd,&rsock);
            FD_SET(sockfd,&esock);
            if (sockfd > maxfd) maxfd = sockfd;
        }
        
        for (map<ClientSocket *,async_description>::const_iterator s = connectSockets.begin(); s != connectSockets.end(); s++) {
            sockfd = s->first->getSock();
            FD_SET(sockfd,&wsock);
            FD_SET(sockfd,&esock);
            if (sockfd > maxfd) maxfd = sockfd;
        }
        
        struct timeval tval = timeout.timeval();
        
        int res = ::select(maxfd+1, &rsock, &wsock, &esock, &tval);
        
        if (res == 0) return 0;
        if (res < 0) throw muscle_exception("Could not select socket");
        
        int flags = 0;
        
        int hasErr;
        for (set<ClientSocket *>::const_iterator s = sendSockets.begin(); s != sendSockets.end(); s++) {
            sockfd = (*s)->getSock();
            hasErr = FD_ISSET(sockfd,&esock);
            if (FD_ISSET(sockfd, &wsock) || hasErr)
            {
                *sender = *s;
                if (hasErr) flags |= 1;
                break;
            }
        }
        for (set<ClientSocket *>::const_iterator s = recvSockets.begin(); s != recvSockets.end(); s++) {
            sockfd = (*s)->getSock();
            hasErr = FD_ISSET(sockfd,&esock);
            if (FD_ISSET(sockfd, &rsock) || hasErr)
            {
                *receiver = *s;
                if (hasErr) flags |= 2;
                break;
            }
        }
        for (map<ServerSocket *,async_description>::const_iterator s = listenSockets.begin(); s != listenSockets.end(); s++) {
            sockfd = s->first->getSock();
            hasErr = FD_ISSET(sockfd,&esock);
            if (FD_ISSET(sockfd, &rsock) || hasErr)
            {
                *listener = s->first;
                if (hasErr) flags |= 4;
                break;
            }
        }
        for (map<ClientSocket *,async_description>::const_iterator s = connectSockets.begin(); s != connectSockets.end(); s++) {
            sockfd = s->first->getSock();
            hasErr = FD_ISSET(sockfd,&esock);
            if (FD_ISSET(sockfd, &wsock) || hasErr)
            {
                *connect = s->first;
                if (hasErr) flags |= 8;
                break;
            }
        }
        
        return flags;
    }
        
    size_t async_cservice::connect(int user_flag, muscle::endpoint& ep, socket_opts *opts, async_acceptlistener* accept)
    {
        if (!accept)
            throw muscle_exception("Accept listener must not be empty");
        
        size_t code = getNextCode();
        async_description desc(code, user_flag, (void *)opts, 0, accept);
        
        bool new_opts = (opts == NULL);
        if (new_opts)
            opts = new socket_opts;

        opts->blocking_connect = false;
        
        CClientSocket *sock = new CClientSocket(ep, this, *opts);

        if (new_opts)
            delete opts;

        connectSockets[sock] = desc;
        return code;
    }
}

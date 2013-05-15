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
    int async_cservice::select(const ClientSocket **sender, const ClientSocket **receiver, const ServerSocket **listener, const ClientSocket **connect, duration& timeout) const
    {
        if (sendSockets.empty() && recvSockets.empty() && listenSockets.empty()) return 0;
        
        /* args: FD_SETSIZE,writeset,readset,out-of-band sent, timeout*/
        fd_set rsock, wsock, esock;
        FD_ZERO(&rsock);
        FD_ZERO(&wsock);
        FD_ZERO(&esock);
        int maxfd = 0, sockfd;
        for (set<const ClientSocket *>::const_iterator s = sendSockets.begin(); s != sendSockets.end(); s++) {
            sockfd = (*s)->getSock();
            FD_SET(sockfd,&wsock);
            FD_SET(sockfd,&esock);
            if (sockfd > maxfd) maxfd = sockfd;
        }
        for (set<const ClientSocket *>::const_iterator s = recvSockets.begin(); s != recvSockets.end(); s++) {
            sockfd = (*s)->getSock();
            FD_SET(sockfd,&rsock);
            FD_SET(sockfd,&esock);
            if (sockfd > maxfd) maxfd = sockfd;
        }
        for (map<const ServerSocket *,async_description>::const_iterator s = listenSockets.begin(); s != listenSockets.end(); s++) {
            sockfd = s->first->getSock();
            FD_SET(sockfd,&rsock);
            FD_SET(sockfd,&esock);
            if (sockfd > maxfd) maxfd = sockfd;
        }
        
        for (map<const ClientSocket *,async_description>::const_iterator s = connectSockets.begin(); s != connectSockets.end(); s++) {
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
        for (set<const ClientSocket *>::const_iterator s = sendSockets.begin(); s != sendSockets.end(); s++) {
            sockfd = (*s)->getSock();
            hasErr = FD_ISSET(sockfd,&esock);
            if (FD_ISSET(sockfd, &wsock) || hasErr)
            {
                *sender = *s;
                if (hasErr) flags |= 1;
                break;
            }
        }
        for (set<const ClientSocket *>::const_iterator s = recvSockets.begin(); s != recvSockets.end(); s++) {
            sockfd = (*s)->getSock();
            hasErr = FD_ISSET(sockfd,&esock);
            if (FD_ISSET(sockfd, &rsock) || hasErr)
            {
                *receiver = *s;
                if (hasErr) flags |= 2;
                break;
            }
        }
        for (map<const ServerSocket *,async_description>::const_iterator s = listenSockets.begin(); s != listenSockets.end(); s++) {
            sockfd = s->first->getSock();
            hasErr = FD_ISSET(sockfd,&esock);
            if (FD_ISSET(sockfd, &rsock) || hasErr)
            {
                *listener = s->first;
                if (hasErr) flags |= 4;
                break;
            }
        }
        for (map<const ClientSocket *,async_description>::const_iterator s = connectSockets.begin(); s != connectSockets.end(); s++) {
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
    
    void async_cservice::erase_connect(size_t code)
    {
        for (map<const ClientSocket *, async_description>::iterator it = connectSockets.begin(); it != connectSockets.end(); it++)
        {
            if (it->second.code == code) {
                async_description& desc = it->second;
                desc.listener->async_done(desc.code, desc.user_flag);
                const ClientSocket *sock = it->first;
                connectSockets.erase(it);
                delete sock;
                break;
            }
        }
    }
    
    void async_cservice::run_accept(const ServerSocket *sock, bool hasErr)
    {
        async_description desc = listenSockets[sock];
        
        if (hasErr)
        {
            muscle_exception ex("ServerSocket had error");
            desc.listener->async_report_error(desc.code, desc.user_flag, ex);
            return;
        }

        ClientSocket *ccsock = NULL;
        try
        {
            socket_opts *opts = desc.data ? (socket_opts *)desc.data : new socket_opts;

            opts->blocking_connect = false;
            
            ccsock = new CClientSocket(*sock, *opts);

            if (!desc.data)
                delete opts;
        }
        catch (exception& ex)
        {
            desc.listener->async_report_error(desc.code, desc.user_flag, ex);
        }

        if (ccsock)
        {
            async_acceptlistener* accept = static_cast<async_acceptlistener*>(desc.listener);
            accept->async_accept(desc.code, desc.user_flag, ccsock);
        }
        else
        {
            muscle_exception ex("Could accept socket: " + string(strerror(errno)));
            desc.listener->async_report_error(desc.code, desc.user_flag, ex);
        }
    }
    
    void async_cservice::run_connect(const ClientSocket *sock, bool hasErr)
    {
        async_description desc = connectSockets[sock];
        connectSockets.erase(sock);

        int err = 0;
        if (hasErr || (err = sock->hasError())) {
            muscle_exception ex("Could not connect to " + sock->getAddress().str(), err);
            desc.listener->async_report_error(desc.code, desc.user_flag, ex);
            delete sock;
        } else {
            async_acceptlistener* accept = static_cast<async_acceptlistener*>(desc.listener);
            accept->async_accept(desc.code, desc.user_flag, sock);
        }
        desc.listener->async_done(desc.code, desc.user_flag);
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

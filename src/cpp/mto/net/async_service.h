//
//  async_service.h
//  CMuscle
//
//  Created by Joris Borgdorff on 17-04-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#ifndef __CMuscle__async_service__
#define __CMuscle__async_service__

#include "socket.h"

#include <queue>
#include <set>
#include <map>

namespace muscle {
    class async_service
    {
        typedef std::pair<time,async_description> timer_t;
        typedef std::map<ClientSocket *, std::queue<async_description> > sockqueue_t;
        typedef std::map<ServerSocket *, async_description> ssockdesc_t;
        typedef std::map<ClientSocket *, async_description> csockdesc_t;
        typedef std::set<ClientSocket *> csocks_t;
    public:
        async_service();
        virtual ~async_service() {};
        
        size_t send(int user_flag, ClientSocket* socket, const void *data, size_t size, async_sendlistener* send);
        size_t receive(int user_flag, ClientSocket* socket, void *data, size_t size, async_recvlistener* recv);
        size_t listen(int user_flag, ServerSocket* socket, socket_opts *,async_acceptlistener* accept);
        size_t timer(int user_flag, time& t, async_function* func, void *user_data);
        virtual size_t connect(int user_flag, muscle::SocketFactory *factory, endpoint& ep, socket_opts *opts, async_acceptlistener* accept);

        void erase(ClientSocket *socket);
        void erase(ServerSocket *socket);
        void erase_timer(size_t);
        virtual void erase_connect(size_t);
        void *update_timer(size_t, time&, void *user_data);
        
        virtual void run();
        virtual void done();
        virtual bool isDone() const;
        virtual bool isShutdown() const;
        virtual void printDiagnostics();
    protected:
        virtual void run_timer(size_t timer);
        virtual void run_send(ClientSocket *sender, bool hasErr);
        virtual void run_recv(ClientSocket *receiver, bool hasErr);
        virtual void run_accept(ServerSocket *listener, bool hasErr);
        virtual void run_connect(ClientSocket *connect, bool hasErr);
        size_t getNextCode() { return _current_code++; }
        virtual int select(ClientSocket **sender, ClientSocket **receiver, ServerSocket **listener, ClientSocket **connect, duration& utimeout);
        csocks_t recvSockets;
        csocks_t sendSockets;
        ssockdesc_t listenSockets;
        csockdesc_t connectSockets;
    private:
        size_t _current_code;
        volatile bool is_done;
        volatile bool is_shutdown;
        sockqueue_t recvQueues;
        sockqueue_t sendQueues;
        std::map<size_t,timer_t> timers;
        std::map<size_t,async_description> done_timers;
        size_t next_alarm();
    };
}
#endif /* defined(__CMuscle__async_service__) */

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
        typedef std::pair<ClientSocket *,std::queue<async_description> > csockqueuepair_t;
        typedef std::vector<csockqueuepair_t *> sockqueue_t;
        typedef std::vector<std::pair<ServerSocket *, async_description> *> ssockdesc_t;
        typedef std::vector<std::pair<ClientSocket *, async_description> *> csockdesc_t;
    public:
		enum SendOptions {
			NONE = 0,
			PLUG_CORK = 1,
			UNPLUG_CORK = 2
		};
		
        async_service(size_t limitSendSize = 6*1024*1024);
        
        size_t send(int user_flag, ClientSocket* socket, const void *data, size_t size, async_sendlistener* send, int options);
        size_t receive(int user_flag, ClientSocket* socket, void *data, size_t size, async_recvlistener* recv);
        size_t listen(int user_flag, ServerSocket* socket, socket_opts *,async_acceptlistener* accept);
        size_t timer(int user_flag, time& t, async_function* func, void *user_data);
        virtual size_t connect(int user_flag, muscle::SocketFactory *factory, endpoint& ep, socket_opts *opts, async_acceptlistener* accept);

        void erase(ClientSocket *socket);
        void erase(ServerSocket *socket);
        void erase_timer(size_t);
        void erase_connect(size_t);
        void *update_timer(size_t, time&, void *user_data);
        
		void run();
        void done();
        bool isDone() const;
        bool isShutdown() const;
        void printDiagnostics();
    private:
        void run_timer(size_t timer);
        void run_send(int fd, bool hasErr);
        void run_recv(int fd, bool hasErr);
        void run_accept(int fd, bool hasErr);
        void run_connect(int fd, bool hasErr);
        size_t getNextCode() { return _current_code++; }
        int select(int *writeFd, int *readableWriteFd, int *readFd, duration& utimeout);

		std::vector<int> readFds;
        std::vector<int> readableWriteFds;
        std::vector<int> writeFds;
        ssockdesc_t listenSockets;
        csockdesc_t connectSockets;
        sockqueue_t recvQueues;
        sockqueue_t sendQueues;
		
        std::map<size_t,timer_t> timers;
        std::map<size_t,async_description> done_timers;
        size_t next_alarm();
		
		size_t _current_code;
		bool is_communicating;
		
		void do_erase_commsocket();
		
        volatile bool is_done;
        volatile bool is_shutdown;
		
		size_t szSendBuffers;
		const size_t limitReadAtSendBufferSize;

		std::queue<int> readFdsToErase;
		std::queue<int> writeFdsToErase;
		std::queue<int> readableWriteFdsToErase;
    };
}
#endif /* defined(__CMuscle__async_service__) */

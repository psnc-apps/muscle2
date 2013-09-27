//
//  async_service.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 17-04-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include "async_service.h"

#include <sys/time.h>
#include <errno.h>
#include <unistd.h>
#include <string>
#include <cstring>
#include <cassert>
#include <algorithm>

using namespace std;

namespace muscle
{
    async_service::async_service(const size_t limitSendSize, const int limitBufferNum) : _current_code(1), is_done(false), is_shutdown(false), is_communicating(false), limitReadAtSendBufferSize(limitSendSize), szSendBuffers(0), limitReadAtSendBufferNum(limitBufferNum), numSendBuffers(0)
    {}
    
    size_t async_service::send(const int user_flag, ClientSocket * const socket, const void * const data, const size_t size, async_sendlistener* send, const int options)
    {
        if (!socket || !data)
            throw muscle_exception("Socket and data must not be empty");
        if (!send)
            send = new async_sendlistener_delete();
        
        const size_t code = getNextCode();
		const int fd = socket->getWriteSock();

		if (sendQueues.size() <= fd)
			sendQueues.resize(fd + 1);
		
		if (!sendQueues[fd]) {
			sendQueues[fd] = new csockqueuepair_t(socket, queue<async_description>());

			// send queue did not exist, so the fd was not yet in the fd set
			if (socket->selectWriteFdIsReadable())
				readableWriteFds.push_back(fd);
			else
				writeFds.push_back(fd);
		}
		
		async_description desc(code, user_flag, (void *)data, size, send);
		desc.opts = options;

		sendQueues[fd]->second.push(desc);
		szSendBuffers += size;
		numSendBuffers++;

        return code;
    }
    
    size_t async_service::receive(const int user_flag, ClientSocket *socket, void *data, size_t size, async_recvlistener* recv)
    {
        if (!socket || !data || !recv)
            throw muscle_exception("Socket, data and receiver must not be empty");
        
        const size_t code = getNextCode();
		const int fd = socket->getReadSock();

		if (recvQueues.size() <= fd)
			recvQueues.resize(fd + 1);
		
		if (!recvQueues[fd]) {
			recvQueues[fd] = new csockqueuepair_t(socket, queue<async_description>());
			// recvQueue was empty, so the fd was not yet in the fd set
			readFds.push_back(fd);
		}
		
		recvQueues[fd]->second.push(async_description(code, user_flag, data, size, recv));
		
        return code;
    }
    size_t async_service::listen(const int user_flag, ServerSocket *socket, socket_opts *opts, async_acceptlistener* accept)
    {
        if (!socket || !accept)
            throw muscle_exception("Socket and accept listener must not be empty");
        
        const size_t code = getNextCode();
        async_description desc(code, user_flag, (void *)opts, 0, accept);
        
		const int fd = socket->getReadSock();
		if (listenSockets.size() <= fd)
			listenSockets.resize(fd + 1);
		
		assert(listenSockets[fd] == NULL);
		
        listenSockets[fd] = new pair<ServerSocket *, async_description>(socket, desc);

		if (find(readFds.begin(), readFds.end(), fd)==readFds.end())
			readFds.push_back(fd);
		
        return code;
        
    }
    size_t async_service::timer(const int user_flag, mtime& t, async_function* func, void *user_data)
    {
        if (!func)
            throw muscle_exception("Function for timer must not be empty");
        
        const size_t code = getNextCode();
        async_description desc(code, user_flag, user_data, 0, func);
        
        timers[code] = timer_t(t, desc);
        return code;
    }
    
    size_t async_service::connect(const int user_flag, muscle::SocketFactory *factory, muscle::endpoint& ep, socket_opts *opts, async_acceptlistener* accept)
    {
        if (!accept || !opts)
            throw muscle_exception("Accept listener or options must not be empty");
        
        opts->blocking_connect = false;
        ClientSocket *sock = factory->connect(ep, *opts);
        
		const int fd = sock->getWriteSock();
		const size_t code = getNextCode();

        async_description desc(code, user_flag, (void *)opts, 0, accept);
		if (connectSockets.size() <= fd)
			connectSockets.resize(fd + 1);
		
		assert(connectSockets[fd] == NULL);

		connectSockets[fd] = new pair<ClientSocket *, async_description>(sock, desc);

		if (sock->selectWriteFdIsReadable())
			readableWriteFds.push_back(fd);
		else
			writeFds.push_back(fd);

        return code;
    }
    
    void async_service::erase_socket(const int rfd, const int wfd, const int rwfd)
    {
		if (rfd >= 0)
			readFdsToErase.push_back(rfd);
		if (wfd >= 0)
			writeFdsToErase.push_back(wfd);
		if (rwfd >= 0)
			readableWriteFdsToErase.push_back(rwfd);
		
		if (!is_communicating) {
			is_communicating = true;
			do_erase_commsocket();
			is_communicating = false;
		}
	}
	void async_service::do_erase_commsocket()
	{
		while (!readFdsToErase.empty())
		{
			const int fd = readFdsToErase.back();
			readFdsToErase.pop_back();
			
			vector<int>::iterator it = find(readFds.begin(), readFds.end(), fd);
			if (it != readFds.end())
			{
				readFds.erase(it);

				csockqueuepair_t *sockQueue = recvQueues[fd];
				if (sockQueue)
				{
					queue<async_description> &q = sockQueue->second;
					while (!q.empty()) {
						async_description& desc = q.front();
						async_recvlistener* recv = static_cast<async_recvlistener*>(desc.listener);
						recv->async_received(desc.code, desc.user_flag, desc.data, desc.data_ptr, desc.size, -1);
						recv->async_done(desc.code, desc.user_flag);
						q.pop();
					}

					delete sockQueue;
					recvQueues[fd] = NULL;
				}
			}
		}
		
		while (!writeFdsToErase.empty() || !readableWriteFdsToErase.empty()) {
			vector<int>& sendQueue = writeFdsToErase.empty() ? readableWriteFdsToErase : writeFdsToErase;
			vector<int>& sendFds = writeFdsToErase.empty() ? readableWriteFds : writeFds;
			const int fd = sendQueue.back();
			sendQueue.pop_back();
			
			vector<int>::iterator it = find(sendFds.begin(), sendFds.end(), fd);

			if (it != sendFds.end())
			{
				sendFds.erase(it);

				csockqueuepair_t *sockQueue = sendQueues[fd];
				if (sockQueue)
				{
					queue<async_description> &q = sockQueue->second;
					// descs exists
					while (!q.empty()) {
						async_description& desc = q.front();
						szSendBuffers -= desc.size;
						numSendBuffers--;
						async_sendlistener* send = static_cast<async_sendlistener*>(desc.listener);
						send->async_sent(desc.code, desc.user_flag, desc.data, desc.size, -1);
						send->async_done(desc.code, desc.user_flag);
						q.pop();
					}
					// Still a transfer in progress
					delete sockQueue;
					sendQueues[fd] = NULL;
				}
			}
		}
    }
    
    void async_service::erase_listen(const int rfd)
    {
		if (rfd >= listenSockets.size()) return;
		
        pair<ServerSocket *, async_description> *ssockDesc = listenSockets[rfd];
        if (ssockDesc)
        {
            async_description& desc = ssockDesc->second;
            desc.listener->async_done(desc.code, desc.user_flag);
			delete ssockDesc;
            listenSockets[rfd] = NULL;
			readFds.erase(find(readFds.begin(), readFds.end(), rfd));
        }
    }
    
    void async_service::erase_timer(const size_t code)
    {
        if (!code)
            throw muscle_exception("Timer is not initialized");
        
        std::map<size_t,timer_t>::iterator itt = timers.find(code);
        if (itt != timers.end())
        {
            async_description& desc = itt->second.second;
            desc.listener->async_done(code, desc.user_flag);
            timers.erase(itt);
        }
        std::map<size_t,async_description>::iterator itd = done_timers.find(code);
        if (itd != done_timers.end())
        {
            async_description& desc = itd->second;
            desc.listener->async_done(code, desc.user_flag);
            done_timers.erase(itd);
        }
    }
    
    void *async_service::update_timer(size_t timer, mtime& t, void *user_data)
    {
        if (!timer)
            throw muscle_exception("Timer is not initialized");

        void *old_data = NULL;
        
        if (timers.find(timer) != timers.end())
        {
            timer_t& desc = timers[timer];
            desc.first = t;
            old_data = desc.second.data;
            desc.second.data = user_data;
        }
        else if (done_timers.find(timer) != done_timers.end())
        {
            timer_t& desc = timers[timer] = timer_t(t, done_timers[timer]);
            done_timers.erase(timer);
            old_data = desc.second.data;
            desc.second.data = user_data;
        } else
            throw muscle_exception("Given code is invalid, or the timer is erased.");
        
        return old_data;
    }
    
    void async_service::run()
    {
        while (!is_done)
        {
            size_t t = next_alarm();
            timer_t *timer = t ? &timers[t] : NULL;
            if (t && timer->first.is_past())
                run_timer(t);
            else
            {
                duration timeout = duration(10,0);
                if (t)
                {
                    duration untilNextEvent = timer->first.duration_until();
                    if (untilNextEvent < timeout)
                        timeout = untilNextEvent;
                }
                
                try {
					int rfd = 0, rwfd = 0, wfd = 0;

                    const bool hasErr = (select(&wfd, &rwfd, &rfd, timeout) == -1);
                    
					if (wfd) {
						if (connectSockets.size() > wfd && connectSockets[wfd])
							run_connect(wfd, hasErr);
						if (sendQueues.size() > wfd && sendQueues[wfd])
							run_send(wfd, hasErr);
					} else if (rwfd) {
						if (connectSockets.size() > rwfd && connectSockets[rwfd])
							run_connect(rwfd, hasErr);
						if (sendQueues.size() > rwfd && sendQueues[rwfd])
							run_send(rwfd, hasErr);
					} else if (rfd) {
						if (listenSockets.size() > rfd && listenSockets[rfd])
							run_accept(rfd, hasErr);
						if (recvQueues.size() > rfd && recvQueues[rfd])
							run_recv(rfd, hasErr);
					}
                } catch (const muscle::muscle_exception& ex) {
                    // Interruption doesn't matter, we can continue to the next part of the loop.
                    if (ex.error_code != EINTR)
                        throw ex;
                }
            }
            
            if (readFds.empty() && writeFds.empty() && readableWriteFds.empty())
            {
                size_t t = next_alarm();
                if (t)
                    timers[t].first.sleep();
                else
                    break;
            }
        }
		
		while (!readFds.empty()) {
			const int fd = readFds[0];
			if (recvQueues.size() > fd && recvQueues[fd])
				erase_socket(fd, -1, -1);
			else if (listenSockets.size() > fd && listenSockets[fd])
				erase_listen(fd);
		}
		
		while (!writeFds.empty()) {
			const int fd = writeFds[0];
			if (sendQueues.size() > fd && sendQueues[fd])
				erase_socket(-1, fd, -1);
			else if (connectSockets.size() > fd && connectSockets[fd])
				erase_connect(connectSockets[fd]->second.code);
		}
		
		while (!readableWriteFds.empty()) {
			const int fd = readableWriteFds[0];
			if (sendQueues.size() > fd && sendQueues[fd])
				erase_socket(-1, -1, fd);
			else if (connectSockets.size() > fd && connectSockets[fd])
				erase_connect(connectSockets[fd]->second.code);
		}
		
        is_shutdown = true;
    }
        
    void async_service::run_timer(size_t timer)
    {
        async_description& desc = done_timers[timer] = timers[timer].second;
        timers.erase(timer);
        
        try
        {
            async_function *func = static_cast<async_function*>(desc.listener);
            func->async_execute(timer, desc.user_flag, desc.data);
        }
        catch (exception& ex)
        {
            desc.listener->async_report_error(timer, desc.user_flag, ex);
        }
    }
    void async_service::run_send(int fd, bool hasErr)
    {
		is_communicating = true;

		ClientSocket *sock = sendQueues[fd]->first;
		queue<async_description>& q = sendQueues[fd]->second;
        async_description& desc = q.front();
		const bool isReadable = sock->selectWriteFdIsReadable();

        ssize_t status = -1;
        int is_final = true;
        
        async_sendlistener* sender = static_cast<async_sendlistener*>(desc.listener);

        if (hasErr)
        {
            muscle_exception ex("Socket had error");
            sender->async_report_error(desc.code, desc.user_flag, ex);
        }
        else
        {
            try {
				if (desc.opts & PLUG_CORK) {
					sock->setCork(true);
				}
                status = sock->send(desc.data_ptr, desc.data_remain());
				if (desc.opts & UNPLUG_CORK) {
					sock->setCork(false);
				}
				logger::finest("Sent %zd/%zu bytes", status, desc.data_remain());
            } catch (exception& ex) {
               sender->async_report_error(desc.code, desc.user_flag, ex);
            }

            if (status > 0)
            {
                char *new_data_ptr = desc.data_ptr_advance(status);
                is_final = (new_data_ptr == desc.data_end());
                if (is_final) {
                    sender->async_sent(desc.code, desc.user_flag, desc.data, desc.size, 1);
                } else {
                    sender->async_sent(desc.code, desc.user_flag, desc.data_ptr, status, 0);
                    desc.data_ptr = new_data_ptr;
                }
            } else if (status == 0) {
				is_final = false;
			} else {
                muscle_exception ex("Could not send all data", errno);
                desc.listener->async_report_error(desc.code, desc.user_flag, ex);
            }
        }
        
        if (is_final) {
            if (status == -1)
                sender->async_sent(desc.code, desc.user_flag, desc.data, desc.size, -1);
            
			szSendBuffers -= desc.size;

			q.pop();
			numSendBuffers--;
            sender->async_done(desc.code, desc.user_flag);
			
            if (q.empty()) {
				if (isReadable)
					readableWriteFdsToErase.push_back(fd);
				else
					writeFdsToErase.push_back(fd);
            }
        }
		
		is_communicating = false;
		// erasing any postponed erases
		do_erase_commsocket();
    }

    void async_service::run_recv(int fd, bool hasErr)
    {
		is_communicating = true;

		ClientSocket *sock = recvQueues[fd]->first;
		queue<async_description>& q = recvQueues[fd]->second;
        async_description& desc = q.front();
        
        ssize_t status = -1;
		bool is_final = true;

        async_recvlistener *recv = static_cast<async_recvlistener*>(desc.listener);
        
        if (hasErr) {
            muscle_exception ex("Socket had error");
            recv->async_report_error(desc.code, desc.user_flag, ex);
        }
        else
        {
            try {
                status = sock->recv(desc.data_ptr, desc.data_remain());
				logger::fine("Received %zd/%zu bytes", status, desc.data_remain());
            } catch (exception& ex) {
                recv->async_report_error(desc.code, desc.user_flag, ex);
            }
			
            if (status >= 0)
            {
                char *new_data_ptr = desc.data_ptr_advance(status);
                is_final = (new_data_ptr == desc.data_end());
                
                if (is_final) {
                    recv->async_received(desc.code, desc.user_flag, desc.data, desc.data_ptr, desc.size, 1);
                } else {
                    is_final = !recv->async_received(desc.code, desc.user_flag, desc.data, desc.data_ptr, status, 0);
                    desc.data_ptr = new_data_ptr;
                }
            }
            else
            {
                string msg = status == -1 ? "Closing connection" : "Sending end closed connection";
                muscle_exception ex(msg, errno, true);
                recv->async_report_error(desc.code, desc.user_flag, ex);
            }
        }
        if (is_final) {
            if (status < 0)
                recv->async_received(desc.code, desc.user_flag, desc.data, desc.data_ptr, desc.size, -1);
            
            recv->async_done(desc.code, desc.user_flag);

			q.pop();

            if (q.empty())
				readFdsToErase.push_back(fd);
        }
		
		is_communicating = false;
		// erasing any postponed erases
		do_erase_commsocket();
    }
    
    bool async_service::isDone() const
    {
        return is_done;
    }
    
    void async_service::done()
    {
        is_done = true;
    }
    
    bool async_service::isShutdown() const
    {
        return is_shutdown;
    }
    
    size_t async_service::next_alarm()
    {
        mtime min = mtime::far_future();
        size_t timer = 0;
        
        for (map<size_t,timer_t>::iterator t = timers.begin(); t != timers.end(); t++)
        {
            if (t->second.first < min) {
                min = t->second.first;
                timer = t->first;
            }
        }
        return timer;
    }
    
    void async_service::printDiagnostics()
    {
        size_t num, totalsz;
        logger::info("Asynchronous service diagnostics:");
		
		num = 0; totalsz = 0;
        for (sockqueue_t::iterator it = sendQueues.begin(); it != sendQueues.end(); ++it)
        {
			if (*it == NULL)
				continue;
			
			num++;
			queue<async_description> &q = (*it)->second;
			const size_t szQ = q.size();
            for (int i = 0; i < szQ; ++i)
            {
                async_description &desc = q.front();
                totalsz += desc.size;
                q.push(desc);
                q.pop();
            }
        }
        logger::info("    Number of sending sockets: %zu; total size of reserved buffers: %zu, sending to:", num, totalsz);
        for (sockqueue_t::iterator it = sendQueues.begin(); it != sendQueues.end(); ++it) {
            if (*it != NULL)
				logger::info("        %s", (*it)->first->str().c_str());
		}

		num = 0; totalsz = 0;
        for (sockqueue_t::iterator it = recvQueues.begin(); it != recvQueues.end(); ++it)
        {
			if (*it == NULL)
				continue;
			
			num++;
			queue<async_description> &q = (*it)->second;
			const size_t szQ = q.size();
            for (int i = 0; i < szQ; ++i)
            {
                async_description &desc = q.front();
                totalsz += desc.size;
                q.push(desc);
                q.pop();
            }
        }
        logger::info("    Number of receiving sockets: %zu; total size of reserved buffers: %zu; receiving from:", num, totalsz);
        for (sockqueue_t::iterator it = recvQueues.begin(); it != recvQueues.end(); ++it) {
			if (*it != NULL)
				logger::info("        %s", (*it)->first->str().c_str());
		}

		num = 0;
        for (ssockdesc_t::iterator it = listenSockets.begin(); it != listenSockets.end(); ++it) {
			if (*it != NULL)
				num++;
		}
        logger::info("    Number of listening sockets: %zu; listening at:", num);
        for (ssockdesc_t::iterator it = listenSockets.begin(); it != listenSockets.end(); ++it) {
			if (*it != NULL)
				logger::info("        %s", (*it)->first->str().c_str());
		}
        
        num = 0;
        for (csockdesc_t::iterator it = connectSockets.begin(); it != connectSockets.end(); ++it) {
			if (*it != NULL)
				num++;
		}
        logger::info("    Number of connecting sockets: %zu; connecting to:", num);
        for (csockdesc_t::iterator it = connectSockets.begin(); it != connectSockets.end(); ++it) {
			if (*it != NULL)
				logger::info("        %s", (*it)->first->str().c_str());
		}
        
        num = timers.size();
        logger::info("    Number of active timers: %zu; at times:", num);
        for (map<size_t,timer_t>::iterator it = timers.begin(); it != timers.end(); ++it)
        {
            mtime& t = it->second.first;
            if (t.is_past())
                logger::info("        -%s", t.duration_since().str().c_str());
            else
                logger::info("        %s", t.duration_until().str().c_str());
        }

        num = done_timers.size();
        logger::info("    Number of inactive timers: %zu", num);
    }
    
    void async_service::erase_connect(size_t code)
    {
        for (int fd = 1; fd < connectSockets.size(); ++fd)
        {
			if (connectSockets[fd] == NULL) continue;
			
			async_description& desc = connectSockets[fd]->second;
            if (desc.code == code) {
                desc.listener->async_done(desc.code, desc.user_flag);
                ClientSocket * const sock = connectSockets[fd]->first;

				vector<int>& connectFds = (sock->selectWriteFdIsReadable()
										   ? readableWriteFds
										   : writeFds);
				connectFds.erase(find(connectFds.begin(), connectFds.end(), fd));
				
				delete sock;
				delete connectSockets[fd];
				connectSockets[fd] = NULL;
                break;
            }
        }
    }
    
    void async_service::run_accept(int fd, bool hasErr)
    {
		ServerSocket *sock = listenSockets[fd]->first;
        async_description& desc = listenSockets[fd]->second;
        
        if (hasErr) {
            muscle_exception ex("ServerSocket had error");
            desc.listener->async_report_error(desc.code, desc.user_flag, ex);
            return;
        }
        
        socket_opts *opts = static_cast<socket_opts*>(desc.data);
		opts->blocking_connect = false;

		ClientSocket *ccsock = NULL;

        try {
            ccsock = sock->accept(*opts);
        } catch (const exception& ex) {
            desc.listener->async_report_error(desc.code, desc.user_flag, ex);
			return;
        }
        
        if (ccsock) {
            async_acceptlistener* accept = static_cast<async_acceptlistener*>(desc.listener);
            accept->async_accept(desc.code, desc.user_flag, ccsock);
        } else {
            muscle_exception ex("Could not accept socket", errno);
            desc.listener->async_report_error(desc.code, desc.user_flag, ex);
        }
    }
    
    void async_service::run_connect(int fd, bool hasErr)
    {
		ClientSocket *sock = connectSockets[fd]->first;
        async_description desc = connectSockets[fd]->second; // copy, so that on erase it can still be used
		delete connectSockets[fd];
		connectSockets[fd] = NULL;

		vector<int>& connectFds = (sock->selectWriteFdIsReadable() ? readableWriteFds : writeFds);
		connectFds.erase(find(connectFds.begin(), connectFds.end(), sock->getWriteSock()));
        
        int err = 0;
        if (hasErr || (err = sock->hasError())) {
            muscle_exception ex("Could not connect to " + sock->getAddress().str(), err, true);
            desc.listener->async_report_error(desc.code, desc.user_flag, ex);
            delete sock;
        } else {
            async_acceptlistener* accept = static_cast<async_acceptlistener*>(desc.listener);
            accept->async_accept(desc.code, desc.user_flag, sock);
        }
        desc.listener->async_done(desc.code, desc.user_flag);
    }
    
    int async_service::select(int *writeFd, int *readableWriteFd, int *readFd, duration& timeout)
    {
        if (readFds.empty() && readableWriteFds.empty() && writeFds.empty()) return 0;
        
        /* args: FD_SETSIZE,writeset,readset,out-of-band sent, timeout*/
        fd_set rsock, wsock, esock;
        FD_ZERO(&rsock);
        FD_ZERO(&wsock);
        FD_ZERO(&esock);
        int maxfd = 0;
		if (szSendBuffers <= limitReadAtSendBufferSize && numSendBuffers <= limitReadAtSendBufferNum) {
			for (vector<int>::iterator it = readFds.begin(); it != readFds.end(); it++) {
				FD_SET(*it,&rsock);
				FD_SET(*it,&esock);
				if (*it > maxfd) maxfd = *it;
			}
		}
		else
			logger::finer("Limiting receives: buffers %zu/%zu and #buffers %d/%d",
						  szSendBuffers, limitReadAtSendBufferSize,
						  numSendBuffers, limitReadAtSendBufferNum);
		
        for (vector<int>::iterator it = readableWriteFds.begin(); it != readableWriteFds.end(); it++) {
            FD_SET(*it,&rsock); // To accomodate for pipes used in mpsocket
            FD_SET(*it,&esock);
            if (*it > maxfd) maxfd = *it;
        }
        for (vector<int>::iterator it = writeFds.begin(); it != writeFds.end(); it++) {
            FD_SET(*it,&wsock);
            FD_SET(*it,&esock);
            if (*it > maxfd) maxfd = *it;
        }
        
        struct timeval tval = timeout.timeval();
        
        int res = ::select(maxfd+1, &rsock, &wsock, &esock, &tval);
        
        if (res == 0) return 0;
        if (res < 0) throw muscle_exception("Could not select socket");
        
        for (vector<int>::iterator it = writeFds.begin(); it != writeFds.end(); it++) {
			if (FD_ISSET(*it, &esock)) {
				*writeFd = *it;
				return -1;
			}
			if (FD_ISSET(*it, &wsock)) {
				*writeFd = *it;
				return 0;
			}
        }
        for (vector<int>::iterator it = readableWriteFds.begin(); it != readableWriteFds.end(); it++) {
			if (FD_ISSET(*it, &esock)) {
				*readableWriteFd = *it;
				return -1;
			}
			if (FD_ISSET(*it, &rsock)) {
				*readableWriteFd = *it;
				return 0;
			}
        }
		if (szSendBuffers <= limitReadAtSendBufferSize && numSendBuffers <= limitReadAtSendBufferNum) {
			for (vector<int>::iterator it = readFds.begin(); it != readFds.end(); it++) {
				if (FD_ISSET(*it, &esock)) {
					*readFd = *it;
					return -1;
				}
				if (FD_ISSET(*it, &rsock)) {
					*readFd = *it;
					return 0;
				}
			}
		}
        
        return 0;
    }

}

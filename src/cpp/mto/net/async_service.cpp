//
//  async_service.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 17-04-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include "../net/async_service.h"

#include <sys/time.h>
#include <errno.h>
#include <unistd.h>
#include <string>
#include <cstring>
#include <cassert>

using namespace std;

namespace muscle
{
    async_service::async_service() : _current_code(1), is_done(false), is_shutdown(false)
    {}
    
    size_t async_service::send(int user_flag, ClientSocket *socket, const void *data, size_t size, async_sendlistener* send)
    {
        if (!socket || !data)
            throw muscle_exception("Socket and data must not be empty");
        if (!send)
            send = new async_sendlistener_delete();
        
        size_t code = getNextCode();
        async_description desc(code, user_flag, (void *)data, size, send);
        
        sendSockets.insert(socket);
        sendQueues[socket].push(desc);
        return code;
    }
    
    size_t async_service::receive(int user_flag, ClientSocket *socket, void *data, size_t size, async_recvlistener* recv)
    {
        if (!socket || !data || !recv)
            throw muscle_exception("Socket, data and receiver must not be empty");
        
        size_t code = getNextCode();
        async_description desc(code, user_flag, data, size, recv);
        
        recvSockets.insert(socket);
        recvQueues[socket].push(desc);
        return code;
        
    }
    size_t async_service::listen(int user_flag, ServerSocket *socket, socket_opts *opts, async_acceptlistener* accept)
    {
        if (!socket || !accept)
            throw muscle_exception("Socket and accept listener must not be empty");
        
        size_t code = getNextCode();
        async_description desc(code, user_flag, (void *)opts, 0, accept);
        
        listenSockets[socket] = desc;
        return code;
        
    }
    size_t async_service::timer(int user_flag, time& t, async_function* func, void *user_data)
    {
        if (!func)
            throw muscle_exception("Function for timer must not be empty");
        
        size_t code = getNextCode();
        async_description desc(code, user_flag, user_data, 0, func);
        
        timers[code] = timer_t(t, desc);
        return code;
    }
    
    void async_service::erase(ClientSocket *socket)
    {
        csocks_t::iterator it = sendSockets.find(socket);
        if (it != sendSockets.end())
        {
            async_description progressing;

            sockqueue_t::iterator descs = sendQueues.find(socket);
            while (!descs->second.empty()) {
                async_description& desc = descs->second.front();
                if (desc.in_progress == 1)
                {
                    assert(progressing.code == 0);
                    progressing = desc;
                    progressing.in_progress = -1;
                }
                else
                {
                    async_sendlistener* send = static_cast<async_sendlistener*>(desc.listener);
                    send->async_sent(desc.code, desc.user_flag, desc.data, desc.size, -1);
                    send->async_done(desc.code, desc.user_flag);
                }
                descs->second.pop();
            }
            // Still a transfer in progress
            if (progressing.code)
                descs->second.push(progressing);
            else
            {
                sendSockets.erase(it);
                sendQueues.erase(descs);
            }
        }
        it = recvSockets.find(socket);
        if (it != recvSockets.end())
        {
            async_description progressing;

            sockqueue_t::iterator descs = recvQueues.find(socket);
            while (!descs->second.empty()) {
                async_description& desc = descs->second.front();
                if (desc.in_progress == 1)
                {
                    assert(progressing.code == 0);
                    progressing = desc;
                    progressing.in_progress = -1;
                }
                else
                {
                    async_recvlistener* recv = static_cast<async_recvlistener*>(desc.listener);
                    recv->async_received(desc.code, desc.user_flag, desc.data, desc.size, -1);
                    recv->async_done(desc.code, desc.user_flag);
                }
                descs->second.pop();
            }
            // Still a transfer in progress
            if (progressing.code)
                descs->second.push(progressing);
            else
            {
                recvQueues.erase(descs);
                recvSockets.erase(it);
            }
        }
    }
    
    void async_service::erase(ServerSocket *socket)
    {
        ssockdesc_t::iterator it = listenSockets.find(socket);
        if (it != listenSockets.end())
        {
            async_description& desc = it->second;
            desc.listener->async_done(desc.code, desc.user_flag);
            listenSockets.erase(it);
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
    
    void *async_service::update_timer(size_t timer, time& t, void *user_data)
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
            ssize_t ret = run_once();
            if (ret > 0)
                timers[ret].first.sleep();
            else if (ret < 0)
                break;
        }
        is_shutdown = true;
    }
    
    ssize_t async_service::run_once()
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
            
            ClientSocket *sender = NULL, *receiver = NULL, *connect = NULL;
            ServerSocket *listener = NULL;
            
            try {
                int res = select(&sender, &receiver, &listener, &connect, timeout);
                
                if (connect != NULL) run_connect(connect, res&8);
                else if (listener != NULL) run_accept(listener, res&4);
                else if (sender != NULL) run_send(sender, res&1);
                else if (receiver != NULL) run_recv(receiver, res&2);
            } catch (const muscle::muscle_exception& ex) {
                // Interruption doesn't matter, we can continue to the next part of the loop.
                if (ex.error_code == EINTR)
                    return 1;
                else
                    throw ex;
            }
        }
        
        if (recvSockets.empty() && sendSockets.empty() && listenSockets.empty() && connectSockets.empty())
        {
            size_t t = next_alarm();
            if (t)
                return t;
            else
                return -1;
        }
        return 0;
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
    void async_service::run_send(ClientSocket *sock, bool hasErr)
    {
        async_description& d = sendQueues[sock].front();
        d.in_progress = 1;

        // Copy to avoid problems due to deletion, due to call to erase
        async_description desc = d;
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
            try
            {
                status = sock->isend(desc.data, desc.size);
            }
            catch (exception& ex)
            {
               sender->async_report_error(desc.code, desc.user_flag, ex);
            }

            if (status > 0)
            {
                char *new_data_ptr = desc.data_ptr_advance(status);
                is_final = new_data_ptr == desc.data_end();
                if (is_final)
                {
                    sender->async_sent(desc.code, desc.user_flag, desc.data, desc.size, 1);
                }
                else
                {
                    sender->async_sent(desc.code, desc.user_flag, desc.data_ptr, status, 0);
                    desc.data_ptr = new_data_ptr;
                }
            }
            else if (status == 0) is_final = false;
            else
            {
                muscle_exception ex("Could not send all data: " + string(strerror(errno)));
                desc.listener->async_report_error(desc.code, desc.user_flag, ex);
            }
        }
        
        if (is_final) {
            if (status == -1)
                sender->async_sent(desc.code, desc.user_flag, desc.data, desc.size, -1);
            
            sender->async_done(desc.code, desc.user_flag);

            if (sendQueues[sock].size() == 1)
            {
                sendQueues.erase(sock);
                sendSockets.erase(sock);
            }
            else
                sendQueues[sock].pop();
        }
        else
        {
            async_description& front = sendQueues[sock].front();
            if (front.in_progress == -1)
            {
                sender->async_sent(desc.code, desc.user_flag, desc.data, desc.size, -1);
                sender->async_done(desc.code, desc.user_flag);
                
                sendQueues.erase(sock);
                sendSockets.erase(sock);
            }
            else
                front.in_progress = 0;
        }
    }

    void async_service::run_recv(ClientSocket *sock, bool hasErr)
    {
        bool is_final = true;

        ssize_t status = -1;
        async_description& d = recvQueues[sock].front();
        d.in_progress = true;
        
        // Copy to avoid problems due to deletion, due to call to erase
        async_description desc = d;
        async_recvlistener *recv = static_cast<async_recvlistener*>(desc.listener);
        
        if (hasErr)
        {
            muscle_exception ex("Socket had error");
            recv->async_report_error(desc.code, desc.user_flag, ex);
        }
        else
        {
            try
            {
                status = sock->irecv(desc.data_ptr, desc.data_remain());
            }
            catch (exception& ex)
            {
                recv->async_report_error(desc.code, desc.user_flag, ex);
            }
            if (status > 0)
            {
                char *new_data_ptr = desc.data_ptr_advance(status);
                is_final = new_data_ptr == desc.data_end();
                
                if (is_final)
                {
                    recv->async_received(desc.code, desc.user_flag, desc.data, desc.size, 1);
                }
                else
                {
                    is_final = !recv->async_received(desc.code, desc.user_flag, desc.data_ptr, status, 0);
                    desc.data_ptr = new_data_ptr;
                    
                }
            }
            else
            {
                string msg = status == -1 ? string(strerror(errno)) : "sending end closed connection";
                muscle_exception ex(msg);
                recv->async_report_error(desc.code, desc.user_flag, ex);
            }
        }
        if (is_final) {
            if (status < 0)
                recv->async_received(desc.code, desc.user_flag, desc.data, desc.size, -1);
            
            recv->async_done(desc.code, desc.user_flag);

            if (recvQueues[sock].size() == 1)
            {
                recvQueues.erase(sock);
                recvSockets.erase(sock);
            }
            else
                recvQueues[sock].pop();
        }
        else
        {
            async_description& front = recvQueues[sock].front();
            if (front.in_progress == -1)
            {
                recv->async_received(desc.code, desc.user_flag, desc.data, desc.size, -1);
                recv->async_done(desc.code, desc.user_flag);
                
                recvQueues.erase(sock);
                recvSockets.erase(sock);
            }
            else
                front.in_progress = 0;
        }
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
        time min = time::far_future();
        size_t timer = 0;
        
        for (map<size_t,timer_t>::iterator t = timers.begin(); t != timers.end(); t++)
        {
            if (t->second.first < min)
            {
                min = t->second.first;
                timer = t->first;
            }
        }
        return timer;
    }
    
    void async_service::printDiagnostics()
    {
        size_t num, totalsz;
        int msgTypes = Logger::MsgType_ClientConn|Logger::MsgType_PeerConn;
        Logger::info(msgTypes, "Asynchronous service diagnostics:");
        totalsz = 0;
        for (sockqueue_t::iterator it = sendQueues.begin(); it != sendQueues.end(); ++it)
        {
            size_t szQ = it->second.size();
            for (int i = 0; i < szQ; ++i)
            {
                async_description &desc = it->second.front();
                totalsz += desc.size;
                it->second.push(desc);
                it->second.pop();
            }
        }
        num = sendQueues.size();
        Logger::info(msgTypes, "    Number of sending sockets: %zu; total size of reserved buffers: %zu, sending to:", num, totalsz);
        for (csocks_t::iterator it = sendSockets.begin(); it != sendSockets.end(); ++it)
            Logger::info(msgTypes, "        %s", (*it)->str().c_str());

        totalsz = 0;
        for (sockqueue_t::iterator it = recvQueues.begin(); it != recvQueues.end(); ++it)
        {
            size_t szQ = it->second.size();
            for (int i = 0; i < szQ; ++i)
            {
                async_description &desc = it->second.front();
                totalsz += desc.size;
                it->second.push(desc);
                it->second.pop();
            }
        }
        num = recvQueues.size();
        Logger::info(msgTypes, "    Number of receiving sockets: %zu; total size of reserved buffers: %zu; receiving from:", num, totalsz);
        for (csocks_t::iterator it = recvSockets.begin(); it != recvSockets.end(); ++it)
            Logger::info(msgTypes, "        %s", (*it)->str().c_str());

        num = listenSockets.size();
        Logger::info(msgTypes, "    Number of listening sockets: %zu; listening at:", num);
        for (ssockdesc_t::iterator it = listenSockets.begin(); it != listenSockets.end(); ++it)
            Logger::info(msgTypes, "        %s", it->first->str().c_str());
        
        num = connectSockets.size();
        Logger::info(msgTypes, "    Number of connecting sockets: %zu; connecting to:", num);
        for (csockdesc_t::iterator it = connectSockets.begin(); it != connectSockets.end(); ++it)
            Logger::info(msgTypes, "        %s", it->first->str().c_str());
        
        num = timers.size();
        Logger::info(msgTypes, "    Number of active timers: %zu; at times:", num);
        for (map<size_t,timer_t>::iterator it = timers.begin(); it != timers.end(); ++it)
        {
            time& t = it->second.first;
            if (t.is_past())
                Logger::info(msgTypes, "        -%s", t.duration_since().str().c_str());
            else
                Logger::info(msgTypes, "        %s", t.duration_until().str().c_str());
        }

        num = done_timers.size();
        Logger::info(msgTypes, "    Number of inactive timers: %zu", num);
    }
    
    void async_service::erase_connect(size_t code)
    {
        for (map<ClientSocket *, async_description>::iterator it = connectSockets.begin(); it != connectSockets.end(); it++)
        {
            if (it->second.code == code) {
                async_description& desc = it->second;
                desc.listener->async_done(desc.code, desc.user_flag);
                ClientSocket *sock = it->first;
                connectSockets.erase(it);
                delete sock;
                break;
            }
        }
    }
    
    void async_service::run_accept(ServerSocket *sock, bool hasErr)
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
            
            ccsock = sock->accept(*opts);
            
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
    
    void async_service::run_connect(ClientSocket *sock, bool hasErr)
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

}

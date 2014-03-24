//
//  mpsocket.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 04-06-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include "mpsocket.h"
#include "muscle2/util/async_service.h"
#include "mpwide/MPWide.h"
#include "mpwide/Socket.h"

#include <strings.h>
#include <cstring>
#include <set>
#include <sys/types.h>
#include <sys/socket.h>
#include <netdb.h>
#include <errno.h>
#include <fcntl.h>
#include <cassert>
#include <signal.h>
#include <sys/select.h>
#include <string>
#include <pthread.h>

#ifndef MSG_NOSIGNAL
#ifdef SO_NOSIGPIPE
#define MSG_NOSIGNAL SO_NOSIGPIPE
#else
#define MSG_NOSIGNAL msg_nosignal_is_not_defined
#endif
#endif

using namespace std;

namespace muscle {
	using namespace net;
	mutex mpsocket::path_mutex;
	
	/////// Internal MPWide wrappers ///////
	// They do locking on a shared mutex, so take
	// care to not lock any mutexes before calling
	// these functions
    
	// Returns path_id if succeeded and -1 if failed.
	static int _mpsocket_do_connect(const endpoint& ep, const socket_opts& opts, bool asServer)
	{
		int path_id;

		if (!asServer && ep.isWildcard()) {
			logger::severe("Cannot connect to a wildcard address %s",ep.str().c_str());
			return -1;
		}
		const string host = asServer ? "0" : ep.getHost();
		
		{
			// Need to modify stream list
			// All creations are mutually exclusive. We assume that a path
			// creation does not structurally modify existing static MPWide data
			// so that it does not rule out reading/writing operations.
			mutex_lock lock = mpsocket::path_mutex.acquire();
			path_id = MPW_CreatePathWithoutConnect(host, ep.port, opts.max_connections);

		}

        if (path_id >= 0 && MPW_ConnectPath(path_id, asServer) == -1)
		{
			// Clean up
			mutex_lock lock = mpsocket::path_mutex.acquire();
			logger::fine("Destroying path %d", path_id);
			MPW_DestroyPath(path_id);
			
            path_id = -1;
        }
        return path_id;
    }
    
    static ssize_t _mpsocket_do_send(char *data, size_t sz, int pathid)
    {
        ssize_t ret = MPW_SendRecv(data, sz, (char *)0, 0, pathid);
		if (ret >= 0)
			return sz;
		else
			return ret;
    }
    
    static ssize_t _mpsocket_do_recv(char *data, size_t sz, int pathid)
    {
        // Read from streams list
        ssize_t ret = MPW_SendRecv((char *)0, 0, data, sz, pathid);
		if (ret >= 0)
			return sz;
		else
			return ret;
    }
    
    /////// Thread handlers /////////////

    void *mpsocket_thread::run()
    {
		ssize_t *ret = new ssize_t;
		if (send) {
            *ret = _mpsocket_do_send(data, sz, sock->pathid);
		} else {
            *ret = _mpsocket_do_recv(data, sz, sock->pathid);
		}
        return ret;
    }
	
	void mpsocket_thread::afterRun() {
		if (send)
			sock->addWriteReady(0);
		else
			sock->addReadReady(0);
	}
	
    // The code seems a bit round-about, but we need to avoid
    // taking too strict locks, and to avoid double locking.
    void *mpsocket_connect_thread::run()
    {
        int *res = new int;
        *res = _mpsocket_do_connect(ep, opts, asServer);
        return res;
    }
	
	void mpsocket_connect_thread::deleteResult(void *result)
	{
		int *res = (int *)result;
		if (*res != -1) {
			mutex_lock lock = mpsocket::path_mutex.acquire();
			MPW_DestroyPath(*res);
		}
		delete res;
	}
	
	void mpsocket_connect_thread::afterRun()
	{
		if (asServer)
			sock->addReadReady(0);
		else
			sock->addWriteReady(0);
	}
    
    //////// mpsocket /////////////////
    
    /** CLIENT SIDE **/
	
	// Accept
    MPClientSocket::MPClientSocket(const ServerSocket& parent, int pathid, const socket_opts& opts) : msocket(parent), pathid(pathid), sendThread(0), recvThread(0), connectThread(0), last_send(0), last_recv(0)
    {
        addReadReady(0);
        addWriteReady(0);
    }

    // Connect
    MPClientSocket::MPClientSocket(endpoint& ep, async_service *service, const socket_opts& opts) : msocket(ep, service), sendThread(0), recvThread(0), connectThread(0), last_send(0), last_recv(0)
    {
        if (opts.blocking_connect)
        {
            pathid = _mpsocket_do_connect(ep, opts, false);
            if (pathid == -1)
                throw muscle_exception("Could not connect to " + ep.str());
          
            if (opts.recv_buffer_size != -1)
                setWin(opts.recv_buffer_size);
            else if (opts.send_buffer_size != -1)
                setWin(opts.send_buffer_size);

            addWriteReady(0);
			addReadReady(0);
        }
        else
        {
            pathid = -1;
            connectThread = new mpsocket_connect_thread(ep, opts, this, false);
        }
    }
    
	// Cancel, close and destroy
    MPClientSocket::~MPClientSocket()
    {
		async_cancel();
		
        delete recvThread;
		delete sendThread;

        if (connectThread) {
			connectThread->cancel();
			int *res = (int *)connectThread->getResult();
			if (res && *res != -1)
				pathid = *res;
			delete res;
			delete connectThread;
		}
		
		if (pathid >= 0) {
			mutex_lock lock = path_mutex.acquire();
			MPW_DestroyPath(pathid);
		}
    }
    
    ssize_t MPClientSocket::recv(void * const s, const size_t size)
    {
		return runInThread(false, (void *)s, size, recvThread);
    }
    
    ssize_t MPClientSocket::send(const void * const s, const size_t size)
    {
		return runInThread(true, (void *)s, size, sendThread);
    }
    
    ssize_t MPClientSocket::runInThread(const bool send, void * const s, const size_t sz, mpsocket_thread *&last_thread)
    {

		if (last_thread)
		{
			// We can't mix two messages
			if (last_thread->data != s) return 0;
            
			// wait until the previous message is sent
			const ssize_t * const res = (ssize_t *)last_thread->getResult();
			const ssize_t ret = res == NULL ? -1 : *res;
            delete last_thread;
			delete res;
			last_thread = 0;

			// success or fail
			return ret;
		}
		else
		{
            // Clear pipe
			if (send)
				removeWriteReady();
			else
				removeReadReady();

			last_thread = new mpsocket_thread(send, s, sz, this);
            // will send it, but up to now we sent nothing.
			return 0;
		}
    }
        
    int MPClientSocket::hasError()
    {
        if (!isConnecting() && pathid < 0)
            return ECONNREFUSED;
        else
            return 0;
    }
    
    bool MPClientSocket::isConnecting()
    {
        if (connectThread && (connectThread->isDone() || hasWriteReady())) {
            int *res = (int *)connectThread->getResult();
			if (res) pathid = *res;
            delete res;
            delete connectThread;
            connectThread = 0;
			addReadReady(0);
        }
        
        return connectThread != NULL;
    }

    void MPClientSocket::setWin(const ssize_t size)
    {
        assert(size > 0);
        MPW_setPathWin(pathid, (int)size);
    }
	
	void MPClientSocket::async_cancel()
	{
		if (server != NULL)
			server->erase_socket(sockfd, -1, readableWriteFd);
	}

    /** SERVER SIDE **/
    MPServerSocket::MPServerSocket(endpoint& ep, async_service *service, const socket_opts& opts) : msocket(ep, service), ServerSocket(opts), server_opts(opts)
    {
        listener = new mpsocket_connect_thread(address, server_opts, this, true);
    }
    
    ClientSocket *MPServerSocket::accept(const socket_opts &opts)
    {
        int *res = (int *)listener->getResult();
        
        // Clear pipe buffer
        removeReadReady();
        
        ClientSocket *sock;
        if (res == NULL || *res == -1)
            sock = NULL;
        else
		{
			try {
				sock = new MPClientSocket(*this, *res, opts);
			} catch (const muscle_exception& ex) {
				const char *reason = ex.what();
				logger::severe("Could not create new client socket: %s", reason);
				sock = NULL;
			}
		}
        
        delete res;
        delete listener;
        listener = new mpsocket_connect_thread(address, server_opts, this, true);
        return sock;
    }

	void MPServerSocket::async_cancel()
	{
		if (server != NULL)
			server->erase_listen(sockfd);
	}
	
    ClientSocket *MPSocketFactory::connect(muscle::net::endpoint &ep, const muscle::net::socket_opts &opts)
    {
        return new MPClientSocket(ep, service, opts);
    }
    
    ServerSocket *MPSocketFactory::listen(muscle::net::endpoint &ep, const muscle::net::socket_opts &opts)
    {
        return new MPServerSocket(ep, service, opts);
    }
    
    int MPSocketFactory::num_mpsocket_factories = 0;
    
    MPSocketFactory::MPSocketFactory(async_service *service) : SocketFactory(service)
    {
        ++num_mpsocket_factories;
    }
    
    MPSocketFactory::~MPSocketFactory()
    {
        if (--num_mpsocket_factories == 0) {
			mutex_lock lock = mpsocket::path_mutex.acquire();
            MPW_Finalize();
        }
    }
}

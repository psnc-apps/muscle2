//
//  mpsocket.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 04-06-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include "mpsocket.h"
#include "async_service.h"
#include "../mpwide/MPWide.h"
#include "../mpwide/Socket.h"

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
    rwmutex mpsocket::path_mutex;
    mutex mpsocket::accept_destroy_mutex;
  
    /////// Internal MPWide wrappers ///////
    // They do locking on a shared mutex, so take
    // care to not lock any mutexes before calling
    // these functions
    
    // Returns path_id if succeeded and -1 if failed.
    static int _mpsocket_do_connect(const endpoint& ep, const socket_opts& opts, bool asServer)
    {
        int pathid;
        {
			// Need to modify stream list
			// All creations are mutually exclusive. We assume that a path
			// creation modifies existing static MPWide structures so that it
			// rules out reading/writing operations.
			rwmutex_lock wrlock = mpsocket::path_mutex.acquireWrite();
			pathid = MPW_CreatePathWithoutConnect(ep.getHost(), ep.port, opts.max_connections);
		}

		bool is_valid;
		if (pathid < 0) {
			is_valid = false;
		} else if (asServer) {
			// Usually, we won't mind if MPWide's datastructures change due to
			// a new CreatePath during an accept. In some cases this
			// WILL cause corruption, further changes to MPWide are
			// necessary to prevent this. The reason we can't have a
			// read lock here is that accepts typically take very long
			// and in the mean time no connect may take place. That would
			// be bad (deadlocks).
			mutex_lock lock = mpsocket::accept_destroy_mutex.acquire();
			is_valid = (MPW_ConnectPath(pathid, true) == 0);
		} else {
			// Need to connect, this is blocking but we only need
			// read permissions of the stream list since we won't
			// modify MPWide's internal structures.
			rwmutex_lock rdlock = mpsocket::path_mutex.acquireRead();
			is_valid = (MPW_ConnectPath(pathid, false) == 0);
		}
		
        if (!is_valid) {
            // Clean up
			MPSocketFactory::destroyer->destroyPath(pathid);
            pathid = -1;
        }
        return pathid;
    }
    
    static ssize_t _mpsocket_do_send(char *data, size_t sz, int pathid)
    {
        // Read from streams list
        rwmutex_lock rdlock = mpsocket::path_mutex.acquireRead();
        ssize_t ret = MPW_SendRecv(data, sz, (char *)0, 0, pathid);
		if (ret >= 0)
			return sz;
		else
			return ret;
    }
    
    static ssize_t _mpsocket_do_recv(char *data, size_t sz, int pathid)
    {
        // Read from streams list
        rwmutex_lock rdlock = mpsocket::path_mutex.acquireRead();
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
        if (cache.stop_condition) {
			*ret = -1;
		} else if (send) {
            *ret = _mpsocket_do_send(data, sz, sock->pathid);
			sock->setWriteReady();
		} else {
            *ret = _mpsocket_do_recv(data, sz, sock->pathid);
			sock->setReadReady();
		}
        
		if (cache.stop_condition && *ret != -1)
			*ret = -1;

        return ret;
    }
    
    // The code seems a bit round-about, but we need to avoid
    // taking too strict locks, and to avoid double locking.
    void *mpsocket_connect_thread::run()
    {
        if (cache.stop_condition)
            return NULL;

        int *res = new int;
        *res = _mpsocket_do_connect(ep, opts, asServer);
  
        // if the stop signal was sent but the socket was valid
        // destroy it anyway.
		if (cache.stop_condition && *res != -1) {
			MPSocketFactory::destroyer->destroyPath(*res);
			*res = -1;
		}

		if (asServer)
			sock->setReadReady();
		else
			sock->setWriteReady();
        
        return res;
    }
    
	void mpsocket_destroy_thread::destroyPath(const int pathid)
	{
		mutex_lock lock = mut.acquire();
		paths.push(pathid);
		lock.notify();
	}

	void *mpsocket_destroy_thread::run()
	{
		while (true)
		{
			int path_id;
			{
				mutex_lock lock = mut.acquire();
				while (paths.empty() && !cache.stop_condition) {
					logger::finer("Waiting paths to be destroyed");
					lock.wait();
				}
				path_id = paths.top();
				paths.pop();
				if (cache.stop_condition) break;
			}
			
			{
				// First we wait for any accepts to finish. This may take a long time.
				mutex_lock accept_lock = mpsocket::accept_destroy_mutex.acquire();
				
				// Once accept is done, we can try to get a lock for other operations
				// We can't get a full lock, since we might cause a deadlock for
				// dependant send/receive calls.
				logger::finer("Waiting for lock to destroy paths");
				rwmutex_lock *wrlock;
				while ((wrlock = mpsocket::path_mutex.tryWrite()) == NULL)
					usleep(100000); // 0.1 second, rather arbitrary
				
				logger::fine("Destroying path %d", path_id);
				MPW_DestroyPath(path_id);
				delete wrlock;
			}
		}
		
		return NULL;
    }
    
    void mpsocket_destroy_thread::cancel()
    {
		pthread_cancel(t);
        mutex_lock lock = mut.acquire();
        cache.stop_condition = true;
        lock.notify();
    }

    //////// mpsocket /////////////////
    
    mpsocket::mpsocket() : socket((async_service *)0)
    {
        int fd[2];
        if (pipe(fd) == -1) throw muscle_exception("Could not create MPWide socket pipe", errno);
        sockfd = fd[0];
        writableReadFd = fd[1];

        if (pipe(fd) == -1) throw muscle_exception("Could not create MPWide socket pipe", errno);
        readableWriteFd = fd[0];
        writableWriteFd = fd[1];
    }
    
    mpsocket::~mpsocket()
    {
        close(sockfd);
        close(writableReadFd);
        close(readableWriteFd);
        close(writableWriteFd);
    }
    
	int mpsocket::getWriteSock() const
	{
		return readableWriteFd;
	}
	
    void mpsocket::setReadReady() const
    {
        char c = 1;
        ::write(writableReadFd, &c, 1);
    }
    void mpsocket::unsetReadReady() const
    {
        char c;
        ::read(sockfd, &c, 1);
    }
	
    bool mpsocket::isReadReady() const
    {
		int result = Socket_select(sockfd, 0, MPWIDE_SOCKET_WRMASK, 0, 1);
		return result >= 0 && (result&MPWIDE_SOCKET_RDMASK)==MPWIDE_SOCKET_RDMASK;
    }
    void mpsocket::setWriteReady() const
    {
        char c = 1;
        ::write(writableWriteFd, &c, 1);
    }
    void mpsocket::unsetWriteReady() const
    {
        char c;
        ::read(readableWriteFd, &c, 1);
    }
	
    bool mpsocket::isWriteReady() const
    {
		int result = Socket_select(readableWriteFd, 0, MPWIDE_SOCKET_WRMASK, 0, 1);
		return result >= 0 && (result&MPWIDE_SOCKET_RDMASK)==MPWIDE_SOCKET_RDMASK;
    }
  
    /** CLIENT SIDE **/
    MPClientSocket::MPClientSocket(const ServerSocket& parent, int pathid, const socket_opts& opts) : socket(parent), pathid(pathid), sendThread(0), recvThread(0), connectThread(0), last_send(0), last_recv(0)
    {
        setReadReady();
        setWriteReady();
    }
    
    MPClientSocket::MPClientSocket(endpoint& ep, async_service *service, const socket_opts& opts) : socket(ep, service), sendThread(0), recvThread(0), connectThread(0), last_send(0), last_recv(0)
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

            setWriteReady();
			setReadReady();
        }
        else
        {
            pathid = -1;
            connectThread = new mpsocket_connect_thread(ep, opts, this, false);
        }
    }
    
    MPClientSocket::~MPClientSocket()
    {
        if (recvThread) delete recvThread;
		if (sendThread) delete recvThread;

        if (connectThread)
			delete connectThread;
        else if (pathid >= 0)
            MPSocketFactory::destroyer->destroyPath(pathid);
    }
    
    ssize_t MPClientSocket::send(const void * const s, const size_t size) const
    {
        // Wait until the previous result is sent or received
        if (sendThread)
            sendThread->getResult();

        return _mpsocket_do_send((char *)s, size, pathid);
    }
    
    
    ssize_t MPClientSocket::recv(void * const s, const size_t size) const
    {
        // Wait until the previous result is sent or received
        if (recvThread)
            recvThread->getResult();

        return _mpsocket_do_recv((char *)s, size, pathid);
        
        return size;
    }
    
    ssize_t MPClientSocket::irecv(void * const s, const size_t size)
    {
		return runInThread(false, (void *)s, size, recvThread);
    }
    
    ssize_t MPClientSocket::isend(const void * const s, const size_t size)
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
			const ssize_t ret = *res;
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
				unsetWriteReady();
			else
				unsetReadReady();

			last_thread = new mpsocket_thread(send, s, sz, this);
            // will send it, but up to now we sent nothing.
			return 0;
		}
    }
    
    ssize_t MPClientSocket::async_recv(int user_flag, void* s, size_t size, async_recvlistener *receiver)
    {
        return server->receive(user_flag, this, s, size, receiver);
    }
    
    ssize_t MPClientSocket::async_send(int user_flag, const void* s, size_t size, async_sendlistener *send)
    {
        return server->send(user_flag, this, s, size, send);
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
        if (connectThread && connectThread->isDone()) {
            int *res = (int *)connectThread->getResult();
            pathid = *res;
            delete res;
            delete connectThread;
            connectThread = 0;
			setReadReady();
        }
        
        return connectThread != NULL;
    }

    void MPClientSocket::setWin(const ssize_t size)
    {
        assert(size > 0);
        rwmutex_lock rdlock = path_mutex.acquireRead();
        MPW_setPathWin(pathid, (int)size);
    }

    /** SERVER SIDE **/
    MPServerSocket::MPServerSocket(endpoint& ep, async_service *service, const socket_opts& opts) : socket(ep, service), ServerSocket(opts), server_opts(opts)
    {
        listener = new mpsocket_connect_thread(endpoint("0", address.port), server_opts, this, true);
    }
    
    ClientSocket *MPServerSocket::accept(const socket_opts &opts)
    {
        int *res = (int *)listener->getResult();
        
        // Clear pipe buffer
        unsetReadReady();
        
        ClientSocket *sock;
        if (*res == -1)
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
        listener = new mpsocket_connect_thread(endpoint("0", address.port), server_opts, this, true);
        return sock;
    }

    size_t MPServerSocket::async_accept(int user_flag, async_acceptlistener *accept, socket_opts *opts)
    {
        return server->listen(user_flag, this, opts, accept);
    }
        
    ClientSocket *MPSocketFactory::connect(muscle::endpoint &ep, const muscle::socket_opts &opts)
    {
        return new MPClientSocket(ep, service, opts);
    }
    
    ServerSocket *MPSocketFactory::listen(muscle::endpoint &ep, const muscle::socket_opts &opts)
    {
        return new MPServerSocket(ep, service, opts);
    }
    
    int MPSocketFactory::num_mpsocket_factories = 0;
    mpsocket_destroy_thread *MPSocketFactory::destroyer = (mpsocket_destroy_thread *)0;
    
    MPSocketFactory::MPSocketFactory(async_service *service) : SocketFactory(service)
    {
        if (num_mpsocket_factories++ == 0) {
			destroyer = new mpsocket_destroy_thread;
		}
    }
    
    MPSocketFactory::~MPSocketFactory()
    {
        if (--num_mpsocket_factories == 0)
        {
			delete destroyer;
			destroyer = NULL;
			
            rwmutex_lock wrlock = mpsocket::path_mutex.acquireWrite();
            MPW_Finalize();
        }
    }
}

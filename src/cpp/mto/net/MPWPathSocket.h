//
//  MPWPathSocket.h
//  CMuscle
//
//  Created by Joris Borgdorff on 25-07-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#ifndef __CMuscle__MPWPathSocket__
#define __CMuscle__MPWPathSocket__

#include "muscle2/util/msocket.h"
#include "muscle2/util/csocket.h"
#include "muscle2/util/thread.h"
#include "DecoupledSelectSocket.h"
#include "ThreadPool.h"

#include <map>
#include <vector>

namespace muscle {
	namespace net {
		class MPWPathSendRecvThread;
		class MPWPathConnectThread;
		
		class MPWPathSocket : virtual public DecoupledSelectSocket
		{
		public:
			MPWPathSocket(muscle::util::ThreadPool *tpool) : tpool(tpool) {}
			virtual ~MPWPathSocket() {}
		protected:
			MPWPathSocket() {}
			muscle::util::ThreadPool *tpool;
		};
		
		/*** CLIENT ***/
		
		class MPWPathClientSocket : public MPWPathSocket, public ClientSocket
		{
		public:
			MPWPathClientSocket(muscle::util::ThreadPool *tpool, endpoint& ep, async_service *server, const socket_opts& opts, int num_threads);
			MPWPathClientSocket(muscle::util::ThreadPool *tpool, endpoint& ep, async_service *server, const socket_opts& opts, int num_threads, int num_channels, CClientSocket **channels);
			virtual ~MPWPathClientSocket();
			
			muscle::util::duration pacing_time;

			// Light-weight, non-blocking
			virtual ssize_t send (const void* s, size_t size);
			virtual ssize_t recv (void* s, size_t size);

			virtual void async_cancel();
			virtual int hasError();
			
			virtual bool selectWriteFdIsReadable() const { return true; }
		private:
			void clear(MPWPathSendRecvThread **commThreads);
			ssize_t runInThread(bool send, char *data, size_t size);
			
			const int num_threads, num_channels;
			CClientSocket **channels;
			MPWPathConnectThread *connector;
			MPWPathSendRecvThread **sendThreads, **recvThreads;
			int numSendThreads, numRecvThreads;
			int numSendThreadsFinished, numRecvThreadsFinished;
			size_t *sendIndexes, *recvIndexes;
			char *sendData, *recvData;
			
			int *fds;
		};
	
		class MPWPathConnectThread : public muscle::util::thread
		{
		public:
			MPWPathConnectThread(int num_channels, MPWPathClientSocket *employer, const socket_opts& opts) : num_channels(num_channels), employer(employer), opts(opts)
			{}
			virtual void *run();
			virtual void afterRun();
			virtual void deleteResult(void *);
		private:
			const int num_channels;
			socket_opts opts;
			MPWPathClientSocket * const employer;
		};
		
		class MPWPathSendRecvThread : public muscle::util::thread
		{
		public:
			MPWPathSendRecvThread(int thread_num, int channel_num, bool send, char *data, size_t *indexes, int *fds, int num_channels, const muscle::util::duration& pacing_time, MPWPathClientSocket *employer);
			virtual ~MPWPathSendRecvThread();
			virtual void *run();
			virtual void afterRun();
			virtual void deleteResult(void *res) { delete (int*)res; }
		private:
			// Data to be sent. This pointer is equal for all send/receive threads.
			char * const data;

			const int thread_num, channel_num;
			// Contains the indexes of the data to be sent, one index per channel
			const size_t * const indexes;
			// The number of channels that this thread will manage
			const int num_channels;
			// Pause after every send/receive to allow the network stack to process the previous message
			const muscle::util::duration pacing_time;
			// Whether this is a sending thread
			const bool send;
			// Whether the communication of a channel is finished, one entry per channel
			bool *commDone;
			// The file descriptors belonging to the channels
			const int * const fds;
			// The socket that issued the send/receive request
			MPWPathClientSocket * const employer;
			// Select a channel within a certain timeout
			int select(const muscle::util::duration &timeout);
		};
		
		/*** SERVER ***/
		class MPWPathAcceptThread;
		
		class MPWPathServerSocket : public MPWPathSocket, public ServerSocket
		{
		public:
			MPWPathServerSocket(muscle::util::ThreadPool *tpool, endpoint& ep, async_service *server, const socket_opts &opts);

			virtual ClientSocket *accept(const socket_opts& opts);
			virtual void async_cancel();
			virtual bool selectWriteFdIsReadable() const { return false; }
			virtual int getWriteSock() const { return sockfd; }
		private:
			int currentId;
			int num_threads;
			socket_opts copts;
			CServerSocket *serversock;
			MPWPathAcceptThread *acceptor;
		};
		
		class MPWPathAcceptThread : public muscle::util::thread
		{
		public:
			MPWPathAcceptThread(socket_opts& opts, ServerSocket *ss, int use_id, int max_connections, MPWPathServerSocket *employer, muscle::util::duration timeout) : opts(opts), serversock(ss), use_id(use_id), max_connections(max_connections), employer(employer), timeout(timeout) { start(); }
			virtual void *run();
			virtual void afterRun();
			virtual void deleteResult(void *);
			int target_size;
		private:
			socket_opts opts;
			ServerSocket * const serversock;
			MPWPathServerSocket * const employer;
			muscle::util::duration timeout;
			const int use_id, max_connections;
		};
		
		/*** FACTORY ***/
		
		class MPWPathSocketFactory : public SocketFactory
		{
		public:
			MPWPathSocketFactory(async_service * service, int num_threads_, bool useThreadPool);
			virtual ~MPWPathSocketFactory();
			virtual ClientSocket *connect(endpoint& ep, const socket_opts& opts);
			virtual ServerSocket *listen(endpoint& ep, const socket_opts& opts);
		private:
			const int num_threads;
			muscle::util::ThreadPool *tpool;
		};
	}
}

#endif /* defined(__CMuscle__MPWPathSocket__) */

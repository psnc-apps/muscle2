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

#include <map>
#include <vector>

namespace muscle {
	namespace net {
		class MPWPathSendRecvThread;
		class MPWPathConnectThread;
		
		class MPWPathSocket : virtual public msocket
		{
		public:
			virtual ~MPWPathSocket();
			
			virtual void setReadReady() const;
			virtual void unsetReadReady() const;
			virtual bool isReadReady() const;
			virtual void setBlocking(bool) {}
			
			const static int RDMASK, WRMASK;
		protected:
			MPWPathSocket();

			int writableWriteFd, readableWriteFd, writableReadFd;
		};
		
		class MPWPathClientSocket : public MPWPathSocket, public ClientSocket
		{
		public:
			MPWPathClientSocket(endpoint& ep, async_service *server, const socket_opts& opts, int num_threads);
			MPWPathClientSocket(endpoint& ep, async_service *server, const socket_opts& opts, int num_threads, int num_channels, CClientSocket **channels);
			virtual ~MPWPathClientSocket();
			
			muscle::util::duration pacing_time;

			// Light-weight, non-blocking
			virtual ssize_t send (const void* s, size_t size);
			virtual ssize_t recv (void* s, size_t size);

			virtual void async_cancel();
			virtual int hasError();
			
			virtual void setWriteReady() const;
			virtual void unsetWriteReady() const;
			virtual bool isWriteReady() const;
			virtual int getWriteSock() const;
			virtual bool selectWriteFdIsReadable() const { return true; }
		private:
			void clear(bool send);
			ssize_t runInThread(bool send, char *data, size_t size);
			
			const int num_threads, num_channels;
			size_t *indexes;
			MPWPathSendRecvThread **sendThreads, **recvThreads;
			CClientSocket **channels;
			MPWPathConnectThread *connector;
			int *fds;
		};
	
		class MPWPathAcceptThread;
		
		class MPWPathSendRecvThread : public muscle::util::thread
		{
		public:
			MPWPathSendRecvThread(bool send, char *data, size_t *indexes, int *fds, int num_channels, const muscle::util::duration& pacing_time, MPWPathClientSocket *employer);
			virtual ~MPWPathSendRecvThread();
			virtual void *run();
			// Data to be sent. This pointer is equal for all send/receive threads.
			char * const data;
		private:
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
		
		class MPWPathConnectThread : public muscle::util::thread
		{
		public:
			MPWPathConnectThread(int num_channels, MPWPathClientSocket *employer, const socket_opts& opts) : num_channels(num_channels), employer(employer), opts(opts)
			{ start(); }
			virtual void *run();
		private:
			void *clear(const CClientSocket * const * sockets);
			const int num_channels;
			socket_opts opts;
			MPWPathClientSocket * const employer;
		};
		
		class MPWPathAcceptThread;
		
		class MPWPathServerSocket : public MPWPathSocket, public ServerSocket
		{
		public:
			MPWPathServerSocket(endpoint& ep, async_service *server, const socket_opts &opts);

			virtual ClientSocket *accept(const socket_opts& opts);
			virtual void async_cancel();
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
			MPWPathAcceptThread(socket_opts& opts, ServerSocket *ss, int use_id, int max_connections, MPWPathServerSocket *employer) : opts(opts), serversock(ss), use_id(use_id), max_connections(max_connections), employer(employer) { start(); }
			virtual void *run();
			int target_size;
		private:
			static void *clear(const CClientSocket *latest_sock, const CClientSocket * const * sockets, int current_size);
			socket_opts opts;
			ServerSocket * const serversock;
			MPWPathServerSocket * const employer;
			const int use_id, max_connections;
		};
		
		class MPWPathSocketFactory : public SocketFactory
		{
		public:
			MPWPathSocketFactory(async_service * service, int num_threads_) : SocketFactory(service), num_threads(num_threads_)
			{}
			virtual ~MPWPathSocketFactory() {}
			virtual ClientSocket *connect(endpoint& ep, const socket_opts& opts);
			virtual ServerSocket *listen(endpoint& ep, const socket_opts& opts);
		private:
			const int num_threads;
		};
	}
}

#endif /* defined(__CMuscle__MPWPathSocket__) */

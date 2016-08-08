//
//  mbarrier.h
//  CMuscle
//
//  Created by Joris Borgdorff on 26-11-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#ifndef __CMuscle__mbarrier__
#define __CMuscle__mbarrier__

#include "thread.h"
#include "msocket.h"

namespace muscle {
	namespace util {
		/**
		 * Barrier is a central class that signals when it may be passed.
		 * It starts a thread which will wait for a given number of sockets
		 * to connect to its server socket. It sends a single byte to each of
		 * those sockets for every signal, unblocking them.
		 */
		class Barrier : public thread
		{
		public:
			Barrier(int num_clients);
			virtual ~Barrier();
			/**
			 * Creates server socket, stores server socket address in epBuffer,
			 * accepts num_clients sockets, signals the sockets for each call
			 * to signal, and cleans up the resources.
			 */
			virtual void *run();
			/**
			 * Signal that the Barrier should be lifted.
			 */
			virtual void signal();
			/**
			 * Pass an empty pointer, createBuffer will allocate the memory
			 * needed.
			 * @returns size of the created buffer
			 */
			static size_t createBuffer(char **buffer);
			/**
			 * Sets the contents of the given buffer, allocated with 
			 * createBuffer, to data that the BarrierClient needs.
			 */
			virtual void fillBuffer(char *buffer);
			virtual void deleteResult(void *) {}
		private:
			/** Number of processes that are involved in the barrier */
			const int num_clients;
			/** 
			 * Number of signals that will be sent. Increment from main and
			 * decrement from thread, both with an acquired signalMutex.
			 */
			int signals;
			/**
			 * Buffer of the endpoint. Write from thread and read from main,
			 * both with an acquired signalMutex.
			 */
			const char *epBuffer;
			/** Mutex to keep the thread and its calls in sync */
			mutex signalMutex;
			
			/** Whether the barrier init sequence has finished */
			bool isInitialized;
		};

		/**
		 * The BarrierClient connects to a Barrier given its buffer, and waits 
		 * for its signal. Only one BarrierClient should be created per waiting
		 * process, and the total should match the number of clients that the
		 * Barrier expects.
		 */
		class BarrierClient
		{
		public:
			BarrierClient(const char *barrier_server);
			virtual ~BarrierClient();
			/** Waits until the barrier sends a signal */
			virtual int wait();
		private:
			net::ClientSocket *csock;
		};
	}
}

#endif /* defined(__CMuscle__mbarrier__) */

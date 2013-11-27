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

		class Barrier : public thread
		{
		public:
			Barrier(int num_procs);
			virtual ~Barrier();
			virtual void fillBuffer(char *buffer);
			virtual void *run();
			virtual void signal();
			static size_t createBuffer(char **buffer);
		private:
			const int num_procs;
			mutex signalMutex;
			int signals;
			net::endpoint *ep;
		};
		
		class BarrierClient
		{
		public:
			BarrierClient(const char *server);
			virtual ~BarrierClient();
			virtual int wait();
		private:
			net::ClientSocket *csock;
		};
	}
}

#endif /* defined(__CMuscle__mbarrier__) */

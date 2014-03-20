//
//  ThreadPool.h
//  CMuscle
//
//  Created by Joris Borgdorff on 3/20/14.
//  Copyright (c) 2014 Joris Borgdorff. All rights reserved.
//

#ifndef __CMuscle__ThreadPool__
#define __CMuscle__ThreadPool__

#include "muscle2/util/thread.h"

namespace muscle {
	namespace util {
		class ThreadRunner : public thread {
		public:
			ThreadRunner() : task(NULL) { start(); }
			virtual ~ThreadRunner() {}
			
			virtual void *run();
			bool isBusy();
			void setTask(thread *t);
			virtual void cancel();
		private:
			thread *task;
			mutex tmutex;
		};
		class ThreadPool {
		public:
			ThreadPool(int base_threads);
			virtual ~ThreadPool();
			
			void execute(thread *t);
		private:
			const int base_threads;
			ThreadRunner** runners;
		};
	}
}

#endif /* defined(__CMuscle__ThreadPool__) */

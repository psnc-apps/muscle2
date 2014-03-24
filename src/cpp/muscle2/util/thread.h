//
//  thread.h
//  CMuscle
//
//  Created by Joris Borgdorff on 19-06-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#ifndef __CMuscle__thread__
#define __CMuscle__thread__

#include "mutex.h"

namespace muscle {
	namespace util {
		class thread;
		
		struct _shared_thread_cache
		{
			_shared_thread_cache() : stop_condition(false), done(false) {}
			
			// Called from owner
			void setThread(thread *t) { runner = t; }
			
			// Called from thread
			void *run();
			volatile bool done;
			volatile bool stop_condition;
		private:
			thread *runner;
		};
		
		class thread
		{
		public:
			thread();
			
			// Only allowed after the thread has terminated
			// Will call cancel (and block) if this is not the case
			virtual ~thread();
			
			virtual void *run() = 0;
			virtual void runWithoutThread();
			// Non-blocking
			virtual bool isDone() { return cache.done; } // Read from owner
			// Blocking
			virtual void *getResult(); // Read from owner, sets stop signal
			virtual void *getRawResult() { return result; } // Read without other operations
			virtual void cancel() { cache.stop_condition = true; } // Called from owner, sets stop signal
			virtual bool isCancelled() { return cache.stop_condition; }
			virtual void beforeRun() {}
			virtual void afterRun() {}
			virtual void start(); // May be called from constructor
			virtual void deleteResult(void *) = 0;
			void deleteResult() {
				if (result != NULL) {
					deleteResult(result);
					result = NULL;
				}
			}
		private:
			// copy not allowed
			thread(const thread& other) {}
			void *result;
			bool resultCollected;
			bool isStarted;
		protected:
			pthread_t t;
			mutex cancelMutex;
			_shared_thread_cache cache;
		};
	}
}

#endif /* defined(__CMuscle__thread__) */

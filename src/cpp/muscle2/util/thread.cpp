//
//  thread.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 19-06-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include "thread.h"
#include "exception.hpp"

#include <cassert>
#include <signal.h>

namespace muscle {
	namespace util {
		static void *_run_cmuscle_util_thread(void *);
		
		thread::thread() : result(NULL), resultCollected(false), isStarted(false)
		{}
		
		thread::~thread()
		{
			cancel();
			getResult();
		}
		
		void thread::start()
		{
			cache.setThread(this);
			int ret;
			if ((ret = ::pthread_create(&t, NULL, &_run_cmuscle_util_thread, &cache)) != 0)
				throw muscle::muscle_exception("Could not create thread.", ret);
			isStarted = true;
		}
		
		void *_shared_thread_cache::run()
		{
			runner->runWithoutThread();
			runner->setDone();
			return runner->getRawResult();
		}
		
		void *thread::getResult()
		{
			if (!resultCollected)
			{
				if (isStarted) {
					::pthread_join(t, &result);
				} else {
					mutex_lock lock = waitMutex.acquire();
					while (!isDone()) { lock.wait(); }
				}
				if (isCancelled())
					deleteResult();

				resultCollected = true;
			}
			return result;
		}
				
		void thread::runWithoutThread()
		{
			beforeRun();
			if (!isCancelled()) result = run();
			// Notify done, whether actually run or not
			afterRun();
		}
		
		void thread::setDone()
		{
			if (isStarted) {
				cache.done = true;
			} else {
				mutex_lock lock = waitMutex.acquire();
				cache.done = true;
				lock.notify();
			}
		}
		
		void *_run_cmuscle_util_thread(void *t)
		{
			return ((_shared_thread_cache *)t)->run();
		}
	}
}

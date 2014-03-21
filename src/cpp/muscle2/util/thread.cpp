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
		
		thread::thread() : result(NULL), isStarted(false), resultCollected(false)
		{}
		
		thread::~thread()
		{
			if (!cache.stop_condition) {
				cancel();
				getResult();
			}
		}
		
		void thread::start()
		{
			if (isStarted)
				return;
			isStarted = true;
			cache.setThread(this);
			int ret;
			if ((ret = ::pthread_create(&t, NULL, &_run_cmuscle_util_thread, &cache)) != 0)
				throw muscle::muscle_exception("Could not create thread.", ret);
		}
		
		void *_shared_thread_cache::run()
		{
			return runner->run();
		}
		
		void thread::cancel()
		{
			cache.stop_condition = true; // Set to true outside of mutex
		}
		
		void *thread::getResult()
		{
			if (!resultCollected)
			{
				if (isStarted) {
					::pthread_join(t, &result);
				} else {
					mutex_lock lock = noThreadMutex.acquire();
					while (!cache.done) { lock.wait(); }
				}
				resultCollected = true;
			}
			return result;
		}
		
		void thread::runWithoutThread()
		{
			mutex_lock lock = noThreadMutex.acquire();
			if (!cache.stop_condition) {
				result = run();
			}
			// Notify done, whether actually run or not
			cache.done = true;
			lock.notify();
		}
		
		void *_run_cmuscle_util_thread(void *t)
		{
			_shared_thread_cache *thread_cache = (_shared_thread_cache *)t;
			void *result;
			if (thread_cache->stop_condition) {
				result = NULL;
			} else {
				result = thread_cache->run();
			}
			thread_cache->done = true;
			return result;
		}
	}
}

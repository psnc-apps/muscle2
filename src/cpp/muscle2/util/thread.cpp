//
//  thread.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 19-06-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include "thread.h"
#include "muscle2/util/exception.hpp"

#include <cassert>
#include <signal.h>

namespace muscle {
	namespace util {
		static void *_run_cmuscle_util_thread(void *);
		
		thread::thread() : result(0)
		{}
		
		thread::~thread()
		{
			if (!cache.stop_condition)
				cancel();
		}
		
		void thread::start()
		{
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
			cache.stop_condition = true;
			::pthread_join(t, &result);
		}
		
		void *thread::getResult()
		{
			if (!cache.stop_condition)
			{
				::pthread_join(t, &result);
				// No need for a mutex: this will only be called from outside the thread
				cache.stop_condition = true;
			}
			return result;
		}
		
		void *_run_cmuscle_util_thread(void *t)
		{
			_shared_thread_cache *thread_cache = (_shared_thread_cache *)t;
			if (thread_cache->stop_condition)
				return NULL;
			void *result = thread_cache->run();
			thread_cache->done = true;
			return result;
		}
	}
}

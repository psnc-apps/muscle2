//
//  thread.h
//  CMuscle
//
//  Created by Joris Borgdorff on 18-06-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#ifndef __CMuscle__mutex__
#define __CMuscle__mutex__

#include <pthread.h>

namespace muscle {
	namespace util {
		class mutex_lock
		{
		private:
			pthread_mutex_t *mut;
			pthread_cond_t *cond;
			// full delete only if *refs == 0
			int *refs;
		public:
			// create (refs = 1)
			mutex_lock(pthread_mutex_t *, pthread_cond_t *, bool onlyTry);
			// copy (refs++)
			mutex_lock(const mutex_lock& lock);
			// delete (refs--)
			~mutex_lock();
			
			bool isValid() const;
			
			void wait() const;
			void notify() const;
			void notifyAll() const;
		};
		
		class mutex
		{
		private:
			pthread_mutex_t *mut;
			pthread_cond_t *cond;
			// full delete only if *refs == 0
			int *refs;
		public:
			// create (refs = 1)
			mutex();
			// copy (refs++)
			mutex(const mutex& mut);
			// delete (refs--)
			virtual ~mutex();
			
			virtual mutex_lock acquire() const;
			virtual mutex_lock *tryLock() const;
		};
	}
}

#endif /* defined(__CMuscle__mutex__) */

//
//  rwmutex.h
//  CMuscle
//
//  Created by Joris Borgdorff on 15-07-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#ifndef __CMuscle__rwmutex__
#define __CMuscle__rwmutex__


#include <pthread.h>

namespace muscle {
    
    class rwmutex_lock
    {
    private:
        pthread_rwlock_t *mut;
        // full delete only if *refs == 0
        int *refs;
    public:
        // create (refs = 1)
        rwmutex_lock(bool read, pthread_rwlock_t *, bool onlyTry);
        // copy (refs++)
        rwmutex_lock(const rwmutex_lock& lock);
        // delete (refs--)
        ~rwmutex_lock();
        
        bool isValid() const;
    };
    
    class rwmutex
    {
    private:
        pthread_rwlock_t *mut;
        // full delete only if *refs == 0
        int *refs;
    public:
        // create (refs = 1)
        rwmutex();
        // copy (refs++)
        rwmutex(const rwmutex& mut);
        // delete (refs--)
        virtual ~rwmutex();
        
        virtual rwmutex_lock acquireRead() const;
        virtual rwmutex_lock acquireWrite() const;
        // Returns lock if succeeded and NULL if failed
        // Caller is responsible for deleting the lock.
        virtual rwmutex_lock *tryWrite() const;
  };
    
}

#endif /* defined(__CMuscle__rwmutex__) */

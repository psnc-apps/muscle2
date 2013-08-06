//
//  rwmutex.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 15-07-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include "rwmutex.h"
#include "muscle2/util/exception.hpp"

using namespace muscle;

rwmutex_lock::rwmutex_lock(bool read, pthread_rwlock_t *m, bool onlyTry) : mut(m)
{
    if (onlyTry)
    {
        int res;
        if (read)
            res = pthread_rwlock_tryrdlock(mut);
        else
            res = pthread_rwlock_trywrlock(mut);
        if (res != 0)
            mut = NULL;
    }
    else
    {
        if (read)
            pthread_rwlock_rdlock(mut);
        else
            pthread_rwlock_wrlock(mut);
    }
    
    refs = new int(1);
}

rwmutex_lock::rwmutex_lock(const rwmutex_lock &other) : mut(other.mut), refs(other.refs)
{
    ++(*refs);
}

rwmutex_lock::~rwmutex_lock()
{
    if (--(*refs) == 0)
    {
		if (mut)
			pthread_rwlock_unlock(mut);
		delete refs;
    }
}

bool rwmutex_lock::isValid() const
{
    return mut != NULL;
}

rwmutex::rwmutex()
{
    int ret;
    mut = new pthread_rwlock_t;
    if ((ret = pthread_rwlock_init(mut, NULL)) != 0)
    {
        delete mut;
        throw muscle::muscle_exception("Could not initialize mutex", ret);
    }
    
    refs = new int(1);
}

rwmutex::rwmutex(const rwmutex& other) : mut(other.mut), refs(other.refs)
{
    ++(*refs);
}

rwmutex::~rwmutex()
{
    if (--(*refs) == 0)
    {
        // Make sure that there is no active lock anymore
        pthread_rwlock_destroy(mut);
        delete mut;
        delete refs;
    }
}

rwmutex_lock rwmutex::acquireRead() const
{
    return rwmutex_lock(true, mut, false);
}

rwmutex_lock rwmutex::acquireWrite() const
{
    return rwmutex_lock(false, mut, false);
}

rwmutex_lock *rwmutex::tryWrite() const
{
    rwmutex_lock *lock = new rwmutex_lock(false, mut, true);

    if (lock->isValid())
        return lock;
    else {
        delete lock;
	    return NULL;
    }
}

//
//  thread.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 18-06-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include "mutex.h"
#include "muscle2/util/exception.hpp"

using namespace muscle::util;

mutex_lock::mutex_lock(pthread_mutex_t *m, pthread_cond_t *c, bool onlyTry) : mut(m), cond(c)
{
    refs = new int(1);
    if (onlyTry)
    {
	    if (pthread_mutex_trylock(mut) != 0)
            mut = NULL;
    }
    else
        pthread_mutex_lock(mut);
}

mutex_lock::mutex_lock(const mutex_lock &other) : mut(other.mut), cond(other.cond), refs(other.refs)
{
    ++(*refs);
}

mutex_lock::~mutex_lock()
{
    if (--(*refs) == 0)
    {
        pthread_mutex_unlock(mut);
        delete refs;
    }
}

void mutex_lock::wait() const
{
    pthread_cond_wait(cond, mut);
}

void mutex_lock::notify() const
{
    pthread_cond_signal(cond);
}

bool mutex_lock::isValid() const
{
    return mut != NULL;
}

mutex::mutex()
{
    int ret;
    mut = new pthread_mutex_t;
    if ((ret = pthread_mutex_init(mut, NULL)) != 0)
    {
        delete mut;
        throw muscle::muscle_exception("Could not initialize mutex", ret);
    }

    cond = new pthread_cond_t;
    if ((ret = pthread_cond_init(cond, NULL)) != 0)
    {
        pthread_mutex_destroy(mut);
        delete mut;
        delete cond;
        throw muscle::muscle_exception("Could not initialize mutex condition", ret);
    }
    refs = new int(1);
}

mutex::mutex(const mutex& other) : mut(other.mut), cond(other.cond), refs(other.refs)
{
    ++(*refs);
}

mutex::~mutex()
{
    if (--(*refs) == 0)
    {
        // Make sure that there is no active lock anymore
        pthread_mutex_destroy(mut);
        pthread_cond_destroy(cond);
        delete mut;
        delete cond;
        delete refs;
    }
}

mutex_lock mutex::acquire() const
{
    return mutex_lock(mut, cond, false);
}

mutex_lock *mutex::tryLock() const
{
    mutex_lock *lock = new mutex_lock(mut, cond, true);
    if (lock->isValid())
        return lock;
    else
    {
        delete lock;
        return NULL;
    }
}

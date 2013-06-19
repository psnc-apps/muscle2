//
//  thread.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 18-06-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include "mutex.h"
#include "exception.hpp"

using namespace muscle;

mutex_lock::mutex_lock(pthread_mutex_t *m, pthread_cond_t *c) : mutex(m), cond(c)
{
    pthread_mutex_lock(mutex);
    refs = new int(1);
}

mutex_lock::mutex_lock(const mutex_lock &other) : mutex(other.mutex), cond(other.cond), refs(other.refs)
{
    ++(*refs);
}

mutex_lock::~mutex_lock()
{
    if (--(*refs) == 0)
    {
        pthread_mutex_unlock(mutex);
        delete refs;
    }
}

void mutex_lock::wait()
{
    pthread_cond_wait(cond, mutex);
}

void mutex_lock::notify()
{
    pthread_cond_signal(cond);
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

mutex_lock mutex::acquire()
{
    return mutex_lock(mut, cond);
}
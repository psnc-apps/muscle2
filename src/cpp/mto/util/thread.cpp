//
//  thread.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 19-06-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include "thread.h"
#include "exception.hpp"

using namespace muscle;

void *_run_cmuscle_util_thread(void *t)
{
    muscle::thread *thread = (muscle::thread *)t;
    void *result = thread->run();
    thread->setDone();
    return result;
}


thread::thread() : stop_condition(false), done(false), result(0), mutex()
{
    int ret;
    if ((ret = ::pthread_create(&t, NULL, &_run_cmuscle_util_thread, this)) != 0)
        throw muscle::muscle_exception("Could not create thread.", ret);
}

thread::~thread()
{
    if (!isDone())
        stop();
    
    ::pthread_detach(t);
}

bool thread::isDone()
{
    mutex_lock lock = mutex.acquire();
    return done;
}

void thread::setDone()
{
    mutex_lock lock = mutex.acquire();
    done = true;
}

void thread::stop()
{
    mutex_lock lock = mutex.acquire();
    stop_condition = true;
}

bool thread::hasStopSignal()
{
    mutex_lock lock = mutex.acquire();
    return stop_condition;
}

void *thread::getResult()
{
    if (!result)
        ::pthread_join(t, &result);
    return result;
}

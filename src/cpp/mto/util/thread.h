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
#include <pthread.h>

void *_run_cmuscle_util_thread(void *);

namespace muscle {

class thread
{
public:
    thread();
    virtual ~thread();
    
    virtual void *run() = 0;
    // Non-blocking
    virtual bool isDone();
    virtual void setDone();
    virtual void stop();
    virtual bool hasStopSignal();
    // Blocking
    virtual void *getResult();
private:
    // copy not allowed
    thread(const thread& other) {}
    pthread_t t;
    bool done;
    bool stop_condition;
    mutex mutex;
    void *result;
};

}


#endif /* defined(__CMuscle__thread__) */

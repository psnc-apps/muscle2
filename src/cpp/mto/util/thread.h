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

namespace muscle {

    class thread;
    
    struct _shared_thread_cache
    {
        _shared_thread_cache() : stop_condition(false), done(false) {}
        
        // Called from owner
        void setThread(thread *t) { runner = t; }
        
        // Called from thread
        void *run();
        volatile bool done;
        volatile bool stop_condition;
    private:
        thread *runner;
    };
    
    class thread
    {
    public:
        thread();

        // Only allowed after the thread has terminated
        // Will call cancel (and block) if this is not the case
        virtual ~thread();
        
        virtual void *run() = 0;
        // Non-blocking
        virtual bool isDone() { return cache.done; } // Read from owner
        // Blocking
        virtual void *getResult(); // Read from owner, sets stop signal
        virtual void cancel(); // Called from owner, sets stop signal
    private:
        // copy not allowed
        thread(const thread& other) {}
        void *result;
    protected:
        pthread_t t;
        _shared_thread_cache cache;
        virtual void start();
    };
}

#endif /* defined(__CMuscle__thread__) */

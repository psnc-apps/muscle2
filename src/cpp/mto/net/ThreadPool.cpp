//
//  ThreadPool.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 3/20/14.
//  Copyright (c) 2014 Joris Borgdorff. All rights reserved.
//

#include "ThreadPool.h"
#include "muscle2/util/exception.hpp"

using namespace muscle::util;

ThreadPool::ThreadPool(int base_threads) : base_threads(base_threads)
{
	runners = new ThreadRunner*[base_threads];
	for (int i = 0; i < base_threads; i++) {
		runners[i] = new ThreadRunner();
	}
}

ThreadPool::~ThreadPool()
{
	for (int i = 0; i < base_threads; i++) {
		runners[i]->cancel();
	}
	for (int i = 0; i < base_threads; i++) {
		runners[i]->getResult();
		delete runners[i];
	}
	delete [] runners;
}

void ThreadPool::execute(thread *t)
{
	for (int i = 0; i < base_threads; i++) {
		if (!runners[i]->isBusy()) {
			runners[i]->setTask(t);
			return;
		}
	}
	
	// Only busy runners found; just start a new thread
	t->start();
}

void *ThreadRunner::run()
{
	while (!cache.stop_condition) {
		{
			mutex_lock lock = tmutex.acquire();
			while (task == NULL && !cache.stop_condition)
				lock.wait();
		}
		
		if (!cache.stop_condition) {
			task->runWithoutThread();
			mutex_lock lock = tmutex.acquire();
			task = NULL;
		}
	}
	return NULL;
}

ThreadRunner::~ThreadRunner()
{
	thread::cancel();
	mutex_lock lock = tmutex.acquire();
	if (task == NULL) {
		lock.notify();
	} else {
		task->cancel();
		task->getResult();
	}
}

void ThreadRunner::cancel()
{
	thread::cancel();
	mutex_lock lock = tmutex.acquire();
	if (task == NULL) {
		lock.notify();
	} else {
		task->cancel();
	}
}

bool ThreadRunner::isBusy()
{
	mutex_lock lock = tmutex.acquire();
	return task != NULL;
}

void ThreadRunner::setTask(thread *t) {
	mutex_lock lock = tmutex.acquire();
	if (task != NULL)
		throw muscle_exception("Cannot run two threads at once");
	
	task = t;
	lock.notify();
}

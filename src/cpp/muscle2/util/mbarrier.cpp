//
//  mbarrier.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 26-11-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include "mbarrier.h"
#include "csocket.h"

using namespace muscle::util;
using namespace muscle::net;

Barrier::Barrier(int num_procs) : num_procs(num_procs), signals(0), ep(NULL)
{
	start();
}

Barrier::~Barrier()
{
	mutex_lock lock = signalMutex.acquire();
	cache.stop_condition = true;
	signalMutex.acquire().notify();
	cancel();
}

void Barrier::signal()
{
	mutex_lock lock = signalMutex.acquire();
	signals++;
	lock.notify();
}

void Barrier::fillBuffer(char *buffer)
{
	{
		mutex_lock lock = signalMutex.acquire();
		while (ep == NULL)
			lock.wait();
	}
	
	ep->serialize(buffer);
}

size_t Barrier::createBuffer(char **buffer)
{
	const size_t len = endpoint::getSize();
	*buffer = new char[len];
	return len;
}

void *Barrier::run()
{
	const int total_csocks = num_procs - 1;
	int num_csocks = 0;

	// 1. Create server socket
	endpoint ep((uint16_t)0);
	socket_opts server_opts(num_procs);
	ep.resolve();
	ServerSocket *ssock = new CServerSocket(ep, NULL, server_opts);
	ssock->setBlocking(true);
	{
		mutex_lock lock = signalMutex.acquire();
		ep = ssock->getAddress();
		lock.notify();
	}

	// 2. Accept clients
	socket_opts client_opts;
	ClientSocket **socks = new ClientSocket*[total_csocks];
	while (!cache.stop_condition)
	{
		const char *msg = NULL;
		int err = 0;
		try {
			socks[num_csocks] = ssock->accept(client_opts);
			socks[num_csocks]->setBlocking(true);
			err = socks[num_csocks]->hasError();
		} catch (muscle_exception& ex) {
			msg = ex.what();
			err = ex.error_code;
		}
		if (err) {
			const char * const errstr = strerror(err);
			if (msg)
				logger::info("Barrier failed to accept socket: %s (%s)", msg, errstr);
			else
				logger::info("Barrier failed to accept socket: %s", errstr);
		} else if (++num_csocks == total_csocks) {
			break;
		}
	}
	// 3. Send notifications until stop_condition
	char data = 1;
	while (!cache.stop_condition)
	{
		{
			mutex_lock lock = signalMutex.acquire();
			while (signals == 0 && !cache.stop_condition)
				lock.wait();
			
			signals--;
		}
		
		for (int i = 0; i < total_csocks && !cache.stop_condition; i++) {
			socks[i]->send(&data, 1);
		}
	}
	// 4. Clean up (only for accepted sockets num_csocks)
	delete ssock;
	for (int i = 0; i < num_csocks; i++) {
		delete socks[i];
	}
	delete [] socks;
	return NULL;
}

BarrierClient::BarrierClient(const char *server)
{
	endpoint ep(server);
	socket_opts opts;
	opts.blocking_connect = true;
	csock = new CClientSocket(ep, NULL, opts);
}

BarrierClient::~BarrierClient()
{
	delete csock;
}

int BarrierClient::wait()
{
	char data;
	return csock->recv(&data, 1) == -1 ? -1 : 0;
}

//
//  mbarrier.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 26-11-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include "barrier.h"
#include "csocket.h"

using namespace muscle::util;
using namespace muscle::net;

Barrier::Barrier(const int num_clients) : num_clients(num_clients), signals(0), epBuffer(NULL)
{
	start();
}

Barrier::~Barrier()
{
	cancel();
	signalMutex.acquire().notify();
	getResult();
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
		while (epBuffer == NULL && !isCancelled())
			lock.wait();

		if (isCancelled())
			return;
	}
	
	memcpy(buffer, epBuffer, endpoint::getSize());
}

size_t Barrier::createBuffer(char **buffer)
{
	const size_t len = endpoint::getSize();
	*buffer = new char[len];
	return len;
}

void *Barrier::run()
{
	int num_csocks = 0;

	// 1. Create server socket
	socket_opts server_opts(num_clients);
	endpoint ep((uint16_t)0);
	ep.resolve();
	ServerSocket *ssock = new CServerSocket(ep, NULL, server_opts);
	ssock->setBlocking(true);
	
	// 2. Send server address to main thread
	char *buffer;
	createBuffer(&buffer);
	ssock->getAddress().serialize(buffer);
	{
		mutex_lock lock = signalMutex.acquire();
		epBuffer = buffer;
		lock.notify();
	}

	// 3. Accept clients
	socket_opts client_opts;
	ClientSocket **socks = new ClientSocket*[num_clients];
	while (!isCancelled())
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
		} else if (++num_csocks == num_clients) {
			break;
		}
	}
	// 4. Send notifications until stop_condition
	char data = 1;
	while (true)
	{
		{
			mutex_lock lock = signalMutex.acquire();
			while (signals == 0 && !isCancelled())
				lock.wait();
			
			if (signals == 0)
				break;

			signals--;
		}
		
		for (int i = 0; i < num_clients; i++) {
			socks[i]->send(&data, 1);
		}
	}
	// 5. Clean up (only for accepted sockets num_csocks)
	{
		mutex_lock lock = signalMutex.acquire();
		epBuffer = NULL;
	}
	delete [] buffer;
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
	ep.resolve();
	socket_opts opts;
	opts.blocking_connect = true;
	csock = new CClientSocket(ep, NULL, opts);
	csock->setBlocking(true);
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

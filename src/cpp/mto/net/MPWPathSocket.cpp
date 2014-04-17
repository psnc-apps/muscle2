//
//  MPWPathSocket.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 25-07-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include "MPWPathSocket.h"
#include "muscle2/util/async_service.h"

#include <sys/select.h>
#include <cassert>

#ifndef MSG_NOSIGNAL
#define MSG_NOSIGNAL 0
#endif

#ifndef min
#define min(X,Y) (((X) < (Y)) ? (X) : (Y))
#endif

using namespace muscle;
using namespace muscle::net;
using namespace muscle::util;
using namespace std;

void MPWPathClientSocket::clear(MPWPathSendRecvThread **commThreads)
{
	for (int i = 0; i < num_threads; i++) {
		if (commThreads[i] == NULL)
			continue;
		
		commThreads[i]->cancel();
	}

	for (int i = 0; i < num_threads; i++) {
		if (commThreads[i] == NULL)
			continue;
		
		commThreads[i]->getResult();
		delete commThreads[i];
		commThreads[i] = NULL;
	}
}

MPWPathClientSocket::MPWPathClientSocket(ThreadPool *tpool_, endpoint& ep, async_service *server, const socket_opts& opts, int num_threads) : msocket(ep, server), num_threads(num_threads), num_channels(opts.max_connections), channels(NULL), sendData(NULL), recvData(NULL), MPWPathSocket(tpool_)
{
	sendIndexes = new size_t[num_channels + 1];
	sendIndexes[0] = 0;
	recvIndexes = new size_t[num_channels + 1];
	recvIndexes[0] = 0;
	sendThreads = new MPWPathSendRecvThread*[num_threads]();
	recvThreads = new MPWPathSendRecvThread*[num_threads]();
	fds = new int[num_channels];
	
	pacing_time = duration(0, 1);
	connector = new MPWPathConnectThread(num_channels, this, opts);
	tpool->execute(connector);
}

MPWPathClientSocket::MPWPathClientSocket(ThreadPool *tpool_, endpoint& ep, async_service *server, const socket_opts& opts, int num_threads, int num_channels, CClientSocket ** channels_) : msocket(ep, server), num_threads(num_threads), num_channels(num_channels), channels(channels_), sendData(NULL), recvData(NULL), MPWPathSocket(tpool_)
{
	sendIndexes = new size_t[num_channels + 1];
	sendIndexes[0] = 0;
	recvIndexes = new size_t[num_channels + 1];
	recvIndexes[0] = 0;
	sendThreads = new MPWPathSendRecvThread*[num_threads]();
	recvThreads = new MPWPathSendRecvThread*[num_threads]();
	
	fds = new int[num_channels];
	for (int i = 0; i < num_channels; i++)
		fds[i] = channels[i]->getReadSock();
	
	pacing_time = duration(0, 1000);
	addReadReady(-1);
	addWriteReady(-1);
}

MPWPathClientSocket::~MPWPathClientSocket()
{
	// Delete threads
	clear(sendThreads);
	clear(recvThreads);
	delete [] sendThreads;
	delete [] recvThreads;
	
	// Delete channels
	if (channels != NULL) {
		for (int i = 0; i < num_channels; i++) {
			delete channels[i];
		}
		delete [] channels;
	}
	delete [] fds;
	delete [] sendIndexes;
	delete [] recvIndexes;
}


int MPWPathClientSocket::hasError()
{
	if (connector) {
		channels = (CClientSocket **)connector->getResult();
		delete connector;
		connector = NULL;
		
		if (channels == NULL) {
			return ECONNREFUSED;
		} else {
			for (int i = 0; i < num_channels; i++)
				fds[i] = channels[i]->getReadSock();
			addReadReady(-1);
		}
	}
	return 0;
}

ssize_t MPWPathClientSocket::recv(void* s, size_t size)
{
	return runInThread(false, (char *)s, size);
}

ssize_t MPWPathClientSocket::send(const void* s, size_t size)
{
	return runInThread(true, (char *)s, size);
}

ssize_t MPWPathClientSocket::runInThread(bool send, char *data, size_t size)
{
	char** currentData;
	size_t **currentIndexes;
	MPWPathSendRecvThread **currentThreads;
	int *currentNumThreads, *currentNumThreadsFinished;
	ssize_t ret = 0;
	
	if (send) {
		currentData = &sendData;
		currentIndexes = &sendIndexes;
		currentThreads = sendThreads;
		currentNumThreads = &numSendThreads;
		currentNumThreadsFinished = &numSendThreadsFinished;
	} else {
		currentData = &recvData;
		currentIndexes = &recvIndexes;
		currentThreads = recvThreads;
		currentNumThreads = &numRecvThreads;
		currentNumThreadsFinished = &numRecvThreadsFinished;
	}
	
	if (*currentData == NULL)
	{
		// Needs to go before any thread is spawned
		if (send)
			removeWriteReady();
		else
			removeReadReady();

		if (send ? hasWriteReady() : hasReadReady()) {
			logger::warning("Socket control out of bounds");
		}
		const int use_channels = min(num_channels, 1 + int(size / (2*1024)));
		
		const size_t sz_per_channel = size / use_channels;
		const int odd_sz_per_channel = size % use_channels;
		
		// Zero is set in constructor
		for (int i = 1; i <= use_channels; ++i)
		{
			(*currentIndexes)[i] = (i <= odd_sz_per_channel)
			           ? (*currentIndexes)[i - 1] + sz_per_channel + 1
			           : (*currentIndexes)[i - 1] + sz_per_channel;
		}
		
		
		*currentNumThreads = min(num_threads, use_channels);
		*currentNumThreadsFinished = 0;
		const int ch_per_thread = use_channels / *currentNumThreads;
		const int odd_channels = use_channels % *currentNumThreads;
		
		int channel = 0;
		for (int i = 0; i < *currentNumThreads; ++i)
		{
			const int ch_in_thread = (i < odd_channels)
			                       ? ch_per_thread + 1
			                       : ch_per_thread;
			
			currentThreads[i] = new MPWPathSendRecvThread(i, channel, send, data, &(*currentIndexes)[channel], &fds[channel], ch_in_thread, pacing_time, this);
			tpool->execute(currentThreads[i]);
			channel += ch_in_thread;
		}
		*currentData = data;
	} else if (*currentData == data) {
		int threadNum = send ? removeWriteReady() : removeReadReady();
		
		int *resPtr = (int *)(currentThreads[threadNum])->getResult();
		int res = resPtr == NULL ? -1 : *resPtr;
		
		delete currentThreads[threadNum];
		currentThreads[threadNum] = NULL;
		
		(*currentNumThreadsFinished)++;
		
		// final call
		if (*currentNumThreadsFinished == *currentNumThreads || res == -1) {
			if (res == -1) {
				clear(currentThreads);
				if (send)
					while (hasWriteReady()) { removeWriteReady(); }
				else
					while (hasReadReady()) { removeReadReady(); }
				
				ret = -1;
			} else {
				ret = size;
			}
			// we can accept a new data packet
			*currentData = NULL;
			if (send)
				addWriteReady(-1);
			else
				addReadReady(-1);
		}
		delete resPtr;
	}
	
	return ret;
}

void MPWPathClientSocket::async_cancel()
{
	if (server != NULL)
		server->erase_socket(sockfd, -1, readableWriteFd);
}

static void serializeInt(unsigned char *data, int value) {
	data[0] = (value >> 24) & 0xff;
	data[1] = (value >> 16) & 0xff;
	data[2] = (value >>  8) & 0xff;
	data[3] =  value        & 0xff;	
}
static int deserializeInt(unsigned char *data) {
	return (data[0] << 24) | (data[1] << 16) | (data[2] << 8) | data[3];
}

// Connect to a MPWPath server socket
// In the first connect, connect and send the number of channels we want to connect to,
// and receive the connection ID.
// In the next connects, send the connection ID
void *MPWPathConnectThread::run()
{
	opts.blocking_connect = true;
	endpoint ep = employer->getAddress();
	async_service *server = employer->getServer();
	unsigned char header[8];
	CClientSocket **sockets = new CClientSocket*[num_channels]();
		
	serializeInt(header, num_channels);
	serializeInt(header+4, 0);
	
	int i;
	for (i = 0; i < num_channels && !isCancelled(); i++) {
		try {
			sockets[i] = new CClientSocket(ep, server, opts);

			sockets[i]->setBlocking(true);
			if (sockets[i]->send(header, 8) == -1) {
				logger::fine("Could not initialize connection with %s", ep.getHost().c_str());
				cancel();
			} else if (i == 0) {
				if (sockets[i]->recv(header+4, 4) == -1) {
					logger::fine("Busy server, could not initialize connection with %s", ep.getHost().c_str());
					cancel();
				} else if (deserializeInt(header+4) == -1) {
					logger::fine("Number of streams requested %d is too large for server", num_channels);
					cancel();
				}
			}
			sockets[i]->setBlocking(false);
		} catch (...) {
			// set error condition
			cancel();
		}
	}
	
	return sockets;
}

void MPWPathConnectThread::deleteResult(void *result)
{
	CClientSocket **sockets = (CClientSocket **)result;
	for (int i = 0; i < num_channels; i++) {
		delete sockets[i];
	}
	delete [] sockets;
}

void MPWPathConnectThread::afterRun()
{
	employer->addWriteReady(-1);
}

// Accepts target_size sockets from on a single server socket, using a unique ID.
//
// The first connector sends ID 0, it gets a response with the ID number, the next connectors
// use the ID that was sent back as their connection ID
//
// Edge cases:
// * Number of requested sockets is too large.
//   Response: -1 is sent back as the ID number
// * Another connect starts while one is busy.
//   Response: disconnect the socket without sending a response.
// * Another connect timed out but is connecting again with another ID
//   Response: disconnect socket
// Each connecting socket is sent back a response
//
void *MPWPathAcceptThread::run()
{
	unsigned char header[8];
	
	target_size = 1;
	int current_size = 0;
	CClientSocket **sockets = NULL;
	
	while (current_size < target_size && !isCancelled())
	{
		mtime start = mtime::now();
		CClientSocket *sock = (CClientSocket *)serversock->accept(opts);
		sock->setBlocking(true);
		
		if (sock->recv(header, 8) == -1) {
			delete sock;
			cancel();
			break;
		}
		
		const int num_channels = deserializeInt(header);
		const int sent_id = deserializeInt(header+4);
		
		// start new range of sockets if the current is taking too long
		if (sent_id != use_id &&
			sockets != NULL	&&
			start.duration_since() > timeout) {
			for (int i = 0; i < current_size; i++) {
				delete sockets[i];
			}
			delete [] sockets;
			sockets = NULL;
			current_size = 0;
		}
		
		if (sockets == NULL) {
			// Another accept is running through this one
			if (sent_id != 0) {
				delete sock;
				continue;
			}
			
			target_size = num_channels;
			
			// Signal that the number of connections
			// requested is too large, and continue
			// with another accept.
			if (target_size > max_connections) {
				serializeInt(header+4, -1);
				sock->send(header+4, 4);
				delete sock;
				continue;
			}
			
			serializeInt(header+4, use_id);
			if (sock->send(header+4, 4) == -1) {
				delete sock;
				continue;
			}

			sockets = new CClientSocket*[target_size];
		}
		// another login attempt
		else if (sent_id != use_id) {
			// ignore new range of sockets
			delete sock;
			continue;
		} else if (target_size != num_channels) {
			delete sock;
			cancel();
			break;
		}
		
		sock->setBlocking(false);
		sockets[current_size++] = sock;
	}
	
	return sockets;
}

void MPWPathAcceptThread::afterRun()
{
	employer->addReadReady(-1);
}

void MPWPathAcceptThread::deleteResult(void *result)
{
	CClientSocket **sockets = (CClientSocket **)result;
	for (int i = 0; i < target_size; i++) {
		delete sockets[i];
	}
	delete [] sockets;
}

MPWPathServerSocket::MPWPathServerSocket(ThreadPool *tpool, endpoint& ep, async_service *server, const socket_opts &opts) : msocket(ep, server), ServerSocket(opts), MPWPathSocket(tpool), currentId(1), num_threads(opts.max_connections), copts(opts)
{
	// Allow for two connecting parties at a time
	copts.max_connections = num_threads*2;
	copts.blocking_connect = true;
	serversock = new CServerSocket(ep, server, copts);
	acceptor = new MPWPathAcceptThread(copts, serversock, currentId++, opts.max_connections, this, duration(1,0));
}

ClientSocket *MPWPathServerSocket::accept(const socket_opts &opts)
{
	MPWPathClientSocket *csock = NULL;
	CClientSocket **sockets = (CClientSocket **)acceptor->getResult();
	
	if (sockets != NULL) {
		csock = new MPWPathClientSocket(tpool, address, server, opts, num_threads, acceptor->target_size, sockets);
	}
	
	removeReadReady();
	delete acceptor;
	acceptor = new MPWPathAcceptThread(copts, serversock, currentId++, opts.max_connections, this, duration(1,0));
	
	return csock;
}

void MPWPathServerSocket::async_cancel()
{
	if (server != NULL)
		server->erase_listen(sockfd);
}

MPWPathSocketFactory::MPWPathSocketFactory(async_service * service, int num_threads_, bool useThreadPool) : SocketFactory(service), num_threads(num_threads_)
{
	tpool = new ThreadPool(useThreadPool ? num_threads * 2 : 0);
}

MPWPathSocketFactory::~MPWPathSocketFactory()
{
	delete tpool;
}

ClientSocket *MPWPathSocketFactory::connect(endpoint& ep, const socket_opts &opts)
{
	return new MPWPathClientSocket(tpool, ep, service, opts, num_threads);
}

ServerSocket *MPWPathSocketFactory::listen(endpoint& ep, const socket_opts &opts)
{
	return new MPWPathServerSocket(tpool, ep, service, opts);
}


MPWPathSendRecvThread::MPWPathSendRecvThread(int thread_num, int channel_num, bool send_, char *data_, size_t *indexes_, int *fds_, int num_channels_, const duration& pacing_time_, MPWPathClientSocket *employer_) : thread_num(thread_num), channel_num(channel_num), send(send_), data(data_), indexes(indexes_), num_channels(num_channels_), fds(fds_), employer(employer_), pacing_time(pacing_time_)
{
	commDone = new bool[num_channels]();
}

MPWPathSendRecvThread::~MPWPathSendRecvThread()
{
	delete [] commDone;
}

int MPWPathSendRecvThread::select(const duration &timeout)
{
	struct timeval t = timeout.timeval();
	
	fd_set opset, errset;
	FD_ZERO(&opset); FD_ZERO(&errset);
	int max = 0;
	for (int i = 0; i < num_channels; i++)
	{
		if (!commDone[i]) {
			FD_SET(fds[i], &opset);
			FD_SET(fds[i], &errset);
			if (fds[i] > max)
				max = fds[i];
		}
	}
	
	int res;
	if (send)
		res = ::select(max + 1, 0, &opset, &errset, &t);
	else
		res = ::select(max + 1, &opset, 0, &errset, &t);
	
	if (res > 0)
	{
		for (int i = 0; i < num_channels; i++)
		{
			if (!commDone[i]) {
				if (FD_ISSET(fds[i], &opset))
					return i + 1;
				else if (FD_ISSET(fds[i], &errset))
					return -2;
			}
		}
		return -3; // should not reach here
	} else if (res == 0) {
		return 0;
	} else {
		return -1;
	}
}

void *MPWPathSendRecvThread::run()
{
	size_t sz_communicated[num_channels];
	memset(sz_communicated, 0, sizeof(sz_communicated));
	
	int num_done = 0;
	int *ret = new int(0);
	ssize_t commResult;
	duration timeout(1, 0);
	
	while (num_done < num_channels && !isCancelled())
	{
		muscle::util::mtime start_time = muscle::util::mtime::now();
		
		int channel = select(timeout);
		
		if (channel < 0) {
			const char * const str = strerror(errno);
			int channel_to = channel_num + num_channels;
			logger::severe("Cannot select on channel %d-%d: %s", channel_num, channel_to, str);
			*ret = -1;
			break;
		} else if (channel > 0) {
			// Set channel index back to zero-based
			channel--;
			const size_t ifd = indexes[channel];
			const size_t totalSize = indexes[channel + 1] - ifd;
			const size_t sz_comm = sz_communicated[channel];
			
			if (send) {
				commResult = ::send(fds[channel], data + ifd + sz_comm, totalSize - sz_comm, MSG_NOSIGNAL);
				if (commResult < 0) {
					const char * const str = strerror(errno);
					int channel_str = channel_num + channel;
					logger::severe("Cannot send on channel %d (fd %d, size %zu): %s", channel_str, fds[channel], totalSize, str);
					*ret = -1;
					break;
				}
			} else {
				commResult = ::recv(fds[channel], data + ifd + sz_comm, totalSize - sz_comm, MSG_NOSIGNAL);
				if (commResult <= 0) {
					int channel_str = channel_num + channel;
					if (commResult == 0) {
						logger::severe("Cannot receive on channel %d (fd %d, size %zu): other side closed socket", channel_str, fds[channel], totalSize);
					} else {
						const char * const str = strerror(errno);
						logger::severe("Cannot receive on channel %d (fd %d, size %zu): %s", channel_str, fds[channel], totalSize, str);
					}
					*ret = -1;
					break;
				}
			}
			
			sz_communicated[channel] += commResult;
			if (sz_communicated[channel] == totalSize) {
				commDone[channel] = true;
				++num_done;
			} else {
				duration sleeping = pacing_time - start_time.duration_since();
				if (sleeping.useconds() > 0) {
					logger::finer("Pacing to sleep for %s on thread %d", sleeping.str().c_str(), thread_num);
					sleeping.sleep();
				}
			}
		}
	}
	
	return ret;
}

void MPWPathSendRecvThread::afterRun()
{	
	if (send) {
		logger::finest("Finished MPWPath send with thread %d", thread_num);
		employer->addWriteReady((char)thread_num);
	} else {
		logger::finest("Finished MPWPath receive with thread %d", thread_num);
		employer->addReadReady((char)thread_num);
	}
}

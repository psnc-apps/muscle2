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

#ifndef MSG_NOSIGNAL
#define MSG_NOSIGNAL SO_NOSIGPIPE
#endif

#define max(X,Y) (((X) > (Y)) ? (X) : (Y))
#define min(X,Y) (((X) < (Y)) ? (X) : (Y))

static int Socket_select_mpwpath(int rs, int ws, int mask, int timeout_s, int timeout_u);

using namespace muscle;
using namespace muscle::net;
using namespace muscle::util;
using namespace std;

const int MPWPathSocket::RDMASK = 1;
const int MPWPathSocket::WRMASK = 2;

MPWPathSocket::MPWPathSocket()
{
	int fd[2];
	if (pipe(fd) == -1)
		throw muscle_exception("Could not create MPWide socket pipe", errno);
	
	sockfd = fd[0];
	writableReadFd = fd[1];
	
	if (pipe(fd) == -1)
		throw muscle_exception("Could not create MPWide socket pipe", errno);
	
	readableWriteFd = fd[0];
	writableWriteFd = fd[1];
}

MPWPathSocket::~MPWPathSocket()
{
	close(sockfd);
	close(writableReadFd);
	close(readableWriteFd);
	close(writableWriteFd);
}

int MPWPathClientSocket::getWriteSock() const
{
	return readableWriteFd;
}

void MPWPathSocket::setReadReady() const
{
	char c = 1;
	::write(writableReadFd, &c, 1);
}
void MPWPathSocket::unsetReadReady() const
{
	char c;
	::read(sockfd, &c, 1);
}

bool MPWPathSocket::isReadReady() const
{
	int result = Socket_select_mpwpath(sockfd, 0, WRMASK, 0, 1);
	return result >= 0 && (result&RDMASK)==RDMASK;
}
void MPWPathClientSocket::setWriteReady() const
{
	char c = 1;
	::write(writableWriteFd, &c, 1);
}
void MPWPathClientSocket::unsetWriteReady() const
{
	char c;
	::read(readableWriteFd, &c, 1);
}

bool MPWPathClientSocket::isWriteReady() const
{
	int result = Socket_select_mpwpath(readableWriteFd, 0, WRMASK, 0, 1);
	return result >= 0 && (result&RDMASK)==RDMASK;
}


void MPWPathClientSocket::clear(const bool send)
{
	MPWPathSendRecvThread **saveThreads = send ? sendThreads : recvThreads;
	
	for (int i = 0; i < num_threads; i++) {
		if (saveThreads[i] == NULL)
			break;
		
		saveThreads[i]->cancel();
		const int * const res = (const int *)saveThreads[i]->getResult();
		delete saveThreads[i];
		saveThreads[i] = NULL;
		if (res) delete res;
	}
}

MPWPathSendRecvThread::MPWPathSendRecvThread(bool send_, char *data_, size_t *indexes_, int *fds_, int num_channels_, const duration& pacing_time_, MPWPathClientSocket *employer_) : send(send_), data(data_), indexes(indexes_), num_channels(num_channels_), fds(fds_), employer(employer_), pacing_time(pacing_time_)
{
	commDone = new bool[num_channels]();
	start();
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
	size_t *sz_communicated = new size_t[num_channels]();
	int num_done = 0;
	int *ret = new int(0);
	ssize_t commResult;
	duration timeout(10, 0);
	
	while (num_done < num_channels && !isDone())
	{
//		muscle::util::mtime start_time = muscle::util::mtime::now();
		
		int channel = select(timeout);
		
//		(pacing_time - start_time.duration_since()).sleep();
		
		if (channel < 0) {
			*ret = -1;
			break;
		} else if (channel > 0) {
			// Set channel index back to zero-based
			channel--;
			const size_t ifd = indexes[channel];
			const size_t totalSize = indexes[channel + 1] - ifd;
			size_t& sz_comm = sz_communicated[ifd];
			
			if (send)
				commResult = ::send(fds[channel], data + ifd + sz_comm, totalSize - sz_comm, MSG_NOSIGNAL);
			else
				commResult = ::recv(fds[channel], data + ifd + sz_comm, totalSize - sz_comm, MSG_NOSIGNAL);
			
			if (commResult < 0 || (!send && commResult == 0)) {
				*ret = -1;
				break;
			}
			
			sz_comm += commResult;
			if (sz_comm == totalSize) {
				commDone[channel] = true;
				++num_done;
			}
		}
	}
	
	delete [] sz_communicated;

	if (send)
		employer->setWriteReady();
	else
		employer->setReadReady();

	return ret;
}

MPWPathClientSocket::MPWPathClientSocket(endpoint& ep, async_service *server, const socket_opts& opts, int num_threads) : msocket(ep, server), num_threads(num_threads), num_channels(opts.max_connections), channels(NULL)
{
	indexes = new size_t[num_channels + 1];
	indexes[0] = 0;
	fds = new int[num_channels];
	sendThreads = new MPWPathSendRecvThread*[num_threads]();
	recvThreads = new MPWPathSendRecvThread*[num_threads]();
	
	connector = new MPWPathConnectThread(num_channels, this, opts);
}

MPWPathClientSocket::MPWPathClientSocket(endpoint& ep, async_service *server, const socket_opts& opts, int num_threads, int num_channels, CClientSocket ** channels_) : msocket(ep, server), num_threads(num_threads), num_channels(num_channels), channels(channels_)
{
	indexes = new size_t[num_channels + 1];
	indexes[0] = 0;
	fds = new int[num_channels];
	sendThreads = new MPWPathSendRecvThread*[num_threads]();
	recvThreads = new MPWPathSendRecvThread*[num_threads]();
	
	for (int i = 0; i < num_channels; i++)
		fds[i] = channels[i]->getReadSock();
}

int MPWPathClientSocket::hasError()
{
	if (connector && (connector->isDone() || isWriteReady())) {
		channels = (CClientSocket **)connector->getResult();
		delete connector;
		connector = NULL;

		if (channels == NULL) {
			return ECONNREFUSED;
		} else {
			for (int i = 0; i < num_channels; i++)
				fds[i] = channels[i]->getReadSock();
			setReadReady();
		}
	}
	return 0;
}

MPWPathClientSocket::~MPWPathClientSocket()
{
	// Delete threads
	clear(true);
	clear(false);
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
	delete [] indexes;
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
	MPWPathSendRecvThread **saveThreads = send ? sendThreads : recvThreads;

	if (saveThreads[0] == NULL)
	{
		const int use_channels = min(num_channels, 1 + int(size / (2*1024)));
		
		const size_t sz_per_channel = size / use_channels;
		const int odd_sz_per_channel = size % use_channels;
		
		// Zero is set in constructor
		for (int i = 1; i <= use_channels; ++i)
		{
			indexes[i] = (i <= odd_sz_per_channel)
			           ? indexes[i - 1] + sz_per_channel + 1
			           : indexes[i - 1] + sz_per_channel;
		}
		
		const int use_threads = min(num_threads, use_channels);
		const int ch_per_thread = use_channels / use_threads;
		int odd_channels = use_channels % use_threads;
		
		int channel = 0;
		for (int i = 0; i < use_threads; ++i)
		{
			const int ch_in_thread = (i < odd_channels)
			                       ? ch_per_thread + 1
			                       : ch_per_thread;
			
			saveThreads[i] = new MPWPathSendRecvThread(send, data, &indexes[channel], &fds[channel], ch_in_thread, pacing_time, this);
		}
		if (send)
			unsetWriteReady();
		else
			unsetReadReady();
	} else if (saveThreads[0]->data == data) {
		bool hasErr = false;
		for (int i = 0; i < num_threads; i++) {
			if (saveThreads[i] == NULL)
				break;
			
			if (saveThreads[i]->isDone()) {
				int *res = (int *)(saveThreads[i])->getResult();
				delete saveThreads[i];
				saveThreads[i] = NULL;
				
				if (*res == -1) {
					hasErr = true;
				}
				delete res;
			}
		}
		
		if (hasErr) {
			clear(send);
			return -1;
		}
		else if (saveThreads[0] == NULL)
			return size;
	}
	
	return 0;
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
	
	for (int i = 0; i < num_channels && !isDone(); i++) {
		try {
			sockets[i] = new CClientSocket(ep, server, opts);

			sockets[i]->setBlocking(true);
			if (sockets[i]->send(header, 8) == -1)
				throw muscle_exception("Could not initialize connection with " + ep.getHost());
			
			if (i == 0) {
				if (sockets[i]->recv(header+4, 4) == -1)
					throw muscle_exception("Busy server, could not initialize connection with " + ep.getHost());
				if (deserializeInt(header+4) == -1) {
					stringstream ss;
					ss << "Number of streams requested " << num_channels << " is too large for server ";
					ss << ep.getHost();
					throw muscle_exception(ss.str());
				}
			}
			sockets[i]->setBlocking(false);
		} catch (const muscle_exception& ex) {
			return clear(sockets);
		}
	}
	
	employer->setWriteReady();
	
	return sockets;
}

// Delete all sockets, including the last acquired one, and mark the employer ready
void *MPWPathConnectThread::clear(const CClientSocket * const * const sockets)
{
	for (int i = 0; i < num_channels && sockets[i] != NULL; i++) {
		delete sockets[i];
	}
	delete sockets;

	employer->setWriteReady();
	return NULL;
}

// Delete all sockets, including the last acquired one
void *MPWPathAcceptThread::clear(const CClientSocket * const latest_sock, const CClientSocket * const * const sockets, const int current_size)
{
	delete latest_sock;
	if (sockets != NULL) {
		for (int i = 0; i < current_size; i++) {
			delete sockets[i];
		}
		delete sockets;
	}
	return NULL;
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
	
	while (current_size < target_size && !isDone())
	{
		CClientSocket *sock = (CClientSocket *)serversock->accept(opts);
		sock->setBlocking(true);
		
		if (sock->recv(header, 8) == -1)
			return clear(sock, sockets, current_size);
		
		const int num_channels = deserializeInt(header);
		const int sent_id = deserializeInt(header+4);
		
		// Another accept is running through this one
		if (sent_id != (sockets == NULL ? 0 : use_id)) {
			delete sock;
			continue;
		}
		
		if (sockets == NULL) {
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
			if (sock->send(header+4, 4) == -1)
				return clear(sock, sockets, current_size);

			sockets = new CClientSocket*[target_size];
		}
		else if (target_size != num_channels)
			return clear(sock, sockets, current_size);
		
		sock->setBlocking(false);
		sockets[current_size++] = sock;
	}
	
	employer->setReadReady();
	
	return sockets;
}

MPWPathServerSocket::MPWPathServerSocket(endpoint& ep, async_service *server, const socket_opts &opts) : msocket(ep, server), ServerSocket(opts), currentId(1), num_threads(opts.max_connections), copts(opts)
{
	// Allow for two connecting parties at a time
	copts.max_connections = num_threads*2;
	copts.blocking_connect = true;
	serversock = new CServerSocket(ep, server, copts);
	acceptor = new MPWPathAcceptThread(copts, serversock, currentId++, opts.max_connections, this);
}

ClientSocket *MPWPathServerSocket::accept(const socket_opts &opts)
{
	MPWPathClientSocket *csock = NULL;
	CClientSocket **sockets = (CClientSocket **)acceptor->getResult();
	
	if (sockets != NULL) {
		csock = new MPWPathClientSocket(address, server, opts, num_threads, acceptor->target_size, sockets);
		delete [] sockets;
	}
	
	unsetReadReady();
	delete acceptor;
	acceptor = new MPWPathAcceptThread(copts, serversock, currentId++, opts.max_connections, this);
	
	return csock;
}

void MPWPathServerSocket::async_cancel()
{
	if (server != NULL)
		server->erase_listen(sockfd);
}

ClientSocket *MPWPathSocketFactory::connect(endpoint& ep, const socket_opts &opts)
{
	return new MPWPathClientSocket(ep, service, opts, num_threads);
}

ServerSocket *MPWPathSocketFactory::listen(endpoint& ep, const socket_opts &opts)
{
	return new MPWPathServerSocket(ep, service, opts);
}

/**
 Returns:
 -1 on error
 0 if no access.
 MPWIDE_SOCKET_RDMASK if read.
 MPWIDE_SOCKET_WRMASK if write.
 MPWIDE_SOCKET_RDMASK|MPWIDE_SOCKET_WRMASK if both.
 int mask is...
 0 if we check for read & write.
 MPWIDE_SOCKET_RDMASK if we check for write only.
 MPWIDE_SOCKET_WRMASK if we check for read only.
 */
int Socket_select_mpwpath(int rs, int ws, int mask, int timeout_s, int timeout_u)
{
    struct timeval timeout;
    timeout.tv_sec  = timeout_s;
    timeout.tv_usec = timeout_u;
	
  	fd_set *rsock = 0, *wsock = 0;
	fd_set rfd, wfd;
	
	if ((mask&MPWPathSocket::RDMASK) != MPWPathSocket::RDMASK) {
		rsock = (fd_set *)&rfd;
		FD_ZERO(rsock);
		FD_SET(rs,rsock);
	}
	if ((mask&MPWPathSocket::WRMASK) != MPWPathSocket::WRMASK) {
		wsock = (fd_set *)&wfd;
		FD_ZERO(wsock);
		FD_SET(ws,wsock);
	}
	
	/* args: FD_SETSIZE,writeset,readset,out-of-band sent, timeout*/
	const int ok = select(max(rs, ws)+1, rsock, wsock, (fd_set *)0, &timeout);
	
	if(ok > 0) {
		return (rsock && FD_ISSET(rs,rsock) ? MPWPathSocket::RDMASK : 0)
		| (wsock && FD_ISSET(ws,wsock) ? MPWPathSocket::WRMASK : 0);
	} else if (ok == 0 || errno == EINTR) { // Interruptions don't matter
		return 0;
	} else {
		return -1;
	}
}


//
//  DecoupledSelectSocket.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 3/18/14.
//  Copyright (c) 2014 Joris Borgdorff. All rights reserved.
//

#include "DecoupledSelectSocket.h"
#include "muscle2/util/async_service.h"
#include <sys/select.h>
#include <unistd.h>
#include <errno.h>

using namespace muscle::net;

const int DecoupledSelectSocket::RDMASK = 1;
const int DecoupledSelectSocket::WRMASK = 2;

#ifndef max
#define max(X,Y) (((X) > (Y)) ? (X) : (Y))
#endif

DecoupledSelectSocket::DecoupledSelectSocket() : msocket((async_service *)0)
{
	int fd[2];
	if (pipe(fd) == -1) throw muscle_exception("Could not create MPWide socket pipe", errno);
	sockfd = fd[0];
	writableReadFd = fd[1];
	
	if (pipe(fd) == -1) throw muscle_exception("Could not create MPWide socket pipe", errno);
	readableWriteFd = fd[0];
	writableWriteFd = fd[1];
}

DecoupledSelectSocket::~DecoupledSelectSocket()
{
	::close(sockfd);
	::close(writableReadFd);
	::close(readableWriteFd);
	::close(writableWriteFd);
}

int DecoupledSelectSocket::getWriteSock() const
{
	return readableWriteFd;
}

void DecoupledSelectSocket::addReadReady(char thread) const
{
	if (::write(writableReadFd, &thread, 1) == -1)
		throw muscle_exception("Cannot write to socket read-control pipe");
}
char DecoupledSelectSocket::removeReadReady() const
{
	char thread;
	if (::read(sockfd, &thread, 1) <= 0)
		throw muscle_exception("Cannot read from socket read-control pipe");
	return thread;
}

void DecoupledSelectSocket::addWriteReady(char thread) const
{
	if (::write(writableWriteFd, &thread, 1) == -1)
		throw muscle_exception("Cannot write to socket write-control pipe");
}
char DecoupledSelectSocket::removeWriteReady() const
{
	char thread;
	if (::read(readableWriteFd, &thread, 1) <= 0)
		throw muscle_exception("Cannot read from socket write-control pipe");
	return thread;
}

bool DecoupledSelectSocket::hasWriteReady() const
{
	int result = select(readableWriteFd, 0, WRMASK, 0, 1);
	return result > 0 && (result&RDMASK)==RDMASK;
}

bool DecoupledSelectSocket::hasReadReady() const
{
	int result = select(sockfd, 0, WRMASK, 0, 1);
	return result > 0 && (result&RDMASK)==RDMASK;
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
int DecoupledSelectSocket::select(const int rs, const int ws, const int mask, const int timeout_s, const int timeout_u)
{
    struct timeval timeout;
    timeout.tv_sec  = timeout_s;
    timeout.tv_usec = timeout_u;
	
  	fd_set *rsock = 0, *wsock = 0;
	fd_set rfd, wfd;
	
	if ((mask&RDMASK) != RDMASK) {
		rsock = (fd_set *)&rfd;
		FD_ZERO(rsock);
		FD_SET(rs,rsock);
	}
	if ((mask&WRMASK) != WRMASK) {
		wsock = (fd_set *)&wfd;
		FD_ZERO(wsock);
		FD_SET(ws,wsock);
	}
	
	/* args: FD_SETSIZE,writeset,readset,out-of-band sent, timeout*/
	const int ok = ::select(max(rs, ws)+1, rsock, wsock, (fd_set *)0, &timeout);
	
	if(ok > 0) {
		return ((rsock && FD_ISSET(rs,rsock)) ? RDMASK : 0)
		     | ((wsock && FD_ISSET(ws,wsock)) ? WRMASK : 0);
	} else if (ok == 0 || errno == EINTR) { // Interruptions don't matter
		return 0;
	} else {
		return -1;
	}
}

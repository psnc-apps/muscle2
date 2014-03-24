/*
* Copyright 2010-2013 Multiscale Applications on European e-Infrastructures (MAPPER) project
*
* GNU Lesser General Public License
*
* This file is part of MUSCLE (Multiscale Coupling Library and Environment).
*
* MUSCLE is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* MUSCLE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/
#include "communicator.hpp"
#include "util/exception.hpp"
#include <strings.h>
#include <string.h>
#include <stdio.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/uio.h>
#include <unistd.h>
#include <errno.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <exception>
#ifdef CPPMUSCLE_TRACE
#include <iostream>
#endif

extern "C" size_t communicator_read_from_socket(void *socket_handle, void *buf, int buf_len)
{
#ifdef CPPMUSCLE_TRACE
    std::cout << "xdr_read_from_socket:" << buf_len << " (fd " << *(int *)socket_handle << ")" << std::endl;
#endif
	return read(*(int *)socket_handle, buf, buf_len);
}

extern "C" size_t communicator_write_to_socket(void *socket_handle, void *buf, int buf_len)
{
#ifdef CPPMUSCLE_TRACE
    std::cout << "xdr_write_to_socket:" << buf_len << " (fd " << *(int *)socket_handle << ")" << std::endl;
#endif
	return write(*(int *)socket_handle, buf, buf_len);
}

namespace muscle {

Communicator::Communicator(net::endpoint &ep) : address(ep)
{
    sock = createSocket();
}

Communicator::~Communicator()
{
	delete sock;
}

net::ClientSocket *Communicator::createSocket()
{
	net::socket_opts opts;
	opts.blocking_connect = true;
	opts.keep_alive = true;

	net::ClientSocket *csock = new net::CClientSocket(address, NULL, opts);
	csock->setBlocking(true);
	csock->setDelay(false);
	return csock;
}

std::string Communicator::retrieve_string(muscle_protocol_t opcode, std::string *name) {
	char *str = (char *)0;
	size_t len = 65536;
	execute_protocol(opcode, name, MUSCLE_STRING, NULL, 0, &str, &len);
	std::string str_out(str);
	free_data(str, MUSCLE_STRING);
	return str_out;
}

}

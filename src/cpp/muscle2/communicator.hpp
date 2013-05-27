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
#ifndef MUSCLE_COMMUNICATOR_HPP
#define MUSCLE_COMMUNICATOR_HPP

#include <string>
#include <unistd.h>
#include <netdb.h>
#include "logger.hpp"
#include "muscle_types.h"

// Keep in sync with Java protocol!
typedef enum { 
	// env
	PROTO_SEND = 4,
	PROTO_RECEIVE = 5,
	PROTO_FINALIZE = 0,
	PROTO_WILL_STOP = 3,
	PROTO_HAS_NEXT = 8,
	// CxA
	PROTO_KERNEL_NAME = 1,
	PROTO_PROPERTY = 2,
	PROTO_HAS_PROPERTY = 10,
	PROTO_PROPERTIES = 6,
	PROTO_TMP_PATH = 7,
	// Logger
	PROTO_LOG_LEVEL = 9
} muscle_protocol_t;

extern "C" size_t communicator_write_to_socket(void *socket_handle, void *buf, int buf_len);
extern "C" size_t communicator_read_from_socket(void *socket_handle, void *buf, int buf_len);

namespace muscle {

class Communicator
{
public:
	Communicator() : sockfd(-1) { }
	virtual ~Communicator() { 
		if (sockfd >= 0) close(sockfd);
	}
	/** Execute a MUSCLE protocol. Identifier is an ID of the name for which to communicate, the msg is the message to MUSCLE and the result the result from MUSCLE. */
	virtual int execute_protocol(muscle_protocol_t opcode, std::string *identifier, muscle_datatype_t type, const void *msg, size_t msg_len, void *result, size_t *result_len) { return 0; }
	/** Retrieves a string from MUSCLE with a certain protocol. If no name is needed for the string, it may be NULL. */
	std::string retrieve_string(muscle_protocol_t opcode, std::string *name);
	/** Free data that MUSCLE allocated */
	virtual void free_data(void *ptr, muscle_datatype_t type) {};
protected:
	void connect_socket(const char *hostname, int port);
	int sockfd;
private:
	int connect_socket_ipv4(struct hostent *server, uint16_t port);
	int connect_socket_ipv6(struct hostent *server, uint16_t port);
};

} // EO namespace muscle
#endif

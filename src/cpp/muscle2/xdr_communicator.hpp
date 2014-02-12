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
#ifndef MUSCLE_XDR_COMMUNICATOR_HPP
#define MUSCLE_XDR_COMMUNICATOR_HPP

#include "communicator.hpp"
#include "complex_data.hpp"
#include "util/logger.hpp"
#include "util/endpoint.h"

#include <rpc/types.h>
#include <rpc/xdr.h>

namespace muscle {

class XdrCommunicator : public Communicator
{
public:
	XdrCommunicator(net::endpoint& ep, bool reconn);
	virtual ~XdrCommunicator() { xdr_destroy(&xdro); xdr_destroy(&xdri); }
	int execute_protocol(muscle_protocol_t opcode, std::string *identifier, muscle_datatype_t type, const void *msg, size_t msg_len, void *result, size_t *result_len);
	void free_data(void *ptr, muscle_datatype_t type);
private:
	static xdrproc_t get_proc(muscle_complex_t type);
	int send_array(muscle_complex_t type, char **msg, unsigned int *len, size_t sz);
	int recv_array(muscle_complex_t type, char **result, unsigned int *len, size_t sz);
	XDR xdro, xdri;
    int sockfd;
	const bool reconnect;
};
} // EO namespace muscle
#endif

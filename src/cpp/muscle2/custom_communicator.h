//
//  custom_communicator.h
//  CMuscle
//
//  Created by Joris Borgdorff on 17-12-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#ifndef __CMuscle__custom_communicator__
#define __CMuscle__custom_communicator__

#include "communicator.hpp"
#include "util/custom_serializer.h"
#include "muscle_types.h"
#include "complex_data.hpp"

namespace muscle {
	class CustomCommunicator : public Communicator
	{
	public:
		CustomCommunicator(net::endpoint& ep, bool reconn);
		virtual ~CustomCommunicator();
		
		int execute_protocol(muscle_protocol_t opcode, std::string *identifier, muscle_datatype_t type, const void *msg, size_t msg_len, void *result, size_t *result_len);
		void free_data(void *ptr, muscle_datatype_t type);
	private:
		void send_array(muscle_complex_t type, const void *msg, size_t len);
		void recv_array(muscle_complex_t type, void *result, size_t *len);

		net::custom_deserializer *sin;
		net::custom_serializer *sout;
		
		bool reconnect;
		
		static const size_t BUFSIZE_IN, BUFSIZE_OUT;
	};
}

#endif /* defined(__CMuscle__custom_communicator__) */

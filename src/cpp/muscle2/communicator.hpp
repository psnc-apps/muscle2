#ifndef MUSCLE_COMMUNICATOR_HPP
#define MUSCLE_COMMUNICATOR_HPP

#include <string>
#include <stdexcept>
#include <unistd.h>
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
	PROTO_PROPERTIES = 6,
	PROTO_TMP_PATH = 7
} muscle_protocol_t;

extern "C" int communicator_write_to_socket(void *socket_handle, void *buf, int buf_len);
extern "C" int communicator_read_from_socket(void *socket_handle, void *buf, int buf_len);

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
	
	class io_exception : public std::exception {
		public:
			io_exception (std::string msg) throw() { desc = "I/O exception: " + msg; };
			virtual const char* what() const throw() { return desc.c_str(); };
			virtual ~io_exception() throw() {};
		private:
			std::string desc;
	};
protected:
	void connect_socket(const char *hostname, int port);
	int sockfd;
};

} // EO namespace muscle
#endif

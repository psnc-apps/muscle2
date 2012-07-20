#ifndef COMMUNICATOR_HPP
#define COMMUNICATOR_HPP

#include <string>
#include <cstring>
#include <boost/asio.hpp>
#include "logger.hpp"
#include "muscle_types.h"

// Keep in sync with Java protocol!
typedef enum { 
	// env
	PROTO_SEND = 4,
	PROTO_RECEIVE = 5,
	PROTO_FINALIZE = 0,
	PROTO_WILL_STOP = 3,
	// CxA
	PROTO_KERNEL_NAME = 1,
	PROTO_PROPERTY = 2,
	PROTO_PROPERTIES = 6,
	PROTO_TMP_PATH = 7
} muscle_protocol_t;

using boost::asio::ip::tcp;

extern "C" int communicator_write_to_socket(void *socket_handle, void *buf, int buf_len);
extern "C" int communicator_read_from_socket(void *socket_handle, void *buf, int buf_len);

namespace muscle {

class Communicator
{
public:
	Communicator() {}
	virtual ~Communicator() { }
	virtual int execute_protocol(muscle_protocol_t opcode, std::string *identifier, muscle_datatype_t type, const void *msg, size_t msg_len, void *result, size_t *result_len) { return 0; }
	std::string retrieve_string(muscle_protocol_t opcode, std::string *name);
	virtual void free_data(void *ptr, muscle_datatype_t type) {};
protected:
	void connect_socket(boost::asio::ip::address_v4 host, int port);
	tcp::socket *s;
};

} // EO namespace muscle
#endif

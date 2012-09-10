#ifndef MUSCLE_XDR_COMMUNICATOR_HPP
#define MUSCLE_XDR_COMMUNICATOR_HPP

#include "logger.hpp"
#include "communicator.hpp"
#include "complex_data.hpp"
#include <rpc/types.h>
#include <rpc/xdr.h>

namespace muscle {

class XdrCommunicator : public Communicator
{
public:
	XdrCommunicator(const char *hostname, int port);
	virtual ~XdrCommunicator() { xdr_destroy(&xdro); xdr_destroy(&xdri); }
	int execute_protocol(muscle_protocol_t opcode, std::string *identifier, muscle_datatype_t type, const void *msg, size_t msg_len, void *result, size_t *result_len);
	void free_data(void *ptr, muscle_datatype_t type);
private:
	static xdrproc_t get_proc(muscle_complex_t type);
	XDR xdro, xdri;
};
} // EO namespace muscle
#endif

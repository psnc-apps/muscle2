#include "xdr_communicator.hpp"
#include "cppmuscle.hpp"
#include "logger.hpp"
#include "muscle_types.h"
#include "complex_data.hpp"
#include <cstring>
#include <rpc/types.h>
#include <rpc/xdr.h>
#include <cmath>
#include "exception.hpp"
#define M2_XDR_BUFSIZE (64*1024)

namespace muscle {

XdrCommunicator::XdrCommunicator(const char *hostname, int port) {
	connect_socket(hostname, port);

/*TODO: detect signature in CMake */
#ifdef __APPLE__
	xdrrec_create(&xdro, 0, 0, &sockfd, 0, (int (*) (void *, void *, int)) communicator_write_to_socket);
	xdrrec_create(&xdri, 0, 0, &sockfd,  (int (*) (void *, void *, int)) communicator_read_from_socket, 0);
#else
	xdrrec_create(&xdro, 0, 0, (char *)&sockfd, 0, (int (*) (char *, char *, int)) communicator_write_to_socket);
	xdrrec_create(&xdri, 0, 0, (char *)&sockfd,  (int (*) (char *, char *, int)) communicator_read_from_socket, 0);
#endif
	xdro.x_op = XDR_ENCODE;
	xdri.x_op = XDR_DECODE;
}

int XdrCommunicator::execute_protocol(muscle_protocol_t opcode, std::string *identifier, muscle_datatype_t type, const void *msg, size_t msg_len, void *result, size_t *result_len)
{
	// Encode
	int op = opcode;
	if (!xdr_int(&xdro, &op)) throw muscle_exception("Can not write int");
	
	if (opcode == PROTO_SEND || opcode == PROTO_RECEIVE || opcode == PROTO_PROPERTY || opcode == PROTO_HAS_NEXT)
	{
		char *cid = (char *)(*identifier).c_str(); //we are encoding only - so this is safe
		if (!xdr_string(&xdro, &cid, (*identifier).length())) throw muscle_exception("Can not write string");
	}
	if (opcode == PROTO_SEND)
	{
		unsigned int count_int;
		size_t sz;
		muscle_complex_t ctype;
		std::vector<int> dims;
		if (type == MUSCLE_COMPLEX) {
			ComplexData *data = (ComplexData *)msg;
			msg = data->getData();
			count_int = data->length();
			sz = data->sizeOfPrimitive();
			ctype = data->getType();
			dims = data->getDimensions();
		}
		else
		{
			count_int = msg_len;
			ctype = ComplexData::getType(type);
			sz = ComplexData::sizeOfPrimitive(ctype);
			// No extra dimensions
		}
		int itype = ctype;
		if (!xdr_int(&xdro, &itype)) throw muscle_exception("Can not write message protocol");
		
		if (ctype == COMPLEX_STRING)
		{
			if (!xdr_string(&xdro, (char **)&msg, count_int)) throw muscle_exception("Can not write identifier");
		}
		else
		{
			float tot_sz = count_int * (sz == 8 ? 8 : 4);
			int chunks = (int)ceil(tot_sz/M2_XDR_BUFSIZE);
			if (!xdr_int(&xdro, &chunks)) throw muscle_exception("Can not write int");
			if (chunks > 1)
			{
				int count_i = count_int;
				if (!xdr_int(&xdro, &count_i)) throw muscle_exception("Can not write int");
			}
		
			unsigned int chunk_len = (unsigned int)ceil(count_int/(float)chunks);
			unsigned int first_chunk_len = count_int - (chunks - 1)*chunk_len;

			char *msg_ptr = (char *)msg;
			if (!send_array(ctype, (char **)&msg_ptr, &first_chunk_len, sz)) throw muscle_exception("Can not write data chunk");;
			msg_ptr += first_chunk_len*sz;

			for (int i = 1; i < chunks; i++)
			{
				if (!xdrrec_endofrecord(&xdro, 1)) throw muscle_exception("Can not send data chunk");
				if (!send_array(ctype, (char **)&msg_ptr, &chunk_len, sz)) throw muscle_exception("Can not write data chunk");
				msg_ptr += chunk_len*sz;
			}
		}
		// Send dimensions
		
		for (std::vector<int>::iterator it = dims.begin();; ++it)
		{
			// don't include last dimension, it can be derived from the length of the array.
			if (it == dims.end()) break;
			int val = *it;
			if (!xdr_int(&xdro, &val)) throw muscle_exception("Can not write dimensions");
		}
	}
	if (!xdrrec_endofrecord(&xdro, 1)) throw muscle_exception("Can not send data");
	
	// Decode
	if (!xdrrec_skiprecord(&xdri)) throw muscle_exception("Can not receive data");
	
	switch (opcode) {
		case PROTO_SEND:
			break;
		case PROTO_RECEIVE: {
			unsigned int len;
			int complex_num;
			if (!xdr_int(&xdri, &complex_num)) throw muscle_exception("Can not read int");
			if (complex_num == -1) throw muscle_exception("Can not receive: conduit disconnected; sending side has quit");
			
			muscle_complex_t ctype = (muscle_complex_t)complex_num;
			size_t sz = ComplexData::sizeOfPrimitive(ctype);
			
			if (ctype == COMPLEX_STRING)
			{
				// cast for easier use
				if (!xdr_string(&xdri, (char **)result, M2_XDR_BUFSIZE)) throw muscle_exception("Can not read string");

				char *result_ptr = *(char **)result;
				
				// search for nul character, within the maximum bounds of the buffer
				char *null_ptr = (char *)memchr(result_ptr, 0, M2_XDR_BUFSIZE);
				if (null_ptr) {
					len = null_ptr - result_ptr + 1;
				} else {
					// nul not found, so add the nul character at the maximum buffer length
					result_ptr[M2_XDR_BUFSIZE - 1] = 0;
					len = M2_XDR_BUFSIZE;
				}
			}
			else
			{
				int chunks;
				if (!xdr_int(&xdri, &chunks)) throw muscle_exception("Can not read int");
				
				if (chunks > 1) {
					// The data was sent in chunks, to prevent a maximum size in XDR.
					int total_len;
					if (!xdr_int(&xdri, &total_len)) throw muscle_exception("Can not read int");
					
					// Allocate all necessary data, only if a null pointer was given
					if (!*(void **)result)
					{
						*(void **)result = malloc(total_len*sz);
					}
					// Convert to char and check if malloc succeeded.
					char *result_ptr = *(char **)result;
					if (!result_ptr) throw muscle_exception("Could not allocate buffer for receiving message");
					
					// Read the first chunk
					if (!recv_array(ctype, &result_ptr, &len, sz)) throw muscle_exception("Can not read data");
					// Update pointer for the next chunk
					result_ptr += len*sz;
					// Read all other chunks
					for (int i = 1; i < chunks; i++) {
						if (!xdrrec_skiprecord(&xdri)) throw muscle_exception("Can not proceed to next chunk");
						if (!recv_array(ctype, &result_ptr, &len, sz)) throw muscle_exception("Can not read data chunk");
						// Update pointer for the next chunk
						result_ptr += len*sz;
					}
					
					len = total_len;
				}
				else
				{
					// Read the data in one single go
					if (!recv_array(ctype, (char **)result, &len, sz))  throw muscle_exception("Can not read data");
				}
			}
			
			// Receive dimensions
			int dim_num = ComplexData::dimensions(ctype);
			if (dim_num > 1) {
				int nprod = 1, dim;
				std::vector<int> dims(dim_num);
				for (int i = 0; i < dim_num - 1; i++)
				{
					if (!xdr_int(&xdri, &dim)) throw muscle_exception("Can not read int");
					dims[i] = dim;
					// store product of dimensions so far
					nprod *= dim;
				}
				// Last dimension is not received but can be calculated
				dims[dim_num - 1] = len/nprod;
				ComplexData *cdata = new ComplexData(*(void **)result, ctype, &dims);
				*(void **)result = cdata;
			}
			// Make sure the call returns the right length
			*result_len = len;
		}
			break;
		case PROTO_WILL_STOP:
		case PROTO_HAS_NEXT:
			//decode answer
			if (!xdr_bool(&xdri, (bool_t *)result)) throw muscle_exception("Can not read boolean");
			break;
		case PROTO_LOG_LEVEL:
			if (!xdr_int(&xdri, (int *)result)) throw muscle_exception("Can not read log level");
			break;
		case PROTO_PROPERTY:
			bool_t success;
			if (!xdr_bool(&xdri, &success)) throw muscle_exception("Can not read property success status");
			if (!success) throw muscle_exception("Property does not exist");
			// no break
		case PROTO_KERNEL_NAME:
		case PROTO_TMP_PATH:
		case PROTO_PROPERTIES:
			//decode answer
			if (!xdr_string(&xdri, (char **)result, *result_len)) throw muscle_exception("Can not read string");
			break;
		case PROTO_FINALIZE:
			break;
	}
	
	return 1;
}

void XdrCommunicator::free_data(void *ptr, muscle_datatype_t type)
{
	if (type == MUSCLE_COMPLEX)
	{
		delete (ComplexData *)ptr;
	}
    // XDR free for xdr_string doesn't actually free anything. Sigh.
    // else if (type == MUSCLE_STRING) xdr_free((xdrproc_t)&xdr_string,(char*)&ptr);
	else
	{
		free(ptr);
	}
}

xdrproc_t XdrCommunicator::get_proc(muscle_complex_t type)
{
	xdrproc_t proc = NULL;
	switch (type)
	{
		case COMPLEX_DOUBLE: case COMPLEX_DOUBLE_ARR: case COMPLEX_DOUBLE_MATRIX_2D: case COMPLEX_DOUBLE_MATRIX_3D: case COMPLEX_DOUBLE_MATRIX_4D:
			proc = (xdrproc_t)&xdr_double;
			break;
		case COMPLEX_FLOAT: case COMPLEX_FLOAT_ARR: case COMPLEX_FLOAT_MATRIX_2D: case COMPLEX_FLOAT_MATRIX_3D: case COMPLEX_FLOAT_MATRIX_4D:
			proc = (xdrproc_t)&xdr_float;
			break;
		case COMPLEX_INT: case COMPLEX_INT_ARR: case COMPLEX_INT_MATRIX_2D: case COMPLEX_INT_MATRIX_3D: case COMPLEX_INT_MATRIX_4D:
			proc = (xdrproc_t)&xdr_int;
			break;
		case COMPLEX_LONG: case COMPLEX_LONG_ARR: case COMPLEX_LONG_MATRIX_2D: case COMPLEX_LONG_MATRIX_3D: case COMPLEX_LONG_MATRIX_4D:
			proc = (xdrproc_t)&xdr_hyper;
			break;
		case COMPLEX_BOOLEAN: case COMPLEX_BOOLEAN_ARR: case COMPLEX_BOOLEAN_MATRIX_2D: case COMPLEX_BOOLEAN_MATRIX_3D: case COMPLEX_BOOLEAN_MATRIX_4D:
			proc = (xdrproc_t)&xdr_char;
			break;
		case COMPLEX_BYTE: case COMPLEX_BYTE_ARR: case COMPLEX_BYTE_MATRIX_2D: case COMPLEX_BYTE_MATRIX_3D: case COMPLEX_BYTE_MATRIX_4D:
			proc = (xdrproc_t)&xdr_char;
			break;
		default:
			muscle::logger::severe("Datatype number %d not supported in native code", type);
			break;
	}
	return proc;
}

int XdrCommunicator::send_array(muscle_complex_t type, char **msg, unsigned int *len, size_t sz)
{
	if (type == COMPLEX_BYTE_ARR) {
		return xdr_bytes(&xdro, msg, len, M2_XDR_BUFSIZE);
	} else {
		xdrproc_t proc = get_proc(type);
		return xdr_array(&xdro, msg, len, M2_XDR_BUFSIZE, sz, proc);
	}
}

int XdrCommunicator::recv_array(muscle_complex_t type, char **result, unsigned int *len, size_t sz)
{
	if (type == COMPLEX_BYTE_ARR) {
		return xdr_bytes(&xdri, result, len, M2_XDR_BUFSIZE);
	} else {
		xdrproc_t proc = get_proc(type);
		return xdr_array(&xdri, result, len, M2_XDR_BUFSIZE, sz, proc);
	}
}

} // EO namespace muscle

#include "xdr_communicator.hpp"
#include "cppmuscle.hpp"
#include "logger.hpp"
#include "muscle_types.h"
#include "complex_data.hpp"
#include <cstring>
#include <rpc/types.h>
#include <rpc/xdr.h>

namespace muscle {

XdrCommunicator::XdrCommunicator(boost::asio::ip::address_v4 host, int port) {
	connect_socket(host, port);

/*TODO: detect signature in CMake */
#ifdef __APPLE__
	xdrrec_create(&xdro, 0, 0, (char*)s, 0, (int (*) (void *, void *, int)) communicator_write_to_socket);
	xdrrec_create(&xdri, 0, 0, (char*)s,  (int (*) (void *, void *, int)) communicator_read_from_socket, 0);
#else
	xdrrec_create(&xdro, 0, 0, (char*)s, 0, (int (*) (char *, char *, int)) communicator_write_to_socket);
	xdrrec_create(&xdri, 0, 0, (char*)s,  (int (*) (char *, char *, int)) communicator_read_from_socket, 0);
#endif
	xdro.x_op = XDR_ENCODE;
	xdri.x_op = XDR_DECODE;
}

int XdrCommunicator::execute_protocol(muscle_protocol_t opcode, std::string *identifier, muscle_datatype_t type, const void *msg, size_t msg_len, void *result, size_t *result_len)
{
	// Encode
	int op = opcode;
	assert( xdr_int(&xdro, &op) == 1 );
	
	if (opcode == PROTO_SEND || opcode == PROTO_RECEIVE || opcode == PROTO_PROPERTY)
	{
		char *cid = (char *)(*identifier).c_str(); //we are encoding only - so this is safe
		assert( xdr_string(&xdro, &cid, (*identifier).length()) == 1 );
	}
	if (opcode == PROTO_SEND)
	{
		ComplexData *data = type == MUSCLE_COMPLEX ? (ComplexData *)msg : new ComplexData(&msg, type, msg_len);
		unsigned int count_int = data->length();
		size_t sz = data->sizeOfPrimitive();
		muscle_complex_t ctype = data->getType();
		int itype = ctype;
		assert( xdr_int(&xdro, &itype) == 1);
		switch (ctype)
		{
			case COMPLEX_DOUBLE_ARR:
			case COMPLEX_DOUBLE_MATRIX_2D:
			case COMPLEX_DOUBLE_MATRIX_3D:
			case COMPLEX_DOUBLE_MATRIX_4D:
				assert( xdr_array(&xdro, (char **)&msg, &count_int, 65536, sz, (xdrproc_t)&xdr_double) == 1);
				break;
			case COMPLEX_FLOAT_ARR:
			case COMPLEX_FLOAT_MATRIX_2D:
			case COMPLEX_FLOAT_MATRIX_3D:
			case COMPLEX_FLOAT_MATRIX_4D:
				assert( xdr_array(&xdro, (char **)&msg, &count_int, 65536, sz, (xdrproc_t)&xdr_float) == 1);
				break;
			case COMPLEX_INT_ARR:
			case COMPLEX_INT_MATRIX_2D:
			case COMPLEX_INT_MATRIX_3D:
			case COMPLEX_INT_MATRIX_4D:
				assert( xdr_array(&xdro, (char **)&msg, &count_int, 65536, sz, (xdrproc_t)&xdr_int) == 1);
				break;
			case COMPLEX_LONG_ARR:
			case COMPLEX_LONG_MATRIX_2D:
			case COMPLEX_LONG_MATRIX_3D:
			case COMPLEX_LONG_MATRIX_4D:
				assert( xdr_array(&xdro, (char **)&msg, &count_int, 65536, sz, (xdrproc_t)&xdr_long) == 1);
				break;
			case COMPLEX_STRING:
				assert( xdr_string(&xdro, (char **)&msg, count_int) == 1);
				break;
			case COMPLEX_STRING_ARR:
				assert( xdr_array(&xdro, (char **)&msg, &count_int, 65536, sz, (xdrproc_t)&xdr_string) == 1);
				break;
			case COMPLEX_BOOLEAN_ARR:
			case COMPLEX_BOOLEAN_MATRIX_2D:
			case COMPLEX_BOOLEAN_MATRIX_3D:
			case COMPLEX_BOOLEAN_MATRIX_4D:
				assert( xdr_array(&xdro, (char **)&msg, &count_int, 65536, sz, (xdrproc_t)&xdr_bool) == 1);
				break;
			case COMPLEX_BYTE_ARR:
			case COMPLEX_BYTE_MATRIX_2D:
			case COMPLEX_BYTE_MATRIX_3D:
			case COMPLEX_BYTE_MATRIX_4D:
				assert( xdr_bytes(&xdro, (char **)&msg, &count_int, 65536) == 1);
				break;
			default:
				muscle::logger::severe("Datatype number %d not supported in native code", ctype);
				break;
		}
		// Send dimensions
		std::vector<int> dims = data->getDimensions();
		for (std::vector<int>::iterator it = dims.begin();; ++it)
		{
			// don't include last dimension, it can be derived from the length of the array.
			if (it == dims.end()) break;
			int val = *it;
			assert( xdr_int(&xdro, &val) == 1);
		}
	}
	assert( xdrrec_endofrecord(&xdro, 1) == 1);
	
	// Decode
	assert( xdrrec_skiprecord(&xdri) == 1);
	
	switch (opcode) {
		case PROTO_SEND:
			break;
		case PROTO_RECEIVE: {
			unsigned int len;
			int complex_num;
			assert( xdr_int(&xdri, &complex_num) == 1);
			muscle_complex_t ctype = (muscle_complex_t)complex_num;
			size_t sz = ComplexData::sizeOfPrimitive(ctype);
			switch (ctype)
			{
				case COMPLEX_DOUBLE_ARR:
				case COMPLEX_DOUBLE_MATRIX_2D:
				case COMPLEX_DOUBLE_MATRIX_3D:
				case COMPLEX_DOUBLE_MATRIX_4D:
					assert( xdr_array(&xdri, (char **)result, &len, *result_len, sz, (xdrproc_t)&xdr_double) == 1);
					break;
				case COMPLEX_FLOAT_ARR:
				case COMPLEX_FLOAT_MATRIX_2D:
				case COMPLEX_FLOAT_MATRIX_3D:
				case COMPLEX_FLOAT_MATRIX_4D:
					assert( xdr_array(&xdri, (char **)result, &len, *result_len, sz, (xdrproc_t)&xdr_float) == 1);
					break;
				case COMPLEX_INT_ARR:
				case COMPLEX_INT_MATRIX_2D:
				case COMPLEX_INT_MATRIX_3D:
				case COMPLEX_INT_MATRIX_4D:
					assert( xdr_array(&xdri, (char **)result, &len, *result_len, sz, (xdrproc_t)&xdr_int) == 1);
					break;
				case COMPLEX_LONG_ARR:
				case COMPLEX_LONG_MATRIX_2D:
				case COMPLEX_LONG_MATRIX_3D:
				case COMPLEX_LONG_MATRIX_4D:
					assert( xdr_array(&xdri, (char **)result, &len, *result_len, sz, (xdrproc_t)&xdr_long) == 1);
					break;
				case COMPLEX_STRING:
					assert( xdr_string(&xdri, (char **)result, *result_len) == 1);
					break;
				case COMPLEX_STRING_ARR:
					assert( xdr_array(&xdri, (char **)result, &len, *result_len, sz, (xdrproc_t)&xdr_string) == 1);
					break;
				case COMPLEX_BOOLEAN_ARR:
				case COMPLEX_BOOLEAN_MATRIX_2D:
				case COMPLEX_BOOLEAN_MATRIX_3D:
				case COMPLEX_BOOLEAN_MATRIX_4D:
					assert( xdr_array(&xdri, (char **)result, &len, *result_len, sz, (xdrproc_t)&xdr_bool) == 1);
					break;
				case COMPLEX_BYTE_ARR:
				case COMPLEX_BYTE_MATRIX_2D:
				case COMPLEX_BYTE_MATRIX_3D:
				case COMPLEX_BYTE_MATRIX_4D:
					assert( xdr_bytes(&xdri, (char **)result, &len, *result_len) == 1);
					break;
				default:
					muscle::logger::severe("Datatype number %d not supported in native code", ctype);
					break;
			}
			
			// Receive dimensions
			int dim_num = ComplexData::dimensions(ctype);
			if (dim_num > 1) {
				int nprod = 1, dim;
				std::vector<int> dims(dim_num);
				for (int i = 0; i < dim_num - 1; i++)
				{
					assert( xdr_int(&xdri, &dim) == 1);
					dims.push_back(dim);
					nprod *= dim;
				}
				dims.push_back(len/nprod);
				ComplexData *cdata = new ComplexData(*(void **)result, ctype, &dims);
				result = &cdata;
			}
			*result_len = len;
		}
			break;
		case PROTO_WILL_STOP:
			//decode answer
			assert( xdr_bool(&xdri, (bool_t *)result) == 1);
			break;
		case PROTO_PROPERTY:
		case PROTO_KERNEL_NAME:
		case PROTO_TMP_PATH:
		case PROTO_PROPERTIES: {
			//decode answer
			char *name = NULL;
			assert( xdr_string(&xdri, &name, *result_len) == 1);
			//convert to std:string
			*((std::string **)result) = new std::string(name);
			//and free the xdr allocated buf
			xdr_free((xdrproc_t)&xdr_string,(char*) &name);
		}
			break;
		case PROTO_FINALIZE:
			break;
	}
	
	return 0;
}

void XdrCommunicator::free_data(void *ptr, muscle_datatype_t type)
{
	if (type == MUSCLE_COMPLEX)
	{
		delete (ComplexData *)ptr;
	}
	else
	{
		free(ptr);
	}
}

} // EO namespace muscle

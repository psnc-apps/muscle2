//
//  custom_communicator.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 17-12-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include "custom_communicator.h"
#include <vector>
#include <cstdlib> // free()

#define M2_XDR_BUFSIZE 2147483647

using namespace muscle::net;

namespace muscle {
	const size_t CustomCommunicator::BUFSIZE_IN = 65536;
	const size_t CustomCommunicator::BUFSIZE_OUT = 65536;
	CustomCommunicator::CustomCommunicator(net::endpoint& ep, bool reconn) : Communicator(ep), reconnect(reconn)
	{
		sin = new custom_deserializer(&sock, BUFSIZE_IN);
		sout = new custom_serializer(&sock, BUFSIZE_OUT);
	}
	
	CustomCommunicator::~CustomCommunicator()
	{
		delete sin;
		delete sout;
	}
	
	int CustomCommunicator::execute_protocol(muscle_protocol_t opcode, std::string *identifier, muscle_datatype_t type, const void *msg, size_t msg_len, void *result, size_t *result_len)
	{
		
		if (reconnect && !sock) {
			sock = createSocket();
		}
		
		// Encode
		sout->encodeInt(opcode);
		
		if (opcode == PROTO_SEND || opcode == PROTO_RECEIVE || opcode == PROTO_PROPERTY || opcode == PROTO_HAS_NEXT || opcode == PROTO_HAS_PROPERTY) {
			sout->encodeString((*identifier).c_str());
		}
		if (opcode == PROTO_SEND)
		{
			size_t count_int;
			muscle_complex_t ctype;
			std::vector<int> dims;
			if (type == MUSCLE_COMPLEX) {
				ComplexData *data = (ComplexData *)msg;
				msg = data->getData();
				/* Check if it is not out of bounds? (unsigned long to unsigned int) */
				count_int = data->length();
				ctype = data->getType();
				dims = data->getDimensions();
			} else {
				count_int = msg_len;
				ctype = ComplexData::getType(type);
				// No extra dimensions
			}
			sout->encodeInt(ctype);
			
			if (ctype == COMPLEX_STRING) {
				sout->encodeString((const char *)msg);
			} else {
				if (count_int + 8 >= M2_XDR_BUFSIZE)
					throw muscle_exception("Message too large, cannot send arrays with more than 2 147 483 639 elements.");
				
				send_array(ctype, (const char *)msg, count_int);
			}
			// Send dimensions (don't include last dimension,
			// it can be derived from the length of the array.)
			for (int i = 0; i < (int)dims.size() - 1; ++i) {
				sout->encodeInt(dims[i]);
			}
		}
		
		// Send data
		sout->flush();

//		if (opcode != PROTO_SEND)
//			sin->endDecoding();
		
		switch (opcode) {
			case PROTO_SEND:
				break;
			case PROTO_RECEIVE: {
				int complex_num = sin->decodeInt();
				if (complex_num == -1)
					throw muscle_exception("Cannot receive: conduit disconnected; sending side has quit");
				
				muscle_complex_t ctype = (muscle_complex_t)complex_num;
				
				if (ctype == COMPLEX_STRING)
				{
					// cast for easier use
					*(char **)result = sin->decodeString(*(char **)result, result_len);
				} else {
					// Read the data in one single go
					recv_array(ctype, result, result_len);
				}
				
				// Receive dimensions
				int dim_num = ComplexData::dimensions(ctype);
				if (dim_num > 1) {
					int nprod = 1;
					std::vector<int> dims(dim_num);
					for (int i = 0; i < dim_num - 1; i++) {
						dims[i] = sin->decodeInt();
						// store product of dimensions so far
						nprod *= dims[i];
					}
					// Last dimension is not received but can be calculated
					dims[dim_num - 1] = (int)(*result_len/nprod);
					ComplexData *cdata = new ComplexData(*(void **)result, ctype, &dims);
					*(void **)result = cdata;
				}
			}
				break;
			case PROTO_WILL_STOP:
			case PROTO_HAS_NEXT:
			case PROTO_HAS_PROPERTY:
				//decode answer
				*(bool *)result = sin->decodeBoolean();
				break;
			case PROTO_LOG_LEVEL:
				*(int *)result = sin->decodeInt();
				break;
			case PROTO_PROPERTY:
				if (!sin->decodeBoolean())
					throw muscle_exception("Property " + *identifier + " does not exist");
				// no break
			case PROTO_KERNEL_NAME:
			case PROTO_TMP_PATH:
			case PROTO_PROPERTIES:
				//decode answer
				*(char **)result = sin->decodeString(*(char **)result, result_len);
				break;
			case PROTO_FINALIZE:
				return 1; // XXXdon't call endDecoding: the socket will be closed
		}
			
		if (opcode != PROTO_SEND) {
			sin->endDecoding();
			/*
			 * Mohamed: here we decide to check or not the connection during the next  operation:
			 * - The server does not close the socket connection with the client if the current operation is a SEND.
			 * - The server is obliged to close the socket connection (forced flush)  with the client for
			 * the other operation since they required send/receive of data
			 */
			if (reconnect) {
				delete sock;
				sock = NULL;
			}
		}
		
		return 1;
	}
	
	void CustomCommunicator::free_data(void *ptr, muscle_datatype_t type)
	{
		if (type == MUSCLE_COMPLEX) {
			delete (ComplexData *)ptr;
		} else {
			free(ptr);
		}
	}
		
	void CustomCommunicator::send_array(muscle_complex_t type, const void *msg, size_t len)
	{
		switch (type) {
			case COMPLEX_BYTE_ARR: case COMPLEX_BYTE_MATRIX_2D: case COMPLEX_BYTE_MATRIX_3D: case COMPLEX_BYTE_MATRIX_4D:
				sout->encodeByteArray((const char *)msg, len);
				break;
			case COMPLEX_DOUBLE_ARR: case COMPLEX_DOUBLE_MATRIX_2D: case COMPLEX_DOUBLE_MATRIX_3D: case COMPLEX_DOUBLE_MATRIX_4D:
				sout->encodeDoubleArray((const double *)msg, len);
				break;
			case COMPLEX_FLOAT_ARR: case COMPLEX_FLOAT_MATRIX_2D: case COMPLEX_FLOAT_MATRIX_3D: case COMPLEX_FLOAT_MATRIX_4D:
				sout->encodeFloatArray((const float *)msg, len);
				break;
			case COMPLEX_INT_ARR: case COMPLEX_INT_MATRIX_2D: case COMPLEX_INT_MATRIX_3D: case COMPLEX_INT_MATRIX_4D:
				sout->encodeIntArray((const int32_t *)msg, len);
				break;
			case COMPLEX_LONG_ARR: case COMPLEX_LONG_MATRIX_2D: case COMPLEX_LONG_MATRIX_3D: case COMPLEX_LONG_MATRIX_4D:
				sout->encodeLongArray((const int64_t *)msg, len);
				break;
			case COMPLEX_BOOLEAN_ARR: case COMPLEX_BOOLEAN_MATRIX_2D: case COMPLEX_BOOLEAN_MATRIX_3D: case COMPLEX_BOOLEAN_MATRIX_4D:
				sout->encodeBooleanArray((const bool *)msg, len);
				break;
			default:
				muscle::logger::severe("Datatype number %d not supported in native code", type);
				break;
		}
	}
	
	void CustomCommunicator::recv_array(muscle_complex_t type, void *result, size_t *len)
	{
		switch (type) {
			case COMPLEX_BYTE_ARR: case COMPLEX_BYTE_MATRIX_2D: case COMPLEX_BYTE_MATRIX_3D: case COMPLEX_BYTE_MATRIX_4D:
				*(char **)result = sin->decodeByteArray(*(char **)result, len);
				break;
			case COMPLEX_DOUBLE_ARR: case COMPLEX_DOUBLE_MATRIX_2D: case COMPLEX_DOUBLE_MATRIX_3D: case COMPLEX_DOUBLE_MATRIX_4D:
				*(double **)result = sin->decodeDoubleArray(*(double **)result, len);
				break;
			case COMPLEX_FLOAT_ARR: case COMPLEX_FLOAT_MATRIX_2D: case COMPLEX_FLOAT_MATRIX_3D: case COMPLEX_FLOAT_MATRIX_4D:
				*(float **)result = sin->decodeFloatArray(*(float **)result, len);
				break;
			case COMPLEX_INT_ARR: case COMPLEX_INT_MATRIX_2D: case COMPLEX_INT_MATRIX_3D: case COMPLEX_INT_MATRIX_4D:
				*(int32_t **)result = sin->decodeIntArray(*(int32_t **)result, len);
				break;
			case COMPLEX_LONG_ARR: case COMPLEX_LONG_MATRIX_2D: case COMPLEX_LONG_MATRIX_3D: case COMPLEX_LONG_MATRIX_4D:
				*(int64_t **)result = sin->decodeLongArray(*(int64_t **)result, len);
				break;
			case COMPLEX_BOOLEAN_ARR: case COMPLEX_BOOLEAN_MATRIX_2D: case COMPLEX_BOOLEAN_MATRIX_3D: case COMPLEX_BOOLEAN_MATRIX_4D:
				*(bool **)result = sin->decodeBooleanArray(*(bool **)result, len);
				break;
			default:
				muscle::logger::severe("Datatype number %d not supported in native code", type);
				break;
		}
	}
}

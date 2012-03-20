
#include "cppmuscle.hpp"

#include <stdlib.h>
#include <rpc/types.h>
#include <rpc/xdr.h>

#include <cstdio>
#include <iomanip>
#include <iostream>
#include <boost/asio.hpp>
#include <boost/lexical_cast.hpp>

using boost::asio::ip::tcp;
using namespace std;


namespace muscle {

tcp::socket *s;
XDR xdro, xdri;

/* #define CPPMUSCLE_TRACE  */


extern "C" int xdr_read_from_socket(void *socket_handle, void *buf, int buf_len)
{
#ifdef CPPMUSCLE_TRACE
	cout << "xdr_read_from_socket:" << buf_len << endl;
#endif
	return ((tcp::socket *)socket_handle)->read_some(boost::asio::buffer(buf, buf_len));
}

extern "C" int xdr_write_to_socket(void *socket_handle, void *buf, int buf_len)
{
#ifdef CPPMUSCLE_TRACE
	cout << "xdr_write_to_socket:" << buf_len << endl;
#endif
	return boost::asio::write(*((tcp::socket *)socket_handle), boost::asio::buffer(buf, buf_len));
}

void env::init(void)
{
	using boost::lexical_cast;

#ifdef CPPMUSCLE_TRACE
	cout << "muscle::env::init() " << endl;
#endif

	try
	{
		boost::asio::io_service io_service;

		s = new tcp::socket(io_service);

		s->connect(
				tcp::endpoint(
						boost::asio::ip::address_v4::loopback(),

						boost::lexical_cast<unsigned short>(getenv("MUSCLE_GATEWAY_PORT")))); /* TODO: check if NOT NULL */
/*TODO: detect signature in CMake */
#ifdef __APPLE__
		xdrrec_create(&xdro, 0, 0, (char*)s, 0, (int (*) (void *, void *, int)) xdr_write_to_socket);
#else
		xdrrec_create(&xdro, 0, 0, (char*)s, 0, (int (*) (char *, char *, int)) xdr_write_to_socket);
#endif
		xdro.x_op = XDR_ENCODE;

#ifdef __APPLE__
		xdrrec_create(&xdri, 0, 0, (char*)s,  (int (*) (void *, void *, int)) xdr_read_from_socket, 0);
#else
		xdrrec_create(&xdri, 0, 0, (char*)s,  (int (*) (char *, char *, int)) xdr_read_from_socket, 0);
#endif
		xdri.x_op = XDR_DECODE;

	} catch (std::exception& e) {
		 cout << "Exception: " << e.what() << endl;
	}


}

void env::finalize(void)
{
	int opcode = 0;

#ifdef CPPMUSCLE_TRACE
	cout << "muscle::env::finalize() " << endl;
#endif

	// encode
	assert( xdr_int(&xdro, &opcode) == 1 );
	// end send request
	assert( xdrrec_endofrecord(&xdro, 1) == 1);

	//s->shutdown(tcp::socket::shutdown_both);
	//s->close();

	//delete s;

	xdr_destroy(&xdro);
	xdr_destroy(&xdri);
}

std::string cxa::kernel_name(void)
{
	int opcode = 1;
	char *kernel_name = NULL;

#ifdef CPPMUSCLE_TRACE
	cout << "muscle::cxa::kernel_name() " << endl;
#endif

	// encode
	assert( xdr_int(&xdro, &opcode) == 1 );
	// end send request
	assert( xdrrec_endofrecord(&xdro, 1) == 1);

	assert( xdrrec_skiprecord(&xdri) == 1);
	//decode answer
	assert( xdr_string(&xdri, &kernel_name,  65536) == 1);

	//convert to std:string
	std::string kernel_name_str = string(kernel_name);
	//and free the xdr allocated buf
	xdr_free((xdrproc_t)&xdr_string,(char*) &kernel_name);

	return kernel_name_str;
}

std::string cxa::get_property(std::string name)
{
	int opcode = 2;
	char *prop_name = (char*)name.c_str(); /*we are encoding only - so this is safe */
	char *prop_value = NULL;

#ifdef CPPMUSCLE_TRACE
	cout << "muscle::cxa::get_property(" << name << ") " << endl;
#endif

	// encode
	assert( xdr_int(&xdro, &opcode) == 1 );
	assert( xdr_string(&xdro, &prop_name, name.length()) == 1 );

	// end send request
	assert( xdrrec_endofrecord(&xdro, 1) == 1);

	assert( xdrrec_skiprecord(&xdri) == 1);
	//decode answer
	assert( xdr_string(&xdri, &prop_value,  65536) == 1);

	//convert to std:string
	std::string prop_value_str = string(prop_value);
	//and free the xdr allocated buf
	xdr_free((xdrproc_t)&xdr_string, (char*) &prop_value);

#ifdef CPPMUSCLE_TRACE
	cout << "muscle::cxa::get_property -> " << prop_value_str << endl;
#endif

	return prop_value_str;
}

std::string cxa::get_properties()
{
	int opcode = 6;
	char *prop_value = NULL;

#ifdef CPPMUSCLE_TRACE
	cout << "muscle::cxa::get_properties()" << endl;
#endif

	// encode
	assert( xdr_int(&xdro, &opcode) == 1 );

	// end send request
	assert( xdrrec_endofrecord(&xdro, 1) == 1);

	assert( xdrrec_skiprecord(&xdri) == 1);
	//decode answer
	assert( xdr_string(&xdri, &prop_value,  65536) == 1);

	//convert to std:string
	std::string prop_value_str = string(prop_value);
	//and free the xdr allocated buf
	xdr_free((xdrproc_t)&xdr_string, (char*) &prop_value);

	return prop_value_str;
}

std::string env::get_tmp_path()
{
	int opcode = 7;
	char *tmp_path = NULL;

#ifdef CPPMUSCLE_TRACE
	cout << "muscle::env::get_tmp_path()" << endl;
#endif

	// encode
	assert( xdr_int(&xdro, &opcode) == 1 );

	// end send request
	assert( xdrrec_endofrecord(&xdro, 1) == 1);

	assert( xdrrec_skiprecord(&xdri) == 1);
	//decode answer
	assert( xdr_string(&xdri, &tmp_path,  65536) == 1);

	//convert to std:string
	std::string prop_value_str = string(tmp_path);
	//and free the xdr allocated buf
	xdr_free((xdrproc_t)&xdr_string, (char*) &tmp_path);

	return prop_value_str;
}

bool env::will_stop(void)
{
	int opcode = 3;
	bool_t is_will_stop = 0;

#ifdef CPPMUSCLE_TRACE
	cout << "muscle::env::will_stop()" << endl;
#endif

	// encode
	assert( xdr_int(&xdro, &opcode) == 1 );
	// end send request
	assert( xdrrec_endofrecord(&xdro, 1) == 1);

	assert( xdrrec_skiprecord(&xdri) == 1);
	//decode answer
	assert( xdr_bool(&xdri, &is_will_stop) == 1);

#ifdef CPPMUSCLE_TRACE
	cout << "muscle::env::will_stop -> " << is_will_stop << endl;
#endif

	return is_will_stop;
}

void env::send(std::string entrance_name, void *data, size_t count, muscle_datatype_t type)
{
	int opcode = 4;
	unsigned int count_int = count;
	char *entrance_name_cstr = (char*)entrance_name.c_str(); /*we are encoding only - so this is safe */

#ifdef CPPMUSCLE_TRACE
	cout << "muscle::env::send()" << endl;
#endif

	// encode
	assert( xdr_int(&xdro, &opcode) == 1 );
	assert( xdr_string(&xdro, &entrance_name_cstr, entrance_name.length()) == 1);
	assert( xdr_array(&xdro, (char **)&data, &count_int, 65536, sizeof(double), (xdrproc_t)&xdr_double) == 1);
	// end send request
	assert( xdrrec_endofrecord(&xdro, 1) == 1);

	assert( xdrrec_skiprecord(&xdri) == 1);

}


void* env::receive(std::string exit_name, void *data, size_t &count,  muscle_datatype_t type)
{
	int opcode = 5;
	unsigned int count_int = count;
	char *exit_name_cstr = (char*)exit_name.c_str(); /*we are encoding only - so this is safe */

#ifdef CPPMUSCLE_TRACE
	cout << "muscle::env::receive()" << endl;
#endif

	// encode
	assert( xdr_int(&xdro, &opcode) == 1 );
	assert( xdr_string(&xdro, &exit_name_cstr, exit_name.length()) == 1);
	// end send request
	assert( xdrrec_endofrecord(&xdro, 1) == 1);

	assert( xdrrec_skiprecord(&xdri) == 1);
	assert( xdr_array(&xdri, (char **)&data, &count_int, 65536, sizeof(double), (xdrproc_t)&xdr_double) == 1);

	return data;
}

void logger::log_message(muscle_loglevel_t level, std::string message)
{
	const char *str = "UNKNOWN";

	switch (level)
	{
	case MUSCLE_LOG_SEVERE:
		str = "SEVERE";
		break;
	case MUSCLE_LOG_WARNING:
		str = "WARNING";
		break;
	case MUSCLE_LOG_INFO:
		str = "INFO";
		break;
	case MUSCLE_LOG_CONFIG:
		str = "CONFIG";
		break;
	case MUSCLE_LOG_FINE:
		str = "FINE";
		break;
	case MUSCLE_LOG_FINER:
		str = "FINER";
		break;
	case MUSCLE_LOG_FINEST:
		str = "FINEST";
		break;
	}

	cout << "[" << setw(7) << str << "] " << message << endl;
}

void logger::severe(std::string message)
{
	logger::log_message(MUSCLE_LOG_SEVERE, message);
}

void logger::warning(std::string message)
{
	logger::log_message(MUSCLE_LOG_WARNING, message);
}

void logger::info(std::string message)
{
	logger::log_message(MUSCLE_LOG_INFO, message);
}

void logger::config(std::string message)
{
	logger::log_message(MUSCLE_LOG_CONFIG, message);
}

void logger::fine(std::string message)
{
	logger::log_message(MUSCLE_LOG_FINE, message);
}

void logger::finer(std::string message)
{
	logger::log_message(MUSCLE_LOG_FINER, message);
}

void logger::finest(std::string message)
{
	logger::log_message(MUSCLE_LOG_FINEST, message);
}

void env::free_data(void *ptr, muscle_datatype_t type)
{
	std::free(ptr);
}



} // EO namespace muscle

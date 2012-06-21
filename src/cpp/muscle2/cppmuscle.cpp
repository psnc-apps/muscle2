
#include "cppmuscle.hpp"

#include <stdlib.h>
#include <rpc/types.h>
#include <rpc/xdr.h>

#include <cstdio>
#include <stdio.h>
#include <string>
#include <iomanip>
#include <iostream>
#include <boost/asio.hpp>
#include <boost/lexical_cast.hpp>

using boost::asio::ip::tcp;
using namespace std;


namespace muscle {

tcp::socket *s;
XDR xdro, xdri;
pid_t muscle_pid;
const char *tmpfifo;

#define CPPMUSCLE_TRACE  


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

int env::init(int *argc, char ***argv)
{
	using boost::lexical_cast;

#ifdef CPPMUSCLE_TRACE
	cout << "muscle::env::init() " << endl;
#endif
	// Initialize host and port on which MUSCLE is listening
	unsigned short port = 0;
	char host_str[16];
	boost::asio::ip::address_v4 host;	
	muscle_pid = -1;
	
	env::muscle2_tcp_location(muscle_pid, host_str, &port);
	
	// Port is not initialized, initialize MUSCLE instead.
	if (port == 0)
	{
		logger::info("MUSCLE port not given. Starting new MUSCLE instance.");
		muscle_pid = env::muscle2_spawn(argc, argv);
		if (muscle_pid < 0)
		{
			if (muscle_pid == -2)
				logger::severe("Could not instantiate MUSCLE: no command line arguments given.");
			
			return MUSCLE_INIT_ERR_SPAWN;
		}
		
		env::muscle2_tcp_location(muscle_pid, host_str, &port);
		if (port == 0)
		{
			logger::severe("Could not contact MUSCLE: no TCP port given.");
			return MUSCLE_INIT_ERR_IO;
		}
	}
	host = *host_str ? boost::asio::ip::address_v4::from_string(host_str) : boost::asio::ip::address_v4::loopback();
	
	// Start communicating with MUSCLE instance
	try
	{
		boost::asio::io_service io_service;

		s = new tcp::socket(io_service);

		s->connect(tcp::endpoint(host, port));
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
		string msg = "Exception: ";
		msg += e.what();
		logger::severe(msg);
		return MUSCLE_INIT_ERR_IO;
	}
	return MUSCLE_INIT_SUCCESS;
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

	if (muscle_pid > 0) {
		int status;
		waitpid(muscle_pid, &status, 0);
		if (!WIFEXITED(status) || WEXITSTATUS(status)) {
			logger::severe("MUSCLE execution failed.");
		}
	}
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

/**
 * Gets the listening port of the created MUSCLE instance.
 * This implementation always returns the fixed port 50210 after a sleep of 10 seconds.
 * @param pid of the created MUSCLE instance
 * @return port number
 * @todo implement some means of retrieving the MUSCLE port number.
 */
void env::muscle2_tcp_location(pid_t pid, char *host, unsigned short *port)
{
	*port = 0;

#ifdef CPPMUSCLE_TRACE
	cout << "muscle::env::muscle2_tcp_location()" << endl;
#endif
	
	if (pid < 0)
	{
		char *port_str = getenv("MUSCLE_GATEWAY_PORT");
		if (port_str != NULL) {
			*port = boost::lexical_cast<unsigned short>(port_str);
			strncpy(host, getenv("MUSCLE_GATEWAY_HOST"), 16);
		}
	}
	else
	{
		FILE *fp = fopen(tmpfifo, "r");
		if(fp == 0 || ferror(fp))
		{	
			string msg = "Could not open temporary file ";
			msg += tmpfifo;
			logger::severe(msg);
			return;
		}
		while (fscanf(fp, "%15[^:]:%hu", host, port) == EOF) {
			sleep(1);
		}
		fclose(fp);
		// Null terminated
		assert( host[15] == 0 );

		char msg[96];
		sprintf(msg, "Will communicate with Java MUSCLE on %s:%d", host, *port);
		logger::info(msg);
	}
}

char * env::create_tmpfifo()
{
#ifdef CPPMUSCLE_TRACE
	cout << "muscle::env::create_tmpfifo()" << endl;
#endif

	char *tmppath;
	while ((tmppath = tempnam(NULL, NULL)))
	{
		if (mknod(tmppath, S_IFIFO|0600, 0) == 0)
			break;
		if (errno != EEXIST)
		{
			string msg = "Can not create temporary file; error ";
			msg += strerror(errno);
			logger::severe(msg);
			return NULL;
		}
	}
	if (!tmppath)
	{
		logger::severe("Can not create temporary file.");
		return NULL;
	}
	
	return tmppath;
}

/**
 * Spawn a new Java MUSCLE, using all arguments after "--" in the programs arguments.
 * For this to work, muscle2 must be in the PATH. All MUSCLE arguments, including "--"
 * will be removed from the arguments of the caller.
 * @param argc pointer to the number of arguments of the main function.
 * @param argv pointer to the arguments of the main function.
 * @return pid of the created MUSCLE instance.
 */
pid_t env::muscle2_spawn(int* argc, char ***argv)
{
#ifdef CPPMUSCLE_TRACE
	cout << "muscle::env::muscle2_spawn()" << endl;
#endif

	pid_t pid = -2;
	string term_str = "--";
	int term = -1;
	
	// Find terminator
	for (int i = 1; i < *argc; i++) {
		if (term_str.compare((*argv)[i]) == 0) {
			term = i;
			break;
		}
	}
	
	if (term == -1) return -1;
	
	tmpfifo = env::create_tmpfifo();
	
	// Size:                           1            | i - 1                     | 1    | *argc - (i + 1)          | 3
	// Number of arguments for muscle: muscle2      +                                    all arguments after "--" + tmparg + tmpdir + null terminator
	// Number of arguments given:      muscleKernel + all arguments before "--" + "--" + all arguments after "--"
	// Number of arguments returned:   muscleKernel + all arguments before "--"
	int new_args = 3;
	int argc_new = *argc + new_args - term;
	
	const char ** argv_new = (const char **)malloc(argc_new*sizeof(char *));
	argv_new[0] = "muscle2";
	// Copy all arguments after "--"
	memcpy(&argv_new[1], &(*argv)[term+1], (argc_new - (new_args + 1))*sizeof(char *));
	argv_new[argc_new - 3] = "--native-tmp-file";
	argv_new[argc_new - 2] = tmpfifo;
	argv_new[argc_new - 1] = NULL;

	// Spawn Java MUSCLE
	pid = env::spawn((char * const *)argv_new);
	free(argv_new);
	
	// Make MUSCLE arguments unavailable to the calling program.
	*argc = term;
	
	return pid;
}
		

/**
 * Spawn a new process with arguments argv. argv[0] is the process name.
 * @return the pid of the created process.
 */
pid_t env::spawn(char * const *argv)
{
	pid_t pid;
	
#ifdef CPPMUSCLE_TRACE
	cout << "muscle::env::spawn(" << argv[0] << ")" << endl;
#endif
	pid = fork();
	if (pid == -1) {
		logger::severe("Could not start new Java MUSCLE instance: fork failed. Aborting.");
		return -1;
	}
	
	// Child process: execute
	if (pid == 0) {
		int rc = execvp(argv[0], argv);
		if (rc == -1) {
			logger::severe("Executable muscle2 not found in the PATH. Aborting.");
			return -1;
		}
	}
	return pid;
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

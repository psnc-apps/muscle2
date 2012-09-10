#ifndef CPPMUSCLE_H
#define CPPMUSCLE_H

//#define CPPMUSCLE_TRACE  

#include <string>
#include <vector>
#include <sys/types.h>

#include "muscle_types.h"
#include "logger.hpp"

namespace muscle {
class env
{
public:

	/** Initialize MUSCLE. Call before any other function, with pointers to the number of arguments and arguments. */
	static muscle_error_t init(int* argc, char ***argv);
	/** Finalize MUSCLE. Call after any other function. */
	static void finalize(void);

	/** Whether MUSCLE should stop, based on the time step of the submodel and the timestamps of the received and sent messages. */
	static bool will_stop(void);

	/** Send a message of a given MUSCLE datatype over the given conduit entrance. The size_t is the number of elements in data. */
	static void send(std::string entrance_name, const void *data, size_t count, muscle_datatype_t type);
	/** Send a message of a double vector over the given conduit entrance. This is a convenience message for send. */
	static void sendDoubleVector(std::string entrance_name, const std::vector<double>& data);

	static bool has_next(std::string exit_name);
	static void* receive(std::string exit_name, void *data, size_t &count, muscle_datatype_t type);
	static std::vector<double> receiveDoubleVector(std::string exit_name);

	static void free_data(void *ptr, muscle_datatype_t type);

	static std::string get_tmp_path(void);
	
	static bool is_main_processor;

private:

	static int detect_mpi_rank();
	static pid_t spawn(char * const *argv);
	static pid_t muscle2_spawn(int* argc, char ***argv);
	static char * create_tmpfifo();
	static void muscle2_tcp_location(pid_t pid, char *host, unsigned short *port);
};

class cxa
{
public:
	static std::string kernel_name(void);
	static std::string get_property(std::string name);
	static std::string get_properties(void);
};

} // EO namespace muscle
#endif

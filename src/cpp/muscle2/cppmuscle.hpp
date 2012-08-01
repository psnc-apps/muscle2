#ifndef CPPMUSCLE_H
#define CPPMUSCLE_H

//#define CPPMUSCLE_TRACE  

#include <string>
#include <sys/types.h>

#include "muscle_types.h"
#include "logger.hpp"

namespace muscle {
class env
{
	public:

	static muscle_error_t init(int* argc, char ***argv);
	static void finalize(void);

	static bool will_stop(void);

	static void send(std::string entrance_name, void *data, size_t count, muscle_datatype_t type);
	static void* receive(std::string exit_name, void *data, size_t &count, muscle_datatype_t type);

	static void free_data(void *ptr, muscle_datatype_t type);

	static std::string get_tmp_path(void);

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

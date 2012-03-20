#ifndef CPPMUSCLE_H
#define CPPMUSCLE_H

#include <string>

#include "muscle_types.h"

namespace muscle {

	class env
	{
	  public:

		static void init(void);
		static void finalize(void);

		static bool will_stop(void);

		static void send(std::string entrance_name, void *data, size_t count, muscle_datatype_t type);
		static void* receive(std::string exit_name, void *data, size_t &count, muscle_datatype_t type);

		static void free_data(void *ptr, muscle_datatype_t type);

		static std::string get_tmp_path(void);
	};

	class cxa
	{
	  public:
		static std::string kernel_name(void);
		static std::string get_property(std::string name);
		static std::string get_properties(void);
	};

	class logger
	{
	  public:
		static void log_message(muscle_loglevel_t level, std::string message);

		static void severe(std::string message);
		static void warning(std::string message);
		static void info(std::string message);
		static void config(std::string message);
		static void fine(std::string message);
		static void finer(std::string message);
		static void finest(std::string message);

	};

} // EO namespace muscle
#endif




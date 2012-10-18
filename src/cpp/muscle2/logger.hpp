#ifndef MSUCLE_LOGGER_HPP
#define MSUCLE_LOGGER_HPP

#include <stdarg.h>
#include "muscle_types.h"

namespace muscle {
	class logger
	{
	  public:
		static void log_message(muscle_loglevel_t level, const char *message, ...);

		static void severe(const char *message, ...);
		static void warning(const char *message, ...);
		static void info(const char *message, ...);
		static void config(const char *message, ...);
		static void fine(const char *message, ...);
		static void finer(const char *message, ...);
		static void finest(const char *message, ...);
		static void setName(const char *_name, const char *_tmp_path);
		static void finalize();
	private:
		static void format(const muscle_loglevel_t level, const char *message, va_list *args);
	};

} // EO namespace muscle
#endif


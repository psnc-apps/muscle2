#ifndef LOGGER_HPP
#define LOGGER_HPP

#include <string>
#include "muscle_types.h"

namespace muscle {
	class logger
	{
	  public:
		static void log_message(muscle_loglevel_t level, std::string message, ...);

		static void severe(std::string message, ...);
		static void warning(std::string message, ...);
		static void info(std::string message, ...);
		static void config(std::string message, ...);
		static void fine(std::string message, ...);
		static void finer(std::string message, ...);
		static void finest(std::string message, ...);
	private:
		static void format(muscle_loglevel_t level, std::string message, va_list args);
	};

} // EO namespace muscle
#endif
/*
* Copyright 2010-2013 Multiscale Applications on European e-Infrastructures (MAPPER) project
*
* GNU Lesser General Public License
* 
* This file is part of MUSCLE (Multiscale Coupling Library and Environment).
* 
* MUSCLE is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* MUSCLE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public License
* along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/
#ifndef MSUCLE_LOGGER_HPP
#define MSUCLE_LOGGER_HPP

#include "muscle_types.h"

#include <stdarg.h>
#include <cstdio>

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
		static void initialize(const char *_name, const char *_tmp_path, int level, bool will_log);
		static void initialize(const char *_name, FILE *file, int level, int file_level, bool will_log);
		static void finalize();
		
		static bool isLoggable(muscle_loglevel_t level);
	private:
		static void format(const muscle_loglevel_t level, const char *message, va_list *args);
        static const char *logger_name;
        static FILE *logger_fd;
        static int logger_level;
        static int logger_file_level;
        static int min_level;
        static bool will_log;
	};
	
} // EO namespace muscle
#endif


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
#include "logger.hpp"

#include <stdlib.h>
#include <cstdio>
#include <stdarg.h>
#include <string>
#include <iomanip>
#include <iostream>
#include <string.h>

using namespace std;

namespace muscle {

const char *logger_name = 0;
FILE *logger_fd = 0;
int logger_level = MUSCLE_LOG_ALL;
bool will_log = true;
	
void logger::log_message(muscle_loglevel_t level, const char *message, ...)
{
	if (!will_log) return;

	va_list args;
	va_start(args, message);
	logger::format(level, message, &args);
	va_end(args);
}

void logger::severe(const char *message, ...)
{
	if (!will_log) return;
	va_list args;
	va_start(args, message);
	logger::format(MUSCLE_LOG_SEVERE, message, &args);
	va_end(args);
}

void logger::warning(const char *message, ...)
{
	if (!will_log) return;
	va_list args;
	va_start(args, message);
	logger::format(MUSCLE_LOG_WARNING, message, &args);
	va_end(args);
}

void logger::info(const char *message, ...)
{
	if (!will_log) return;
	va_list args;
	va_start(args, message);
	logger::format(MUSCLE_LOG_INFO, message, &args);
	va_end(args);
}

void logger::config(const char *message, ...)
{
	if (!will_log) return;
	va_list args;
	va_start(args, message);
	logger::format(MUSCLE_LOG_CONFIG, message, &args);
	va_end(args);
}

void logger::fine(const char *message, ...)
{
	if (!will_log) return;
	va_list args;
	va_start(args, message);
	logger::format(MUSCLE_LOG_FINE, message, &args);
	va_end(args);
}

void logger::finer(const char *message, ...)
{
	if (!will_log) return;
	va_list args;
	va_start(args, message);
	logger::format(MUSCLE_LOG_FINER, message, &args);
	va_end(args);
}

void logger::finest(const char *message, ...)
{
	if (!will_log) return;
	va_list args;
	va_start(args, message);
	logger::format(MUSCLE_LOG_FINEST, message, &args);
	va_end(args);
}

inline void logger::format(const muscle_loglevel_t level, const char *message, va_list *args)
{	time_t timer;
	char timebuf[9];
	struct tm* tm_info;
	const char *level_str;

	time(&timer);
	tm_info = localtime(&timer);

	strftime(timebuf, 9, "%H:%M:%S", tm_info);

	switch (level)
	{
	case MUSCLE_LOG_SEVERE:
		level_str = "ERROR: ";
		break;
	case MUSCLE_LOG_WARNING:
		level_str = "warning: ";
		break;
	case MUSCLE_LOG_INFO:
		level_str = "";
		break;
	case MUSCLE_LOG_CONFIG:
		level_str = "config: ";
		break;
	case MUSCLE_LOG_FINE:
		level_str = "debug: ";
		break;
	case MUSCLE_LOG_FINER:
		level_str = "debug: ";
		break;
	case MUSCLE_LOG_FINEST:
		level_str = "debug: ";
		break;
	default:
		level_str = "";
		break;
	}

	va_list cp_args;
	if (logger_fd)
	{	va_copy(cp_args, *args);
	}
	if (level >= logger_level) {
		if (logger_name)
		{	printf("(%8s %6s) %s", timebuf, logger_name, level_str);
		}
		else
		{	printf("(%8s       ) %s", timebuf, level_str);
		}

		vprintf(message, *args);
		printf("\n");
		fflush(stdout);
	}
	if (logger_fd)
	{
		if (logger_name)
		{	fprintf(logger_fd, "(%8s %6s) %s", timebuf, logger_name, level_str);
		}
		else
		{	fprintf(logger_fd, "(%8s       ) %s", timebuf, level_str);
		}

		vfprintf(logger_fd, message, cp_args);
		va_end(cp_args);
		fprintf(logger_fd,"\n");
		fflush(logger_fd);
	}
}

void logger::initialize(const char *_name, const char *_tmp_path, int _level, bool _will_log)
{
	logger_name = strdup(_name);
	if (strlen(_tmp_path) + strlen(_name) > 506) {
		logger::warning("Temporary directory <%s> and name <%s> are too long to open a log file for.", _tmp_path, _name);
		return;
	}
	char filename[512];
	sprintf(filename, "%s/%s.native.log", _tmp_path, _name);
	logger_fd = fopen(filename,"a");
	logger_level = _level;
    will_log = _will_log;
}

void logger::finalize()
{
	if (logger_fd) {
		fclose(logger_fd);
	}
}

} // EO namespace muscle

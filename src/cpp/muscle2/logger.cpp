#include "logger.hpp"

#include <stdlib.h>
#include <cstdio>
#include <stdarg.h>
#include <string>
#include <iomanip>
#include <iostream>
#include "cppmuscle.hpp"

using namespace std;

namespace muscle {

const char *logger_name = 0;
	
void logger::log_message(muscle_loglevel_t level, string message, ...)
{
	if (!env::is_main_processor) return;

	va_list args;
	va_start(args, message);
	logger::format(level, message, &args);
	va_end(args);
}

void logger::severe(string message, ...)
{
	if (!env::is_main_processor) return;
	va_list args;
	va_start(args, message);
	logger::format(MUSCLE_LOG_SEVERE, message, &args);
	va_end(args);
}

void logger::warning(string message, ...)
{
	if (!env::is_main_processor) return;
	va_list args;
	va_start(args, message);
	logger::format(MUSCLE_LOG_WARNING, message, &args);
	va_end(args);
}

void logger::info(string message, ...)
{
	if (!env::is_main_processor) return;
	va_list args;
	va_start(args, message);
	logger::format(MUSCLE_LOG_INFO, message, &args);
	va_end(args);
}

void logger::config(string message, ...)
{
	if (!env::is_main_processor) return;
	va_list args;
	va_start(args, message);
	logger::format(MUSCLE_LOG_CONFIG, message, &args);
	va_end(args);
}

void logger::fine(string message, ...)
{
	if (!env::is_main_processor) return;
	va_list args;
	va_start(args, message);
	logger::format(MUSCLE_LOG_FINE, message, &args);
	va_end(args);
}

void logger::finer(string message, ...)
{
	if (!env::is_main_processor) return;
	va_list args;
	va_start(args, message);
	logger::format(MUSCLE_LOG_FINER, message, &args);
	va_end(args);
}

void logger::finest(string message, ...)
{
	if (!env::is_main_processor) return;
	va_list args;
	va_start(args, message);
	logger::format(MUSCLE_LOG_FINEST, message, &args);
	va_end(args);
}

inline void logger::format(const muscle_loglevel_t level, string message, va_list *args)
{
	const char *level_str;

	switch (level)
	{
	case MUSCLE_LOG_SEVERE:
		level_str = "SEVERE";
		break;
	case MUSCLE_LOG_WARNING:
		level_str = "WARNING";
		break;
	case MUSCLE_LOG_INFO:
		level_str = "INFO";
		break;
	case MUSCLE_LOG_CONFIG:
		level_str = "CONFIG";
		break;
	case MUSCLE_LOG_FINE:
		level_str = "FINE";
		break;
	case MUSCLE_LOG_FINER:
		level_str = "FINER";
		break;
	case MUSCLE_LOG_FINEST:
		level_str = "FINEST";
		break;
	default:
		level_str = "OTHER";
		break;
	}

	if (logger_name)
	{
		printf("[%7s:%7s] ", logger_name, level_str);
	}
	else
	{
		printf("[       %7s] ", level_str);
	}
	vprintf(message.c_str(), *args);
	printf("\n");
	fflush(stdout);
}

void logger::setName(std::string _name)
{
	logger_name = _name.c_str();
}

} // EO namespace muscle

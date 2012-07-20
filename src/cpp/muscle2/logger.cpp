#include "logger.hpp"

#include <stdlib.h>
#include <cstdio>
#include <stdarg.h>
#include <string>
#include <iomanip>
#include <iostream>

using namespace std;

namespace muscle {

void logger::log_message(muscle_loglevel_t level, string message, ...)
{
	va_list args;
	va_start(args, message);
	logger::format(level, message, args);
	va_end(args);
}

void logger::severe(string message, ...)
{
	va_list args;
	va_start(args, message);
	logger::log_message(MUSCLE_LOG_SEVERE, message, args);
	va_end(args);
}

void logger::warning(string message, ...)
{
	va_list args;
	va_start(args, message);
	logger::log_message(MUSCLE_LOG_WARNING, message, args);
	va_end(args);
}

void logger::info(string message, ...)
{
	va_list args;
	va_start(args, message);
	logger::log_message(MUSCLE_LOG_INFO, message, args);
	va_end(args);
}

void logger::config(string message, ...)
{
	va_list args;
	va_start(args, message);
	logger::log_message(MUSCLE_LOG_CONFIG, message, args);
	va_end(args);
}

void logger::fine(string message, ...)
{
	va_list args;
	va_start(args, message);
	logger::log_message(MUSCLE_LOG_FINE, message, args);
	va_end(args);
}

void logger::finer(string message, ...)
{
	va_list args;
	va_start(args, message);
	logger::log_message(MUSCLE_LOG_FINER, message, args);
	va_end(args);
}

void logger::finest(string message, ...)
{
	va_list args;
	va_start(args, message);
	logger::log_message(MUSCLE_LOG_FINEST, message, args);
	va_end(args);
}

void logger::format(muscle_loglevel_t level, string message, va_list args)
{
	const char *fmt = message.c_str();
	int sz;
	if ((sz = vsnprintf(NULL, 0, fmt, args) + 1) <= 0) return; // 1 for \0, return on problems
	char *c_msg = (char *)malloc(sz*sizeof(char));
	if (c_msg == NULL) return; //ignore out of memory here.
	if (vsnprintf(c_msg, sz, fmt, args) < 0) return; //ignore other problems here.
	string cpp_msg = (string)c_msg;
	
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
	default:
		str = "OTHER";
		break;
	}

	cout << "[" << setw(7) << str << "] " << cpp_msg << endl;
	free(c_msg);
}

} // EO namespace muscle

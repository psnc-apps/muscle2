/*
Copyright 2008,2009 Complex Automata Simulation Technique (COAST) consortium

GNU Lesser General Public License

This file is part of MUSCLE (Multiscale Coupling Library and Environment).

    MUSCLE is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    MUSCLE is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/

#ifndef MUSCLE_LOGGER_H
#define MUSCLE_LOGGER_H

#include <jni.h> 
#include <vector>
#include <iostream>
#include <stdexcept>
#include <sstream>
#include <cassert> 

#if __GNUC__ >= 4
	#include <tr1/memory>
#else
	#include <memory>
#endif

#include <JNIMethod.h>
#include <JNITool.h>
#include <access/JNICounterpart.h>
#include "LogLevel.h"
#include "LogHandler.h"
#include <logging/StreamLogHandler.h>

namespace muscle {


#if defined(__GNUC__) || (defined(__MWERKS__) && (__MWERKS__ >= 0x3000)) || (defined(__ICC) && (__ICC >= 600))
# define MUSCLE_FUNCTION __PRETTY_FUNCTION__
#elif defined(__DMC__) && (__DMC__ >= 0x810)
# define MUSCLE_FUNCTION __PRETTY_FUNCTION__
#elif defined(__FUNCSIG__)
# define MUSCLE_FUNCTION __FUNCSIG__
#elif (defined(__INTEL_COMPILER) && (__INTEL_COMPILER >= 600)) || (defined(__IBMCPP__) && (__IBMCPP__ >= 500))
# define MUSCLE_FUNCTION __FUNCTION__
#elif defined(__BORLANDC__) && (__BORLANDC__ >= 0x550)
# define MUSCLE_FUNCTION __FUNC__
#elif defined(__STDC_VERSION__) && (__STDC_VERSION__ >= 199901)
# define MUSCLE_FUNCTION __func__
#else
# define MUSCLE_FUNCTION "(unknown)"
#endif


class LogHandler;

/**
provides a logging mechanism
control verbosity via a predefined LogLevel
\author Jan Hegewald
*/
class Logger
{
public:	
	Logger();
	Logger(std::tr1::shared_ptr<LogHandler> newHandler);
	Logger(std::tr1::shared_ptr<LogHandler> newHandler, std::tr1::shared_ptr<LogLevel> newLevel);

	void setHandler(std::tr1::shared_ptr<LogHandler> newHandler);
	
	void log(const std::string & file, const int & line, const std::string & function, const std::tr1::shared_ptr<LogLevel> & level, const std::string & msg);

	bool isLoggable(const std::tr1::shared_ptr<LogLevel> & l)
	{
		return (*l) >= (*level);
	}
	
	static const std::tr1::shared_ptr<LogHandler> defaultHandler()
	{
		return std::tr1::shared_ptr<LogHandler>( new StreamLogHandler(&std::cout) );
	} 

	static const std::tr1::shared_ptr<LogLevel> defaultLevel()
	{
		return LogLevel::WARNING;
	} 

private:
	std::tr1::shared_ptr<LogHandler> handler;
	std::tr1::shared_ptr<LogLevel> level;

};


} // EO namespace muscle
#endif

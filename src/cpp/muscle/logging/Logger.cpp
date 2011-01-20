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

#include <logging/Logger.h>

#include "LogHandler.h"

namespace muscle {
/**
see header for class description
\author Jan Hegewald
*/


Logger::Logger()
	: handler(Logger::defaultHandler()), level(Logger::defaultLevel())
{
}


//
Logger::Logger(std::tr1::shared_ptr<LogHandler> newHandler)
	: handler(newHandler), level(Logger::defaultLevel())
{
}


//
Logger::Logger(std::tr1::shared_ptr<LogHandler> newHandler, std::tr1::shared_ptr<LogLevel> newLevel)
	: handler(newHandler), level(newLevel)
{
}


//
void Logger::log(const std::string & file, const int & line, const std::string & function, const std::tr1::shared_ptr<LogLevel> & level, const std::string & msg)
{
	if( isLoggable(level) )
	{
		handler->log(file, line, function, level, msg);
	}
}

} // EO namespace muscle

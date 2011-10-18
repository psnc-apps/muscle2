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

#ifndef MUSCLE_STREAMLOGHANDLER_H
#define MUSCLE_STREAMLOGHANDLER_H

#include <jni.h> 
#include <vector>
#include <iostream>
#include <stdexcept>
#include <sstream>
#include <cassert> 
#include <ctime>

#include "LogHandler.h"

namespace muscle {

/**
pure c++ logging without any java stuff
writes log messages to a std::ostream
\author Jan Hegewald
*/
class StreamLogHandler : public LogHandler
{
public:
	
	StreamLogHandler(std::ostream * newLogStream);
		
	void log(const std::string & file, const int & line, const std::string & function, const std::tr1::shared_ptr<LogLevel> & level, const std::string & msg);

private:
	std::ostream * logStream;
};


} // EO namespace muscle
#endif

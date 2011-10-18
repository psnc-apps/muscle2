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

#include <logging/StreamLogHandler.h>

#include <sstream>

#include "LogLevel.h"

namespace muscle {
/**
see header for class description
\author Jan Hegewald
*/


StreamLogHandler::StreamLogHandler(std::ostream * newLogStream)
	: logStream(newLogStream)
{
}
	
void StreamLogHandler::log(const std::string & file, const int & line, const std::string & function, const std::tr1::shared_ptr<LogLevel> & level, const std::string & msg)
{
	// get date string without trailing \n
	time_t rawtime;
	time ( &rawtime );
	std::ostringstream oss;
	oss<<ctime(&rawtime);
	std::string date = oss.str();
	date = date.substr(0, date.size()-1);
	
	// write to our stream
	(*logStream) <<date<<" "<<file<<":"<<line<<" "<<function<<"\n"
		<<level->name<<": "<<msg<<std::endl;
}

} // EO namespace muscle

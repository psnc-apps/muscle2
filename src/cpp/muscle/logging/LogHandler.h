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

#ifndef MUSCLE_LOGHANDLER_H
#define MUSCLE_LOGHANDLER_H

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

namespace muscle {

class LogLevel;

/**
performs the actual logging within a Logger
\author Jan Hegewald
*/
class LogHandler
{
public:
		
	virtual ~LogHandler()
	{}
	
	virtual void log(const std::string & file, const int & line, const std::string & function, const std::tr1::shared_ptr<LogLevel> & level, const std::string & msg) = 0;
	
private:
};


} // EO namespace muscle
#endif

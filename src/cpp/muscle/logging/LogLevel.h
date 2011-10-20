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

#ifndef MUSCLE_LOGLEVEL_H
#define MUSCLE_LOGLEVEL_H

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

/**
provides predefined lod levels to control verbosity of a logger
\author Jan Hegewald
*/
class LogLevel
{
public:
	LogLevel(std::string n, jint v)
		: name(n), value(v)
	{
	}
	
	bool operator >=(const LogLevel & b) const
	{
		return value >= b.value;
	}

	static jobject javaLevel(JNIEnv *env, const std::tr1::shared_ptr<LogLevel> & cppLevel);
	static LogLevel coarsestLoggableLevel(JNIEnv *env, jobject& javaLogger);

//
private:
	static LogLevel cppLevel(JNIEnv*& env, jobject& jLevel);


//
public:
	//const std::tr1::shared_ptr<LogLevel> OFF; // Integer.MAX_VALUE
	static const std::tr1::shared_ptr<LogLevel> SEVERE; // highest value
	static const std::tr1::shared_ptr<LogLevel> WARNING;
	static const std::tr1::shared_ptr<LogLevel> INFO;
	static const std::tr1::shared_ptr<LogLevel> CONFIG;
	static const std::tr1::shared_ptr<LogLevel> FINE;
	static const std::tr1::shared_ptr<LogLevel> FINER;
	static const std::tr1::shared_ptr<LogLevel> FINEST; // lowest value
	//const std::tr1::shared_ptr<LogLevel> OFF; // Integer.MIN_VALUE

	const std::string name;
	const jint value;

//
private:
	static const std::string CLASSNAME; // name of the Java class

};


} // EO namespace muscle
#endif

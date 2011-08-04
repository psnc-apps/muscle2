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

#ifndef MUSCLE_JAVALOGHANDLER_H
#define MUSCLE_JAVALOGHANDLER_H

#include <jni.h> 
#include <vector>
#include <iostream>
#include <stdexcept>
#include <sstream>
#include <cassert> 

#include <JNIMethod.h>
#include <JNITool.h>
#include <logging/LogHandler.h>
#include <access/JNICounterpart.h>
#include <access/MethodCaller.h>


namespace muscle {

/**
writes log messages via a Java logger
\author Jan Hegewald
*/
class JavaLogHandler : public LogHandler, public JNICounterpart
{
public:
	
	JavaLogHandler(JNIEnv* newEnv, jobject newLogger);
	
	void log(const std::string & file, const int & line, const std::string & function, const std::tr1::shared_ptr<LogLevel> & level, const std::string & msg);
	

public:
	static std::tr1::shared_ptr<JavaLogHandler> create(JNIEnv*& env, jobject obj);
	static const std::string CLASSNAME; // name of the Java class	

private:
	std::tr1::shared_ptr< MethodCaller<void> > logMethod;
};

} // EO namespace muscle
#endif

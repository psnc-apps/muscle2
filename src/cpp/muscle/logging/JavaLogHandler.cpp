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

#include "JavaLogHandler.h"

#include "LogLevel.h"


namespace muscle {
/**
see header for class description
\author Jan Hegewald
*/

const std::string JavaLogHandler::CLASSNAME = "java/util/logging/Logger";


//
JavaLogHandler::JavaLogHandler(JNIEnv* newEnv, jobject newLogger)
	: JNICounterpart(newEnv, newLogger, CLASSNAME)
{
	// get log method of the java object
	std::vector<std::string> argTypes;
	argTypes.push_back( JNITool::fieldDescriptor("java/util/logging/Level") );
	argTypes.push_back( JNITool::fieldDescriptor("java/lang/String") );
	const std::string signature = JNIMethod::signature(JNITool::fieldDescriptor<void>(), argTypes );
	
	const std::string methodName = "log";
	
	logMethod = std::tr1::shared_ptr<MethodCaller<void> >( new MethodCaller<void>(newEnv, newLogger, JavaLogHandler::CLASSNAME, methodName, signature) );
}


/**
create a java logger whose name is based on the classname of the jobject
*/
std::tr1::shared_ptr<JavaLogHandler> JavaLogHandler::create(JNIEnv*& env, jobject obj)
{
	std::string name = JNITool::classNameForObject(env, obj);
	name += ".cpp";
	
	jclass loggerCls = env->FindClass(JavaLogHandler::CLASSNAME.c_str());
	assert(loggerCls != NULL);
	jmethodID mid = env->GetStaticMethodID( loggerCls, "getLogger", JNIMethod::signature(JNITool::fieldDescriptor(JavaLogHandler::CLASSNAME), JNITool::fieldDescriptor("java/lang/String")).c_str() );
	JNITool::catchJREException(env, __FILE__, __LINE__);
	jobject jlogger = env->CallStaticObjectMethod(loggerCls, mid, JNITool::jstringFromString(env, name));
	JNITool::catchJREException(env, __FILE__, __LINE__);
	
	std::tr1::shared_ptr<JavaLogHandler> handler(new JavaLogHandler(env, jlogger));
	return handler;
}


//
void JavaLogHandler::log(const std::string & file, const int & line, const std::string & function, const std::tr1::shared_ptr<LogLevel> & level, const std::string & msg)
{		
	logMethod->call(VABEGIN::FIRST, LogLevel::javaLevel(getEnv(), level), JNITool::jstringFromString(getEnv(), msg));
}


} // EO namespace muscle

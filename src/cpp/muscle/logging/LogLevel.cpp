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

#include "LogLevel.h"
#include <access/NativeAccess.h>


namespace muscle {
/**
see header for class description
\author Jan Hegewald
*/

const std::string LogLevel::CLASSNAME = "java/util/logging/Level";

const std::tr1::shared_ptr<LogLevel> LogLevel::SEVERE( new LogLevel("SEVERE", 1000) );
const std::tr1::shared_ptr<LogLevel> LogLevel::WARNING( new LogLevel("WARNING", 900) );
const std::tr1::shared_ptr<LogLevel> LogLevel::INFO( new LogLevel("INFO", 800) );
const std::tr1::shared_ptr<LogLevel> LogLevel::CONFIG( new LogLevel("CONFIG", 700) );
const std::tr1::shared_ptr<LogLevel> LogLevel::FINE( new LogLevel("FINE", 500) );
const std::tr1::shared_ptr<LogLevel> LogLevel::FINER( new LogLevel("FINER", 400) );
const std::tr1::shared_ptr<LogLevel> LogLevel::FINEST( new LogLevel("FINEST", 300) );

	
// TODO: store these and access via singleton
/**
converts a muscle::logging::LogLevel to a java.util.logging.Level
*/
jobject LogLevel::javaLevel(JNIEnv *env, const std::tr1::shared_ptr<LogLevel> & cppLevel)
{
	jclass levelCLS = env->FindClass(CLASSNAME.c_str());
	JNITool::catchJREException(env, __FILE__, __LINE__);

	// get one of the static loggers from the java Logger class
//	jfieldID fid = env->GetStaticFieldID(levelCLS, cppLevel.name.c_str(), JNITool::fieldDescriptor(CLASSNAME).c_str());
//	if (fid == NULL)
//	{
//		std::stringstream exceptionMessage;
//		exceptionMessage << __FILE__ << ":" << __LINE__ << "(fid == NULL)";
//		throw std::runtime_error(exceptionMessage.str());
//	}
//
//	return env->GetStaticObjectField(levelCLS, fid);


	// create (or use default) java Logger
	jmethodID mid = env->GetStaticMethodID(levelCLS, "parse", JNIMethod::signature(JNITool::fieldDescriptor(CLASSNAME), JNITool::fieldDescriptor("java/lang/String")).c_str());
	JNITool::catchJREException(env, __FILE__, __LINE__);

	// use the int part of the level here, because the Logger.parse method will create a new level if this level does not already exist
	// this would not work with the level name
	std::ostringstream oss;
	oss<<cppLevel->value;
	std::string valString = oss.str();
	jobject jLevel = env->CallStaticObjectMethod(levelCLS, mid, JNITool::jstringFromString(env, valString));	
	JNITool::catchJREException(env, __FILE__, __LINE__);

	return jLevel;
}


/**
returns current coarsest loggable level of given java logger
*/
LogLevel LogLevel::coarsestLoggableLevel(JNIEnv *env, jobject& javaLogger)
{
	JNITool::assertInstanceOf(env, javaLogger, java_util_logging_Logger::_CLASSNAME(), __FILE__, __LINE__);

	jobject jLevel = javatool_LoggerTool::loggableLevel_Logger(env, javaLogger);

	return LogLevel::cppLevel(env, jLevel);
}


/**
converts a java.util.logging.Level to a muscle::logging::LogLevel 
*/
LogLevel LogLevel::cppLevel(JNIEnv*& env, jobject& jLevel)
{
	JNITool::assertInstanceOf(env, jLevel, CLASSNAME, __FILE__, __LINE__);

	// get the name of the logger
	jmethodID mid = env->GetMethodID(env->GetObjectClass(jLevel), "getName", JNIMethod::signature(JNITool::fieldDescriptor("java/lang/String")).c_str());
	JNITool::catchJREException(env, __FILE__, __LINE__);

	jstring jName = (jstring)env->CallObjectMethod(jLevel, mid);	
	JNITool::catchJREException(env, __FILE__, __LINE__);
	bool isNull;
	std::string name = JNITool::stringFromJString(env, jName, isNull);
	if(isNull)
	{
		std::stringstream exceptionMessage;
		exceptionMessage << __FILE__ << ":" << __LINE__ << "name of log level is null";
		throw std::runtime_error(exceptionMessage.str());
	}
	
	// get the int value of the logger
	mid = env->GetMethodID(env->GetObjectClass(jLevel), "intValue", JNIMethod::signature(JNITool::fieldDescriptor<jint>()).c_str());
	JNITool::catchJREException(env, __FILE__, __LINE__);

	jint value = env->CallIntMethod(jLevel, mid);	
	JNITool::catchJREException(env, __FILE__, __LINE__);

	LogLevel level(name, value);
	return level;
}


} // EO namespace muscle

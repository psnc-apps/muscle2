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

#include <access/KernelController.h>

#include <JNITool.h>
#include <logging/Logger.h>
#include <logging/JavaLogHandler.h>
#include <access/MethodIDDescription.h>
#include <access/NativeAccess.h>

namespace muscle {
/**
see header for class description
\author Jan Hegewald
*/


//
KernelController::KernelController(JNIEnv *newEnv, jobject newKernelController)
	: JNICounterpart(newEnv, newKernelController, muscle_core_kernel_CAController::_CLASSNAME())
	, logger(NULL)
{
	// get willStop method of the java object
	willStopMethod = muscle_core_kernel_CAController::_willStop(newEnv, newKernelController);
}

//
KernelController::KernelController(JNIEnv* newEnv, jobject newKernelController, std::string newCLASSNAME)
	: JNICounterpart(newEnv, newKernelController, newCLASSNAME), logger(NULL)
{
	// get willStop method of the java object
	willStopMethod = muscle_core_kernel_CAController::_willStop(newEnv, newKernelController);
}


//
KernelController::~KernelController()
{
	if(logger != NULL)
		delete logger;
}


//
Logger& KernelController::getLogger()
{
	if(logger == NULL)
	{
		// init our logger
		// get logger object from java kernel
		std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > getLoggerMethod = muscle_core_kernel_CAController::_getLogger(getEnv(), getDelegate());
		jobject javaLogger = getLoggerMethod->call();

		JNITool::catchJREException(getEnv(), __FILE__, __LINE__);
		
		// determine current coarsest loggable level of the java logger
		LogLevel level = LogLevel::coarsestLoggableLevel(getEnv(), javaLogger); 
		
		std::tr1::shared_ptr<LogHandler> handler( new JavaLogHandler(getEnv(), javaLogger) );		
		std::tr1::shared_ptr<LogLevel> level_ptr( new LogLevel(level.name, level.value) );
		logger = new Logger(handler, level_ptr);
	}
	
	return (*logger);
}


/**
see java class muscle.core.kernel.CAController
*/
std::string KernelController::getCxAPath()
{
	return CxAEnvironment::getPath(getEnv(), getDelegate());
}


/**
see java class muscle.core.kernel.CAController
*/
std::string KernelController::getKernelPath()
{
	return KernelEnvironment::getPath(getEnv(), getDelegate());
}


/**
see java class muscle.core.kernel.CAController
*/
std::string KernelController::getTmpPath()
{
	return KernelEnvironment::getTmpPath(getEnv(), getDelegate());
}


/**
returns old style properties as one single string with key value format
*/
std::string KernelController::getCxAProperties()
{
	return CxAEnvironment::getProperties(getEnv(), getDelegate());
}


/**
returns a CxA property for a given key, or null if key does not exist
*/
std::string KernelController::getCxAProperty(const std::string & key, bool & isNull)
{
	return CxAEnvironment::getProperty(getEnv(), getDelegate(), key, isNull);
}


/**
see java class muscle.core.kernel.CAController
*/
bool KernelController::willStop()
{
	jboolean stop = willStopMethod->call();
	return stop == JNI_TRUE;
}


} // EO namespace muscle

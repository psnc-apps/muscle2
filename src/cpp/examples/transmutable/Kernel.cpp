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

#include "Kernel.h"

#include <JNITool.h>

#include <access/ConduitExitArray.h>
#include <access/ConduitExitSingle.h>
#include <access/ConduitEntranceArray.h>
#include <access/ConduitEntranceSingle.h>

#include <logging/StreamLogHandler.h>
#include <logging/Logger.h>
#include <logging/JavaLogHandler.h>
#include <access/KernelController.h>
#include <Version.h>

using namespace muscle;
using namespace std;

/**
a native kernel which sends and receives an array of double
\author Jan Hegewald
*/
JNIEXPORT void JNICALL Java_examples_transmutable_Kernel_callNative
  (JNIEnv* env, jobject obj, jint length, jobject exitJref, jobject entranceJref)
{
	try
	{
		cout<<"c++: begin "<<__FILE__<<endl;
		
		KernelController kernel(env, obj);

		cout<<"kernel tmp path: "<<kernel.getTmpPath()<<endl;
		bool isNull;
		cout<<"CxA property 'max_timesteps': "<<kernel.getCxAProperty("max_timesteps", isNull)<<"\n"<<endl;
		cout<<"CxA properties:\n"<<kernel.getCxAProperties()<<"\n"<<endl;
		
		ConduitExitArray<jdouble>* exitA = new ConduitExitArray<jdouble>(env, exitJref);
		ConduitEntranceArray<jdouble>* entranceA = new ConduitEntranceArray<jdouble>(env, entranceJref);

		for(int i = 0; !kernel.willStop(); i++)
		{
			cout<<"t: "<<i<<" "<<__FILE__<<endl;
			int size = 42;
			JNIArray<jdouble> data(kernel.getEnv(), size);
			data[0] = 0.42;
			data[size-1] = 4200.99;
			entranceA->send(data);

			JNIArray<jdouble>& data2 = exitA->receive();
			cout<<"data in c++ : "<<data2[0]<<" "<<data2[size-1]<<endl;
		}

	
		// use cpp-only logger
		std::tr1::shared_ptr<LogHandler> cppHandler(new StreamLogHandler(&std::cout));
		Logger cppLogger(cppHandler);
		cppLogger.log(__FILE__,__LINE__,MUSCLE_FUNCTION,LogLevel::WARNING, "my message");

		// use java logger for a CAController
		Logger& kernelLogger = kernel.getLogger();
		kernelLogger.log(__FILE__,__LINE__,MUSCLE_FUNCTION,LogLevel::INFO, "my message");

		// use java logger for generic objects
		std::tr1::shared_ptr<JavaLogHandler> javaHandler = JavaLogHandler::create(env, obj);
		Logger javaLogger(javaHandler);
		javaLogger.log(__FILE__,__LINE__,MUSCLE_FUNCTION,LogLevel::WARNING, "my message");

		delete exitA;
		delete entranceA;
	}
	catch(std::exception& e)
	{
		std::cerr<<"\nRUNTIME ERROR: "<<e.what()<<"\n"<<std::endl;
	}
	catch(...)
	{
		std::cerr<<"unknown error"<<std::endl;
	}
}




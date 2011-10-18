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

#include "Sender.h"

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
a native kernel which sends an array of double
\author Jan Hegewald
*/
JNIEXPORT void JNICALL Java_examples_simplecpp_Sender_callNative
  (JNIEnv* env, jobject obj, jobject entranceJref)
{
	try
	{
		cout<<"c++: begin "<<__FILE__<<endl;

		KernelController kernel(env, obj);
		ConduitEntranceArray<jdouble>* entrance = new ConduitEntranceArray<jdouble>(env, entranceJref);

		int size = 5;
		JNIArray<jdouble> dataA(kernel.getEnv(), size);

		for(int time = 0; !kernel.willStop(); time ++) {
								
			// process data
			for(int i = 0; i < dataA.size(); i++) {
				dataA[i] = i;
			}
						
			// dump to our portals
			entrance->send(dataA);
		}
	}
	catch(std::runtime_error& e)
	{
		std::cerr<<"\nRUNTIME ERROR: "<<e.what()<<"\n"<<std::endl;
	}
	catch(...)
	{
		std::cerr<<"unknown error"<<std::endl;
	}
}

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

#ifndef ConduitEntranceArray_F33AEF2F_2CE3_4791_BB3B_0CDFF6F558F2
#define ConduitEntranceArray_F33AEF2F_2CE3_4791_BB3B_0CDFF6F558F2

#include <jni.h> 
#include <string>

#if __GNUC__ >= 4
#include <tr1/memory>
#else
	#include <memory>
#endif

#include <JNIArray.h>
#include <JNITool.h>
#include <access/JNICounterpart.h>
#include <ArrayToJavaTool.h>
#include <access/AbstractMethodCaller.h>
#include <access/NativeAccess.h>


namespace muscle {


/**
complementary class for the Java muscle.core.JNIConduitEntrance using primitive type arrays,
e.g. muscle.core.JNIConduitEntrance<double[], Object><br>
use muscle::ConduitEntranceSingle to access single primitive data instead of arrays
\brief access an entrance which yields arrays (send)
\author Jan Hegewald
*/
template<typename PTYPE>
class ConduitEntranceArray : public JNICounterpart
{

//
public:

	//
	ConduitEntranceArray(JNIEnv* newEnv, jobject newConduitEntrance)
		: JNICounterpart(newEnv, newConduitEntrance, muscle_core_JNIConduitEntrance::_CLASSNAME())
	{
		// get target callback method
		jobject midobj = muscle_core_JNIConduitEntrance(newEnv, newConduitEntrance).toJavaJNIMethod();
		
		transmitter = new ArrayToJavaTool<PTYPE>(getEnv(), newConduitEntrance, muscle_core_JNIConduitEntrance::_CLASSNAME(), midobj);
	}

	~ConduitEntranceArray()
	{
		delete transmitter;
	}

	void send(JNIArray<PTYPE> & data)
	{
		transmitter->put(data);
	}


//	//
//	void close()
//	{
//		// TODO: close java portal?
//	}

//
private:
	ArrayToJavaTool<PTYPE>* transmitter;

};


} // EO namespace muscle
#endif

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

#ifndef ConduitEntranceSingle_656D1045_534C_4358_87B7_7665D5AFB15C
#define ConduitEntranceSingle_656D1045_534C_4358_87B7_7665D5AFB15C

#include <jni.h> 
#include <string>

#if __GNUC__ >= 4
#include <tr1/memory>
#else
	#include <memory>
#endif

#include <JNITool.h>
#include <SingleToJavaTool.h>
#include <access/JNICounterpart.h>
#include <access/MethodCaller.h>
#include <access/NativeAccess.h>


namespace muscle {


/**
complementary class for the Java muscle.core.JNIConduitEntrance using single primitive type data,
e.g. muscle.core.JNIConduitEntrance<java.lang.Double, Object><br>
use muscle::ConduitEntranceArray to access arrays of primitive data instead of single values<br>
note: though java.lang.Double is an object, the muscle::ConduitEntranceSingle will yield a jdouble, not a jobject
\brief access an entrance which yields single primitive datatypes (send)
\author Jan Hegewald
*/
template<typename PTYPE>
class ConduitEntranceSingle : public JNICounterpart
{

//
public:

	//
	ConduitEntranceSingle(JNIEnv* newEnv, jobject newConduitEntrance)
		: JNICounterpart(newEnv, newConduitEntrance, muscle_core_JNIConduitEntrance::_CLASSNAME())
	{
		// get target callback method
		jobject midobj = muscle_core_JNIConduitEntrance(newEnv, newConduitEntrance).toJavaJNIMethod();
		
		transmitter = new SingleToJavaTool<PTYPE>(getEnv(), newConduitEntrance, muscle_core_JNIConduitEntrance::_CLASSNAME(), midobj);
	}

	~ConduitEntranceSingle()
	{
		delete transmitter;
	}


	void send(PTYPE & data)
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
	SingleToJavaTool<PTYPE>* transmitter;

};

} // EO namespace muscle
#endif

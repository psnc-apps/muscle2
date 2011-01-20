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

#ifndef ConduitExitSingle_2DC219BC_E715_462B_959A_171B6B2EF6EB
#define ConduitExitSingle_2DC219BC_E715_462B_959A_171B6B2EF6EB

#include <jni.h> 
#include <string>

#if __GNUC__ >= 4
#include <tr1/memory>
#else
	#include <memory>
#endif

#include <JNITool.h>
#include <SingleFromJavaTool.h>
#include <access/JNICounterpart.h>
#include <access/NativeAccess.h>


namespace muscle {


/**
complementary class for the Java muscle.core.JNIConduitExit using single primitive type data,
e.g. muscle.core.JNIConduitExit<java.lang.Double, Object><br>
use muscle::ConduitExitArray to access arrays of primitive data instead of single values<br>
note: though java.lang.Double is an object, the muscle::ConduitExitSingle will yield a jdouble, not a jobject
\brief access an exit which yields single primitive datatypes (receive)
\author Jan Hegewald
*/
template<typename PTYPE>
class ConduitExitSingle : public JNICounterpart
{

//
public:

	//
	ConduitExitSingle(JNIEnv* newEnv, jobject newConduitExit)
		: JNICounterpart(newEnv, newConduitExit, muscle_core_JNIConduitExit::_CLASSNAME())
	{
		// get target callback method
		jobject midobj = muscle_core_JNIConduitExit(newEnv, newConduitExit).fromJavaJNIMethod();
		
		transmitter = new SingleFromJavaTool<PTYPE>(getEnv(), newConduitExit, muscle_core_JNIConduitExit::_CLASSNAME(), midobj);
	}

	~ConduitExitSingle()
	{
		delete transmitter;
	}


	PTYPE receive()
	{
		return transmitter->get();
	}


//	//
//	void close()
//	{
//		// TODO: close java portal?
//	}

//
private:
	SingleFromJavaTool<PTYPE>* transmitter;

};

} // EO namespace muscle
#endif

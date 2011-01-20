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

#ifndef AbstractMethodCaller_87FE631B_8EBE_47AA_A243_3B5DF286638C
#define AbstractMethodCaller_87FE631B_8EBE_47AA_A243_3B5DF286638C

#include <jni.h> 
#include <string>

#include <JNITool.h>
#include <access/CallTraits.h>
#include <VABEGIN.h>
//#include <tr1/tuple>

namespace muscle {


/**
calls a java method via JNI
\author Jan Hegewald
*/
template<typename J>
class AbstractMethodCaller
{
//
public:

	//
	AbstractMethodCaller(JNIEnv* newEnv)
		: jenv(newEnv)
	{
		jint result = newEnv->GetJavaVM(&jvm);
		if(result < 0)
			throw std::runtime_error("error obtaining JVM");
	}
	
	
	virtual ~AbstractMethodCaller()
	{
	}


	/**
	returns a reference to the JNIEnv pointer belonging to the current thread
	*/
	JNIEnv*& getEnv()
	{
		jint result = jvm->AttachCurrentThread((void **)&jenv, NULL);
		if(result != JNI_OK)
			throw std::runtime_error("error obtaining Java env");

		return jenv;
	}


	//
	virtual J call(const VABEGIN& first, ...) = 0;

	//
	virtual J callV(va_list& args) = 0;


	//
	virtual J call()
	{
		return call(VABEGIN::FIRST);
	}


//
protected:
	JNIEnv* jenv;	
	JavaVM* jvm;
};


} // EO namespace muscle
#endif

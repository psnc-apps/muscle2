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

#ifndef ConduitExitArray_82BFE08F_53B3_40DA_8A88_A361CBBAA18D
#define ConduitExitArray_82BFE08F_53B3_40DA_8A88_A361CBBAA18D

#include <jni.h> 
#include <string>

#if __GNUC__ >= 4
#include <tr1/memory>
#else
	#include <memory>
#endif

#include <JNIArray.h>
#include <JNITool.h>
#include <ArrayFromJavaTool.h>
#include <access/JNICounterpart.h>
#include <access/NativeAccess.h>


namespace muscle {


/**
complementary class for the Java muscle.core.JNIConduitExit using primitive type arrays,
e.g. muscle.core.JNIConduitExit<double[], Object><br>
use muscle::ConduitExitSingle to access single primitive data instead of arrays
\brief access an exit which yields arrays (receive)
\author Jan Hegewald
*/
template<typename PTYPE>
class ConduitExitArray : public JNICounterpart
{
private:
	typedef typename JNIArray<PTYPE>::ARRAYTYPE ARRAYTYPE;

//
public:

	//
	ConduitExitArray(JNIEnv* newEnv, jobject newConduitExit)
		: JNICounterpart(newEnv, newConduitExit, muscle_core_JNIConduitExit::_CLASSNAME()), buffer(NULL)
	{
		// get target callback method
		jobject midobj = muscle_core_JNIConduitExit(newEnv, newConduitExit).fromJavaJNIMethod();
		
		transmitter = new ArrayFromJavaTool<PTYPE>(getEnv(), newConduitExit, muscle_core_JNIConduitExit::_CLASSNAME(), midobj);
	}

	~ConduitExitArray()
	{
		if(buffer != NULL)
			delete buffer;
		delete transmitter;
	}

	JNIArray<PTYPE>& receive()
	{
		if(buffer != NULL)
			delete buffer;

		ARRAYTYPE ref = transmitter->get();
		if(ref == NULL)
		{
			std::stringstream exceptionMessage;
			exceptionMessage << __FILE__ << ":" << __LINE__ << "(jarray == NULL)";
			throw std::runtime_error(exceptionMessage.str());
		}

		buffer = new JNIArray<PTYPE>(getEnv(), ref);

		return (*buffer);
	}

//
private:
	ArrayFromJavaTool<PTYPE>* transmitter;
	JNIArray<PTYPE> * buffer;

};


} // EO namespace muscle
#endif

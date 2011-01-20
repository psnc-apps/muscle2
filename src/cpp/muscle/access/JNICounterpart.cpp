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

#include <access/JNICounterpart.h>


namespace muscle {
/**
see header for class description
\author Jan Hegewald
*/

	//
	JNICounterpart::JNICounterpart(JNIEnv* newEnv, jobject newDelegate, std::string newClassName)
		: delegate(newDelegate), className(newClassName), jvm(NULL), jenv(newEnv)
	{
		jint result = newEnv->GetJavaVM(&jvm);
		if(result < 0)
			throw std::runtime_error("error obtaining JVM");

		// see if we got the right jobject
		JNITool::assertInstanceOf(newEnv, newDelegate, newClassName, __FILE__, __LINE__);
	}
	
		
	//
	jclass JNICounterpart::getClass()
	{
		jclass cls = getEnv()->GetObjectClass(delegate);
		JNITool::catchJREException(getEnv(), __FILE__, __LINE__);
		return cls;
	}
	

	/**
	returns a reference to the JNIEnv pointer belonging to the current thread
	*/
	JNIEnv*& JNICounterpart::getEnv()
	{
		jint result = jvm->AttachCurrentThread((void **)&jenv, NULL);
		if(result != JNI_OK)
			throw std::runtime_error("error obtaining Java env");

		return jenv;
	}
	
	jobject& JNICounterpart::getDelegate()
	{
		return delegate;
	}


	//
	jmethodID JNICounterpart::getMID(const MethodIDDescription& m)
	{
		jclass delegateClass = getEnv()->GetObjectClass(delegate);
		jmethodID mid = getEnv()->GetMethodID(delegateClass, m.name.c_str(), m.signature.c_str());
		
		JNITool::catchJREException(getEnv(), __FILE__, __LINE__);
		return mid;
	}
	
	
} // EO namespace muscle

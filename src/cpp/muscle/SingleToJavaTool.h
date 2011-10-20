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

#ifndef SingleToJavaTool_H
#define SingleToJavaTool_H

#include <jni.h> 
#include <vector>
#include <iostream>
#include <stdexcept>
#include <sstream>
#include <cassert> 

#if __GNUC__ >= 4
#include <tr1/memory>
#else
	#include <memory>
#endif

#include <JNITool.h>
#include <access/JNICounterpart.h>
#include <access/MethodCaller.h>


namespace muscle {


/**
internal muscle class
provides utility functions to pass a single value to java
\author Jan Hegewald
*/
template<typename J>
class SingleToJavaTool : public JNICounterpart
{	
public:
	SingleToJavaTool(JNIEnv* newEnv, jobject newDelegate, std::string newDelegateClassName, jobject midobj)
		: JNICounterpart(newEnv, newDelegate, newDelegateClassName)
	{
		// force our jni method to accept only our argtype
		std::string jniSignature = JNIMethod::signature(JNITool::fieldDescriptor<void>(), JNITool::fieldDescriptor<J>());
		JNIMethod cmid(getEnv(), midobj, JNIMethod::NONE, jniSignature);

		assert( getEnv()->IsSameObject(newDelegate, cmid.getDelegate()) == JNI_TRUE );
		jmethodID mid = cmid.getMethodID();
		
		method = std::tr1::shared_ptr<MethodCaller<void> >( new MethodCaller<void>(newEnv, newDelegate, newDelegateClassName, mid) );
	}
	
	// fill a single primitive value with data from java
	void put(const J& data);

private:	
	std::tr1::shared_ptr< MethodCaller<void> > method;
};


} // EO namespace muscle
#endif

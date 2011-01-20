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

#ifndef JNICounterpart_EC628BCB_F2AE_476A_A2A5_898CC7247DD7
#define JNICounterpart_EC628BCB_F2AE_476A_A2A5_898CC7247DD7

#include <jni.h> 
#include <string>

#include <JNITool.h>
#include <access/MethodIDDescription.h>
#include <access/CallTraits.h>
#include <access/MethodCaller.h>


namespace muscle {

/**
simplifies the setup of a complementary class for Java classes
\author Jan Hegewald
*/
class JNICounterpart
{
//
public:
	JNICounterpart(JNIEnv* newEnv, jobject newDelegate, std::string newClassName);	
	
	jclass getClass();
	JNIEnv*& getEnv();
	jobject& getDelegate();
	
	//
	template<typename PTYPE>
	MethodCaller<PTYPE> createMethodCaller(const std::string methodName, const std::string methodSignature)
	{
		return MethodCaller<PTYPE>(getEnv(), delegate, className, methodName, methodSignature);
	}
	
//
protected:
	jmethodID getMID(const MethodIDDescription& m);
	
	
//
private:
	jobject delegate; // the java counterpart object
	std::string className;
	JavaVM* jvm;
	JNIEnv* jenv; 
};

} // EO namespace muscle
#endif

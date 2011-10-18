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

#ifndef MethodCaller_3957D579_13F2_48A2_82C5_0F162C8D94D5
#define MethodCaller_3957D579_13F2_48A2_82C5_0F162C8D94D5

#include <jni.h> 
#include <string>

#include <JNITool.h>
#include <access/CallTraits.h>
#include <access/AbstractMethodCaller.h>

namespace muscle {

/**
calls a java method via JNI
\author Jan Hegewald
*/
template<typename J>
class MethodCaller : public AbstractMethodCaller<J>
{
//
public:

	//
	MethodCaller(JNIEnv* newEnv, jobject newDelegate, const std::string newClassName, const std::string newMethodName, const std::string newMethodSignature)
		: AbstractMethodCaller<J>(newEnv), delegate(newDelegate)
	{
		// see if we got the right jobject
		JNITool::assertInstanceOf(newEnv, newDelegate, newClassName, __FILE__, __LINE__);

		jclass delegateClass = newEnv->GetObjectClass(delegate);
		jmid = newEnv->GetMethodID(delegateClass, newMethodName.c_str(), newMethodSignature.c_str());
		
		JNITool::catchJREException(newEnv, __FILE__, __LINE__);
	}


	/**
	init from existing mid
	*/
	MethodCaller(JNIEnv* newEnv, jobject newDelegate, const std::string newClassName, jmethodID newJmid)
		: AbstractMethodCaller<J>(newEnv), delegate(newDelegate), jmid(newJmid)
	{
		// see if we got the right jobject
		JNITool::assertInstanceOf(newEnv, newDelegate, newClassName, __FILE__, __LINE__);
		
		JNITool::catchJREException(newEnv, __FILE__, __LINE__);
	}


	//
	J call(const VABEGIN& first, ...)
	{
		va_list args;
		va_start(args, first);
		J result = CallTraits<J>::CallMethodV(this->getEnv(), this->delegate, this->jmid, args); // use this-> here to make gcc happy
		va_end(args);
		return result;
	}


	//
	J callV(va_list& args)
	{
		J result = CallTraits<J>::CallMethodV(this->getEnv(), this->delegate, this->jmid, args); // use this-> here to make gcc happy
		return result;
	}
	
	
	//
	using AbstractMethodCaller<J>::call;

//
private:
	jobject delegate; // the java counterpart object
	jmethodID jmid; // it seems to be safe to cache this (also with different threads)
};


// template specialization


/**
calls a java method via JNI
\author Jan Hegewald
*/
template<>
class MethodCaller<void> : public AbstractMethodCaller<void>
{
//
public:

	//
	MethodCaller(JNIEnv* newEnv, jobject newDelegate, const std::string newClassName, const std::string newMethodName, const std::string newMethodSignature)
		: AbstractMethodCaller<void>(newEnv), delegate(newDelegate)
	{
		// see if we got the right jobject
		JNITool::assertInstanceOf(newEnv, newDelegate, newClassName, __FILE__, __LINE__);

		jclass delegateClass = newEnv->GetObjectClass(delegate);
		jmid = newEnv->GetMethodID(delegateClass, newMethodName.c_str(), newMethodSignature.c_str());
		
		JNITool::catchJREException(newEnv, __FILE__, __LINE__);
	}

	//
	MethodCaller(JNIEnv* newEnv, jobject newDelegate, const std::string newClassName, jmethodID newJmid)
		: AbstractMethodCaller<void>(newEnv), delegate(newDelegate), jmid(newJmid)
	{
		// see if we got the right jobject
		JNITool::assertInstanceOf(newEnv, newDelegate, newClassName, __FILE__, __LINE__);
		
		JNITool::catchJREException(newEnv, __FILE__, __LINE__);
	}
	
	
	//
	void call(const VABEGIN& first, ...)
	{
		va_list args;
		va_start(args, first);
		CallTraits<void>::CallMethodV(getEnv(), delegate, jmid, args);
		va_end(args);
	}
	
	
	//
	void callV(va_list& args)
	{
		CallTraits<void>::CallMethodV(getEnv(), delegate, jmid, args);
	}
	
	
	//
	using AbstractMethodCaller<void>::call;
	

//
private:
	jobject delegate; // the java counterpart object
	jmethodID jmid; // it seems to be safe to cache this (also with different threads)
};

} // EO namespace muscle
#endif

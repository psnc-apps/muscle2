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

#ifndef MethodCallerStatic_179F2BAA_4F62_4334_B083_CD8326814900
#define MethodCallerStatic_179F2BAA_4F62_4334_B083_CD8326814900

#include <jni.h> 
#include <string>

#include <JNITool.h>
#include <access/CallStaticTraits.h>
#include <access/AbstractMethodCaller.h>

namespace muscle {

/**
calls a java method via JNI
\author Jan Hegewald
*/
template<typename J>
class MethodCallerStatic : public AbstractMethodCaller<J>
{
//
public:

	//
	MethodCallerStatic(JNIEnv* newEnv, jclass newCls, const std::string newClassName, const std::string newMethodName, const std::string newMethodSignature)
		: AbstractMethodCaller<J>(newEnv), cls(newCls)
	{
		// see if we got the right class
		JNITool::assertClass(newEnv, cls, newClassName, __FILE__, __LINE__);

		jmid = newEnv->GetStaticMethodID(cls, newMethodName.c_str(), newMethodSignature.c_str());
		
		JNITool::catchJREException(newEnv, __FILE__, __LINE__);
	}


	/**
	init from existing mid
	*/
	MethodCallerStatic(JNIEnv* newEnv, jclass newCls, const std::string newClassName, jmethodID newJmid)
		: AbstractMethodCaller<J>(newEnv), cls(newCls), jmid(newJmid)
	{
		// see if we got the right jobject
		JNITool::assertClass(newEnv, cls, newClassName, __FILE__, __LINE__);
		
		JNITool::catchJREException(newEnv, __FILE__, __LINE__);
	}


	//
	J call(const VABEGIN& first, ...)
	{
		va_list args;
		va_start(args, first);
		J result = CallStaticTraits<J>::CallMethodV(this->getEnv(), this->cls, this->jmid, args); // use this-> here to make gcc happy
		va_end(args);
		return result;
	}


	//
	J callV(va_list& args)
	{
		J result = CallStaticTraits<J>::CallMethodV(this->getEnv(), this->cls, this->jmid, args); // use this-> here to make gcc happy
		return result;
	}
	
	
	//
	using AbstractMethodCaller<J>::call;

//
private:
	jclass cls; // the java class owning the method
	jmethodID jmid; // it seems to be safe to cache this (also with different threads)
};


// template specialization


/**
calls a java method via JNI
\author Jan Hegewald
*/
template<>
class MethodCallerStatic<void> : public AbstractMethodCaller<void>
{
//
public:

	//
	MethodCallerStatic(JNIEnv* newEnv, jclass newCls, const std::string newClassName, const std::string newMethodName, const std::string newMethodSignature)
		: AbstractMethodCaller<void>(newEnv), cls(newCls)
	{
		// see if we got the right class
		JNITool::assertClass(newEnv, cls, newClassName, __FILE__, __LINE__);

		jmid = newEnv->GetStaticMethodID(cls, newMethodName.c_str(), newMethodSignature.c_str());
		
		JNITool::catchJREException(newEnv, __FILE__, __LINE__);
	}

	//
	MethodCallerStatic(JNIEnv* newEnv, jclass newCls, const std::string newClassName, jmethodID newJmid)
		: AbstractMethodCaller<void>(newEnv), cls(newCls), jmid(newJmid)
	{
		// see if we got the right jobject
		JNITool::assertClass(newEnv, cls, newClassName, __FILE__, __LINE__);
		
		JNITool::catchJREException(newEnv, __FILE__, __LINE__);
	}
	
	
	//
	void call(const VABEGIN& first, ...)
	{
		va_list args;
		va_start(args, first);
		CallStaticTraits<void>::CallMethodV(getEnv(), cls, jmid, args);
		va_end(args);
	}
	
	
	//
	void callV(va_list& args)
	{
		CallStaticTraits<void>::CallMethodV(getEnv(), cls, jmid, args);
	}
	
	
	//
	using AbstractMethodCaller<void>::call;
	

//
private:
	jclass cls; // the java class owning the method
	jmethodID jmid; // it seems to be safe to cache this (also with different threads)
};

} // EO namespace muscle
#endif

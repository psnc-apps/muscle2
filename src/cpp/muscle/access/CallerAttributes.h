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

#ifndef CallerAttributes_D2D4F359_FE8A_439E_8776_17561A98E0C8
#define CallerAttributes_D2D4F359_FE8A_439E_8776_17561A98E0C8


namespace muscle {

/**
calls a java method via JNI
\author Jan Hegewald
*/
template<typename J>
class CallerAttributes
{
public:

//	CallerAttributes(JNIEnv* newEnv, jobject newDelegate, const std::string newClassName, const std::string newMethodName, const std::string newMethodSignature)
//		: jenv(newEnv), delegate(newDelegate)
//	{
//		jint result = newEnv->GetJavaVM(&jvm);
//		if(result < 0)
//			throw std::runtime_error("error obtaining JVM");
//
//		// see if we got the right jobject
//		JNITool::assertInstanceOf(newEnv, newDelegate, newClassName, __FILE__, __LINE__);
//
//		jclass delegateClass = newEnv->GetObjectClass(delegate);
//		jmid = newEnv->GetMethodID(delegateClass, newMethodName.c_str(), newMethodSignature.c_str());
//		
//		JNITool::catchJREException(newEnv, __FILE__, __LINE__);
//	}

	CallerAttributes(JNIEnv* newEnv, jobject newDelegate, const std::string newClassName, jmethodID newJmid)
		: jenv(newEnv), delegate(newDelegate), jmid(newJmid)
	{
		jint result = newEnv->GetJavaVM(&jvm);
		if(result < 0)
			throw std::runtime_error("error obtaining JVM");

		// see if we got the right jobject
		JNITool::assertInstanceOf(newEnv, newDelegate, newClassName, __FILE__, __LINE__);
		
		JNITool::catchJREException(newEnv, __FILE__, __LINE__);
	}


	//
	J call(const VABEGIN& first, ...)
	{
		va_list args;
		va_start(args, first);
		J result = CallTraits<J>::CallMethodV(getEnv(), delegate, jmid, args);
		va_end(args);
		return result;
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
private:
	JNIEnv* jenv;	
public:
	JavaVM* jvm;
	jobject delegate; // the java counterpart object
	jmethodID jmid; // it seems to be safe to cache this (also with different threads)
};


// template specialization


/**
calls a java method via JNI
\author Jan Hegewald
*/
template<>
class CallerAttributes<void>
{
public:

//	CallerAttributes(JNIEnv* newEnv, jobject newDelegate, const std::string newClassName, const std::string newMethodName, const std::string newMethodSignature)
//		: jenv(newEnv), delegate(newDelegate)
//	{
//		jint result = newEnv->GetJavaVM(&jvm);
//		if(result < 0)
//			throw std::runtime_error("error obtaining JVM");
//
//		// see if we got the right jobject
//		JNITool::assertInstanceOf(newEnv, newDelegate, newClassName, __FILE__, __LINE__);
//
//		jclass delegateClass = newEnv->GetObjectClass(delegate);
//		jmid = newEnv->GetMethodID(delegateClass, newMethodName.c_str(), newMethodSignature.c_str());
//		
//		JNITool::catchJREException(newEnv, __FILE__, __LINE__);
//	}

	CallerAttributes(JNIEnv* newEnv, jobject newDelegate, const std::string newClassName, jmethodID newJmid)
		: jenv(newEnv), delegate(newDelegate), jmid(newJmid)
	{
		jint result = newEnv->GetJavaVM(&jvm);
		if(result < 0)
			throw std::runtime_error("error obtaining JVM");

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
private:
	JNIEnv* jenv;	
public:
	JavaVM* jvm;
	jobject delegate; // the java counterpart object
	jmethodID jmid; // it seems to be safe to cache this (also with different threads)
};


} // EO namespace muscle
#endif

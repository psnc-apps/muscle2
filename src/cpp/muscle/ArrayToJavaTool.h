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

#ifndef ARRAYTOJAVATOOL_H
#define ARRAYTOJAVATOOL_H

#include <jni.h> 
#include <vector>
#include <iostream>
#include <stdexcept>
#include <sstream>
#include <cassert> 

#include <ArrayTraits.h>
#include <JNIMethod.h>
#include <JNITool.h>
#include <access/JNICounterpart.h>
//#include <access/MethodCallerVoid.h>
#include <access/MethodCaller.h>


namespace muscle {

/**
internal muscle class
provides utility functions to pass arrays to java
\author Jan Hegewald
*/
template<typename T, typename Traits = ArrayTraits<T> >
class ArrayToJavaTool : public JNICounterpart
{	
private:
	typedef typename Traits::ARRAYTYPE ARRAYTYPE;
	typedef typename Traits::PTYPE PTYPE;

public:
	ArrayToJavaTool(JNIEnv* newEnv, jobject newDelegate, std::string newDelegateClassName, jobject midobj)
		: JNICounterpart(newEnv, newDelegate, newDelegateClassName)
	{
		// force our jni method to accept only our argtype
		std::string jniSignature = JNIMethod::signature(JNITool::fieldDescriptor<void>(), JNITool::fieldDescriptor<ARRAYTYPE>());
		JNIMethod cmid(getEnv(), midobj, JNIMethod::NONE, jniSignature);

		assert( getEnv()->IsSameObject(newDelegate, cmid.getDelegate()) == JNI_TRUE );
		jmethodID mid = cmid.getMethodID(); // it seems to be safe to cache this (also with different threads)
		
		method = std::tr1::shared_ptr<MethodCaller<void>  >( new MethodCaller<void>(newEnv, newDelegate, newDelegateClassName, mid) );
	}
	

	//
	void put(JNIArray<T> & data)
	{
		jsize size = data.size();
		assert(size >= 0);
		//data.commit();
		put(&size, &data[0]);
	}


	//
	void put(const std::vector<T> & data)
	{
		jsize size = data.size();
		assert(size >= 0);
		put(&size, &data[0]);
	}

	
	//
	void put(const jsize* const & sizeX, const jsize* const & sizeY, const T* const & data)
	{
		jsize size = (*sizeX)*(*sizeY);
		put(&size, data);
	}
	
	
	//
	void put(const jsize* const & size, const T* const & data)
	{
		JNIEnv* env = getEnv();

		// make sure the jvm is clean
		JNITool::catchJREException(env, __FILE__, __LINE__);

//		cout<<"c++ send# "<<(sendCount++)<<" size:"<<(*size)<<" "<<__FILE__<<":"<<__LINE__<<endl;

		ARRAYTYPE jdata = Traits::NewArray(env, *size); /*alloc*/ // maybe we should only alloc once for the whole calc loop to speed up things

		if(jdata == NULL) {
			std::stringstream exceptionMessage;
			exceptionMessage << __FILE__ << ":" << __LINE__ << "(jdata == NULL)";
			throw std::runtime_error(exceptionMessage.str());
		}

		// we must call a matching Release<Type>ArrayElements 1) Get<Type>ArrayElements 2) Release<Type>ArrayElements
		// this allows our native code to obtain a direct pointer to primitive arrays 
		// get a pointer to a primitive array inside the JVM
		jboolean isCopy;
		PTYPE* tmpdata = Traits::GetArrayElements(env, jdata, &isCopy); // do not forget to release this!
		if(tmpdata == NULL) {
			std::stringstream exceptionMessage;
			exceptionMessage << __FILE__ << ":" << __LINE__ << "(tmpdata == NULL)"; /* out of memory */
			throw std::runtime_error(exceptionMessage.str());
		}

		// TODO get rid of the copying
		for(jsize i = 0; i < (*size); i++)
		{
			tmpdata[i] = data[i];
		}

		// release our hold on javas array
		// (this tells the java garbage collector we no longer need the tmpdata)
		jint mode = 0; // copy back the content and free the tmpdata buffer
		Traits::ReleaseArrayElements(env, jdata, tmpdata, mode);

		method->call(VABEGIN::FIRST, jdata);

		// free local references to help the garbage collector, see section 5.2.1 jni.pdf
		env->DeleteLocalRef(jdata); 

		// make sure the jvm is clean
		JNITool::catchJREException(env, __FILE__, __LINE__);
	}


private:
	std::tr1::shared_ptr< MethodCaller<void> > method;
};


} // EO namespace muscle
#endif

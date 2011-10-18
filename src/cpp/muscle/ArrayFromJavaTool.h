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

#ifndef ARRAYFROMJAVATOOL_H
#define ARRAYFROMJAVATOOL_H

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

#include <ArrayTraits.h>
#include <JNIMethod.h>
#include <JNITool.h>
#include <JNIArray.h>
#include <access/JNICounterpart.h>

namespace muscle {

/**
internal muscle class
provides utility functions to retrieve arrays from java
\author Jan Hegewald
*/
template<typename T, typename Traits = ArrayTraits<T> >
class ArrayFromJavaTool : public JNICounterpart
{	
private:
	typedef typename Traits::ARRAYTYPE ARRAYTYPE;
	typedef typename Traits::PTYPE PTYPE;

public:
	ArrayFromJavaTool(JNIEnv* newEnv, jobject newDelegate, std::string newDelegateClassName, jobject midobj)
		: JNICounterpart(newEnv, newDelegate, newDelegateClassName)
	{
		// force our jni method to return only our returntype
		std::string jniSignature = JNIMethod::signature(JNITool::fieldDescriptor<ARRAYTYPE>());
		JNIMethod cmid(getEnv(), midobj, JNIMethod::NONE, jniSignature);
		
		assert( getEnv()->IsSameObject(newDelegate, cmid.getDelegate()) == JNI_TRUE );
		
		jmethodID mid = cmid.getMethodID();
		method = std::tr1::shared_ptr<MethodCaller<ARRAYTYPE> >( new MethodCaller<ARRAYTYPE>(newEnv, newDelegate, newDelegateClassName, mid) );
	}
	
	// return jarray from our connected java method
	ARRAYTYPE get()
	{
		return method->call();
	}

	// fill a vector with data from a java array
	void get(std::vector<T> & data)
	{		
		ARRAYTYPE jdata = method->call(); // do we have to release this??
		jsize size = data.size();
		assert(size >= 0);
		get(&size, &data[0], jdata);
	}

	// fill a vector with data from a java array
	// memory is allocated if necessary
	void getAndAllowResize(std::vector<T> & data)
	{		
		ARRAYTYPE jdata = method->call(); // do we have to release this??
		jsize requiredSize = getEnv()->GetArrayLength(jdata);
		if( requiredSize != (int)data.size() )
		{
			data.resize(requiredSize);
		}
		assert(requiredSize >= 0);
		get(&requiredSize, &data[0], jdata);
	}


	// fill a c-style array with data from a java array
	// warning: as we currently can not handle reallocated arrays in fortran, do not reallocate data here (therefore the const pointer)
	void get(const jsize* const & size, T* const & data)
	{		
		assert(size >= 0);
		ARRAYTYPE jdata = method->call(); // do we have to release this??
		get(size, data, jdata);
	}


//	// fill a c-style array with data from a java array
//	// warning: as we currently can not handle reallocated arrays in fortran, do not reallocate data here (therefore the const pointer)
//	void get(const jsize* const & sizeX, const jsize* const & sizeY, T* const & data)
//	{		
//		ARRAYTYPE jdata = method->call(); // do we have to release this??
//		jsize size = (*sizeX)*(*sizeY);
//		get(size, data, jdata);
//	}


	// fill a c-style array with data from a java array
	// memory is allocated if necessary (fortran does not like it if memory is reallocated)
	void getAndAllowResize(jsize* const & size, T* & data)
	{		
		ARRAYTYPE jdata = method->call(); // do we have to release this??
		jsize requiredSize = getEnv()->GetArrayLength(jdata);
		if( *size != requiredSize )
		{
			T* newData = (T*)realloc(data, requiredSize * sizeof(T));
			if(newData == NULL)
			{
				std::stringstream exceptionMessage;
				exceptionMessage << __FILE__ << ":" << __LINE__ << "memory allocation failed for size <"<<requiredSize<<">)";
				throw std::runtime_error(exceptionMessage.str());
			}
			else {
				*size = requiredSize;
				data = newData;
			}
		}		
		get(size, data, jdata);
	}


private:
	// share this for our various get array variants 
	void get(const jsize* const & size, T* const & data, ARRAYTYPE & jdata)
	{
		JNIEnv* env = getEnv();

		// make sure the jvm is clean
		JNITool::catchJREException(env, __FILE__, __LINE__);
		
		jsize dataSize = getEnv()->GetArrayLength(jdata);
		if( dataSize != (*size) )
		{
			std::stringstream exceptionMessage;
			exceptionMessage << __FILE__ << ":" << __LINE__ << "(dataSize<"<<dataSize<<"> != size<"<<((*size))<<">)";
			throw std::runtime_error(exceptionMessage.str());
		}	

		if(jdata == NULL)
		{
			std::stringstream exceptionMessage;
			exceptionMessage << __FILE__ << ":" << __LINE__ << "(jdata == NULL)";
			throw std::runtime_error(exceptionMessage.str());
		}

		// get a pointer to a primitive array inside the JVM
		jboolean isCopy;
		PTYPE* tmpdata = Traits::GetArrayElements(env, jdata, &isCopy); // do not forget to release this!
		if(tmpdata == NULL)
		{
			std::stringstream exceptionMessage;
			exceptionMessage << __FILE__ << ":" << __LINE__ << "(tmpdata == NULL)"; /* out of memory */
			throw std::runtime_error(exceptionMessage.str());
		}
		
		// TODO get rid of the copying
		for(jsize i = 0; i < dataSize; i++)
		{
			data[i] = tmpdata[i];
		}
		// release our hold on javas array
		// (this tells the java garbage collector we no longer need the tmpdata)
		jint mode = JNI_ABORT; // free the buffer without copying back the possible changes
		Traits::ReleaseArrayElements(env, jdata, tmpdata, mode);
		
		// free local references to help the garbage collector, see section 5.2.1 jni.pdf
		env->DeleteLocalRef(jdata);

		// make sure the jvm is clean
		JNITool::catchJREException(env, __FILE__, __LINE__);
	}


private:
	std::tr1::shared_ptr< MethodCaller<ARRAYTYPE> > method;
};


} // EO namespace muscle
#endif

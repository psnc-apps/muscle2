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

#ifndef ARRAYTRAITS_H
#define ARRAYTRAITS_H

#include <jni.h> 
#include <vector>
#include <iostream>
#include <stdexcept>
#include <sstream>
#include <cassert> 

#include <JNIMethod.h>
#include <JNITool.h>

namespace muscle {


/**
internal muscle class
lets us use all the JNI array calls via templates
\author Jan Hegewald
*/
template<typename P>
class ArrayTraits
{
public:
};

// template specializations


template<>
class ArrayTraits<jboolean>
{
public:
	typedef jbooleanArray ARRAYTYPE;
	typedef jboolean PTYPE;

	static PTYPE* GetArrayElements(JNIEnv* & env, ARRAYTYPE & jdata, jboolean* const & isCopy)
	{
		return env->GetBooleanArrayElements(jdata, isCopy);
	}

	static void ReleaseArrayElements(JNIEnv* & env, ARRAYTYPE & jdata, PTYPE* & cdata, jint & mode)
	{
		env->ReleaseBooleanArrayElements(jdata, cdata, mode);
	}

	static ARRAYTYPE NewArray(JNIEnv* & env, const jsize & size)
	{
		// alloc memory
		ARRAYTYPE jdata = env->NewBooleanArray(size);
		return jdata;
	}

	static void SetArrayRegion(JNIEnv* & env, ARRAYTYPE& jdata, jsize& start, jsize& len, PTYPE* & cdata)
	{
		env->SetBooleanArrayRegion(jdata, start, len, cdata);
	}
	static void SetArrayRegion(JNIEnv* & env, ARRAYTYPE& jdata, jsize& start, jsize& len, const PTYPE* & cdata)
	{
		env->SetBooleanArrayRegion(jdata, start, len, cdata);
	}
};


template<>
class ArrayTraits<jint>
{
public:
	typedef jintArray ARRAYTYPE;
	typedef jint PTYPE;

	static PTYPE* GetArrayElements(JNIEnv* & env, ARRAYTYPE & jdata, jboolean* const & isCopy)
	{
		return env->GetIntArrayElements(jdata, isCopy);
	}

	static void ReleaseArrayElements(JNIEnv* & env, ARRAYTYPE & jdata, PTYPE* & cdata, jint & mode)
	{
		env->ReleaseIntArrayElements(jdata, cdata, mode);
	}

	static ARRAYTYPE NewArray(JNIEnv* & env, const jsize & size)
	{
		// alloc memory
		ARRAYTYPE jdata = env->NewIntArray(size);
		return jdata;
	}

	static void SetArrayRegion(JNIEnv* & env, ARRAYTYPE& jdata, jsize& start, jsize& len, PTYPE* & cdata)
	{
		env->SetIntArrayRegion(jdata, start, len, cdata);
	}
	static void SetArrayRegion(JNIEnv* & env, ARRAYTYPE& jdata, jsize& start, jsize& len, const PTYPE* & cdata)
	{
		env->SetIntArrayRegion(jdata, start, len, cdata);
	}
};


template<>
class ArrayTraits<jfloat>
{
public:
	typedef jfloatArray ARRAYTYPE;
	typedef jfloat PTYPE;

	static PTYPE* GetArrayElements(JNIEnv* & env, ARRAYTYPE & jdata, jboolean* const & isCopy)
	{
		return env->GetFloatArrayElements(jdata, isCopy);
	}

	static void ReleaseArrayElements(JNIEnv* & env, ARRAYTYPE & jdata, PTYPE* & cdata, jint & mode)
	{
		env->ReleaseFloatArrayElements(jdata, cdata, mode);
	}

	static ARRAYTYPE NewArray(JNIEnv* & env, const jsize & size)
	{
		// alloc memory
		ARRAYTYPE jdata = env->NewFloatArray(size);
		return jdata;
	}

	static void SetArrayRegion(JNIEnv* & env, ARRAYTYPE& jdata, jsize& start, jsize& len, PTYPE* & cdata)
	{
		env->SetFloatArrayRegion(jdata, start, len, cdata);
	}
	static void SetArrayRegion(JNIEnv* & env, ARRAYTYPE& jdata, jsize& start, jsize& len, const PTYPE* & cdata)
	{
		env->SetFloatArrayRegion(jdata, start, len, cdata);
	}
};


template<>
class ArrayTraits<jdouble>
{
public:
	typedef jdoubleArray ARRAYTYPE;
	typedef jdouble PTYPE;

	static PTYPE* GetArrayElements(JNIEnv* & env, ARRAYTYPE & jdata, jboolean* const & isCopy)
	{
		return env->GetDoubleArrayElements(jdata, isCopy);
	}

	static void ReleaseArrayElements(JNIEnv* & env, ARRAYTYPE & jdata, PTYPE* & cdata, jint & mode)
	{
		env->ReleaseDoubleArrayElements(jdata, cdata, mode);
	}

	static ARRAYTYPE NewArray(JNIEnv* & env, const jsize & size)
	{
		// alloc memory
		ARRAYTYPE jdata = env->NewDoubleArray(size);
		return jdata;
	}

	static void SetArrayRegion(JNIEnv* & env, ARRAYTYPE& jdata, jsize& start, jsize& len, PTYPE* & cdata)
	{
		env->SetDoubleArrayRegion(jdata, start, len, cdata);
	}
	static void SetArrayRegion(JNIEnv* & env, ARRAYTYPE& jdata, jsize& start, jsize& len, const PTYPE* & cdata)
	{
		env->SetDoubleArrayRegion(jdata, start, len, cdata);
	}
};



} // EO namespace muscle
#endif

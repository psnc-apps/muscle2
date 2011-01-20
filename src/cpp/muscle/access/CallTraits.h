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

#ifndef CALLTRAITS_CE7E92E4_05E0_4495_B64B_2618C38E3FC8
#define CALLTRAITS_CE7E92E4_05E0_4495_B64B_2618C38E3FC8

#include <jni.h> 


namespace muscle {

//	void returnvoid()
//	{
//	}

/**
internal muscle class
provides traits to access the various jni Call... methods from a template
\author Jan Hegewald
*/
template<typename T>
class CallTraits
{
public:
	// this call is suitable for all return types of reference type jobject, the jarray types et al
	static inline T CallMethodV(JNIEnv*& env, jobject& obj, jmethodID& mid, va_list& args)
	{
		return (T)env->CallObjectMethodV(obj, mid, args);
	}
};

// template specializations

template<>
class CallTraits<void>
{
public:
	typedef void PTYPE;

	static inline PTYPE CallMethodV(JNIEnv* & env, jobject& obj, jmethodID& mid, va_list& args)
	{
		env->CallVoidMethodV(obj, mid, args);
	}
};

template<>
class CallTraits<jboolean>
{
public:
	typedef jboolean PTYPE;

	static inline PTYPE CallMethodV(JNIEnv*& env, jobject& obj, jmethodID& mid, va_list& args)
	{
		return env->CallBooleanMethodV(obj, mid, args);
	}
};

template<>
class CallTraits<bool>
{
public:
	typedef bool PTYPE;

	static inline PTYPE CallMethodV(JNIEnv*& env, jobject& obj, jmethodID& mid, va_list& args)
	{
		if( env->CallBooleanMethodV(obj, mid, args) )
			return JNI_TRUE;

	  return JNI_FALSE;
	}
};

template<>
class CallTraits<jint>
{
public:
	typedef jint PTYPE;

	static inline PTYPE CallMethodV(JNIEnv*& env, jobject& obj, jmethodID& mid, va_list& args)
	{
		return env->CallIntMethodV(obj, mid, args);
	}
};

template<>
class CallTraits<jfloat>
{
public:
	typedef jfloat PTYPE;

	static inline PTYPE CallMethodV(JNIEnv*& env, jobject& obj, jmethodID& mid, va_list& args)
	{
		return env->CallFloatMethodV(obj, mid, args);
	}
};

template<>
class CallTraits<jdouble>
{
public:
	typedef jdouble PTYPE;

	static inline PTYPE CallMethodV(JNIEnv*& env, jobject& obj, jmethodID& mid, va_list& args)
	{
		return env->CallDoubleMethodV(obj, mid, args);
	}
};

template<>
class CallTraits<jobject>
{
public:
	typedef jobject TYPE;

	static inline TYPE CallMethodV(JNIEnv*& env, jobject& obj, jmethodID& mid, va_list& args)
	{
		return env->CallObjectMethodV(obj, mid, args);
	}
};

} // EO namespace muscle
#endif

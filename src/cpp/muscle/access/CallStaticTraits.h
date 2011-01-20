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

#ifndef CALLSTATICTRAITS_1EC7B47A_2C23_469F_BAA2_85F659913234
#define CALLSTATICTRAITS_1EC7B47A_2C23_469F_BAA2_85F659913234

#include <jni.h> 


namespace muscle {

//	void returnvoid()
//	{
//	}

/**
internal muscle class
provides traits to access the various jni CallStatic... methods from a template
\author Jan Hegewald
*/
template<typename T>
class CallStaticTraits
{
public:
	// this call is suitable for all return types of reference type jobject, the jarray types et al
	static inline T CallMethodV(JNIEnv*& env, jclass& cls, jmethodID& mid, va_list& args)
	{
		return (T)env->CallStaticObjectMethodV(cls, mid, args);
	}
};

// template specializations

template<>
class CallStaticTraits<void>
{
public:
	typedef void PTYPE;

	static inline PTYPE CallMethodV(JNIEnv* & env, jclass& cls, jmethodID& mid, va_list& args)
	{
		env->CallStaticVoidMethodV(cls, mid, args);
	}
};

template<>
class CallStaticTraits<jboolean>
{
public:
	typedef jboolean PTYPE;

	static inline PTYPE CallMethodV(JNIEnv*& env, jclass& cls, jmethodID& mid, va_list& args)
	{
		return env->CallStaticBooleanMethodV(cls, mid, args);
	}
};

template<>
class CallStaticTraits<bool>
{
public:
	typedef bool PTYPE;

	static inline PTYPE CallMethodV(JNIEnv*& env, jclass& cls, jmethodID& mid, va_list& args)
	{
		if( env->CallStaticBooleanMethodV(cls, mid, args) )
			return JNI_TRUE;

	  return JNI_FALSE;
	}
};

template<>
class CallStaticTraits<jint>
{
public:
	typedef jint PTYPE;

	static inline PTYPE CallMethodV(JNIEnv*& env, jclass& cls, jmethodID& mid, va_list& args)
	{
		return env->CallStaticIntMethodV(cls, mid, args);
	}
};

template<>
class CallStaticTraits<jfloat>
{
public:
	typedef jfloat PTYPE;

	static inline PTYPE CallMethodV(JNIEnv*& env, jclass& cls, jmethodID& mid, va_list& args)
	{
		return env->CallStaticFloatMethodV(cls, mid, args);
	}
};

template<>
class CallStaticTraits<jdouble>
{
public:
	typedef jdouble PTYPE;

	static inline PTYPE CallMethodV(JNIEnv*& env, jclass& cls, jmethodID& mid, va_list& args)
	{
		return env->CallStaticDoubleMethodV(cls, mid, args);
	}
};

template<>
class CallStaticTraits<jobject>
{
public:
	typedef jobject TYPE;

	static inline TYPE CallMethodV(JNIEnv*& env, jclass& cls, jmethodID& mid, va_list& args)
	{
		return env->CallStaticObjectMethodV(cls, mid, args);
	}
};

} // EO namespace muscle
#endif

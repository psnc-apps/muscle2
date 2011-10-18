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

#ifndef JNITOOL_6C77CD92_8E01_4B56_9AC9_11C8A69FD499
#define JNITOOL_6C77CD92_8E01_4B56_9AC9_11C8A69FD499

#include <jni.h> 
#include<vector>
#include <iostream>
#include <stdexcept>
#include <sstream>
#include <cassert> 

#include <JNIMethod.h>

namespace muscle {

/**
static functions for miscellaneous JNI stuff
\author Jan Hegewald

note:
jni.h provides machine independent types for primitive data e.g. jint, jdouble
be careful when casting these to a plain primitive e.g. int, double because these could be machine dependent
in /System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Headers/jni.h and C:\\Programme\\Java\\jdk1.6.0_03\\include\\jni.h we have:
	jni_md.h contains the machine-dependent typedefs for
								jbyte
								jint
								jlong
and the others are:
	typedef unsigned char	jboolean;
	typedef unsigned short	jchar;
	typedef short		jshort;
	typedef float		jfloat;
	typedef double		jdouble;
it would be best not to assume anything of the length of an double, int etc
*/
class JNITool
{		
public:	
	
	static bool isAssignableFrom(JNIEnv *env, std::string parentDescriptor, std::string childDescriptor);
	static bool boolFromJboolean(const jboolean& b);
	static jlong toJlong(JNITool* tool);
	static JNITool* fromJlong(jlong longPtr);
	
	static std::string stringFromJObjectArray(JNIEnv *env, jobjectArray jArray, jint index);
	static std::string stringFromJString(JNIEnv* env, jstring jText, bool & isNull);
	static jstring jstringFromString(JNIEnv *env, const std::string str);

	static void catchJREException(JNIEnv* env, const char* file, int line);
	static void assertInstanceOf(JNIEnv* & env, const jobject & obj, const std::string className, const std::string& file, const int& line);
	static void assertClass(JNIEnv* & env, const jclass & cls, const std::string className, const std::string& file, const int& line);
	static JNIEnv* getEnv(JavaVM*& jvm);
	static std::string classNameForObject(JNIEnv*& env, const jobject& obj);
	
	static std::string fieldDescriptor(const std::string name);
	template<typename T>
	static std::string fieldDescriptor();
};


} // EO namespace muscle
#endif

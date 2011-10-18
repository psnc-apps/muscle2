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

#ifndef JNIMETHOD_H
#define JNIMETHOD_H

#include <jni.h> 
#include <string>
#include <vector>

namespace muscle {

/**
complementary class for the Java class utilities.jni.JNIMethod
\author Jan Hegewald
*/
class JNIMethod // TODO: inherit from JNICounterpart
{
//
public:

	/**
	init from a java.lang.reflect.Method object
	*/
	JNIMethod(JNIEnv *newEnv, jobject midobj/*Java JNIMethod*/)
	{
		JNIMethod(newEnv, midobj, NONE, NONE);// ignore if NONE
	}

	/**
	init from a java.lang.reflect.Method object
	*/
	JNIMethod(JNIEnv *newEnv, jobject midobj/*Java JNIMethod*/, const std::string jniMethodName, const std::string jniDescriptor);

	jobject getDelegate();

	jmethodID getMethodID();

	static std::string signature(const std::string& returnType, const std::string& argType = "");
	static std::string signature(const std::string& returnType, const std::vector<std::string>& argTypes);

//
public:
	static const std::string NONE;
//
private:
	jobject delegate;
	jmethodID methodID;
};


} // EO namespace muscle
#endif

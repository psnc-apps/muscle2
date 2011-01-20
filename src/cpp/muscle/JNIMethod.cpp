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

#include <JNIMethod.h>

#include <iostream>
#include <stdexcept>
#include <sstream>
#include <cassert>

#include <JNITool.h>
#include <access/NativeAccess.h>

namespace muscle {
/**
see header for class description
\author Jan Hegewald
*/

using namespace std;

const std::string JNIMethod::NONE = "-";

//
JNIMethod::JNIMethod(JNIEnv *env, jobject midobj/*Java JNIMethod*/, const std::string jniMethodName, const std::string jniDescriptor)
{
	// get hold on the Java class JNIMethod
	// see if we got the right jobject
	JNITool::assertInstanceOf(env, midobj, utilities_jni_JNIMethod::_CLASSNAME(), __FILE__, __LINE__);
	jclass midclass = env->FindClass(utilities_jni_JNIMethod::_CLASSNAME().c_str());
	
	// get access to the getName callback method
	jmethodID mid = env->GetMethodID(midclass, "getName", "()Ljava/lang/String;");
	JNITool::catchJREException(env, __FILE__, __LINE__);
	jstring jname = (jstring)env->CallObjectMethod(midobj, mid);	
	bool isNull;
	string name = JNITool::stringFromJString(env, jname, isNull);

	if(isNull)
		throw std::runtime_error("can not convert jstring(=NULL) to a std::string");

	if(jniMethodName != NONE && jniMethodName != name)
		throw std::runtime_error("method names do not match <"+jniMethodName+"> != <"+name+">");

	// get access to the getSignature callback method
	mid = env->GetMethodID(midclass, "getDescriptor", "()Ljava/lang/String;");
	JNITool::catchJREException(env, __FILE__, __LINE__);
	jstring jsig = (jstring)env->CallObjectMethod(midobj, mid);	
	string signature = JNITool::stringFromJString(env, jsig, isNull);

	if(isNull)
		throw std::runtime_error("can not convert jstring(=NULL) to a std::string");

	if(jniDescriptor != NONE && jniDescriptor != signature)
		throw std::runtime_error("method signatures do not match <"+jniDescriptor+"> != <"+signature+">");

	// get access to the getDelegate callback method
	mid = env->GetMethodID(midclass, "getDelegate", "()Ljava/lang/Object;");
	JNITool::catchJREException(env, __FILE__, __LINE__);
	delegate = env->CallObjectMethod(midobj, mid);
	
//	jclass delegateClass = env->GetObjectClass(delegate);
	
	// get target callback method
	// the signature we really use dies not necessarily need to be the same of the method,
	// since we may call a java.lang.Object method with a int[] (which is an Object)
	// get access to the getMethod callback method
	mid = env->GetMethodID(midclass, "getMethod", "()Ljava/lang/reflect/Method;");
	JNITool::catchJREException(env, __FILE__, __LINE__);
	jobject jmethod = (jobject)env->CallObjectMethod(midobj, mid);	
	methodID = env->FromReflectedMethod(jmethod);
	JNITool::catchJREException(env, __FILE__, __LINE__);
}


// returns the jobject on which to call the methodID
jobject JNIMethod::getDelegate()
{
	return delegate;
}


// returns the jmethodID of the method we can call on the delegate
jmethodID JNIMethod::getMethodID()
{
	return methodID;
}


// builds a jni method signature string
std::string JNIMethod::signature(const std::string& jniReturn, const std::vector<std::string>& argTypes)
{
	string jniArgs = "";
	for(int i = 0; i < (int)argTypes.size(); i++) {
		jniArgs += argTypes[i];
	}
	
	if( jniReturn.size() == 0 )
		return "("+jniArgs+")"+"V"; // only the method return type is marked as void, never the argument
	return "("+jniArgs+")"+jniReturn;
}
//
std::string JNIMethod::signature(const std::string& jniReturn, const std::string& argType)
{
	vector<string> types;
	types.push_back(argType);
	return signature(jniReturn, types);
}

//
//void JNIMethod::assertSignature(const std::string& signature)
//{
//	// get access to the JNITool.toFieldDescriptor method
//	jclass midclass = env->FindClass("utilities/jni/JNITool");
//	jmethodID mid = env->GetStaticMethodID(midclass, "toFieldDescriptor", "(Ljava/lang/String;)Ljava/lang/String;");
//	JNITool::catchJREException(env, __FILE__, __LINE__);
////	jclass typeClass = env->FindClass("Ljava/lang/String;");
//
////string typeName = "java.lang.Integer";
//string typeName = JNITool::fieldDescriptor<jint>();
//	jstring typeClass = env->NewStringUTF(typeName.c_str());
//	JNITool::catchJREException(env, __FILE__, __LINE__);
//	jstring jsig = (jstring)env->CallStaticObjectMethod(midclass, mid, typeClass);	
//	JNITool::catchJREException(env, __FILE__, __LINE__);
//	string sig = JNITool::stringFromJString(env, jsig);
////	assertSignature(signature);
//	cout<<"sigi: "<<signature<<" <-> "<<sig<<endl;
//}

} // EO namespace muscle

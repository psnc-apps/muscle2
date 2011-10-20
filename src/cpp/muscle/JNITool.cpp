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

#include <JNITool.h>

namespace muscle {
/**
see header for class description
\author Jan Hegewald
*/

using namespace std;


//
bool JNITool::isAssignableFrom(JNIEnv *env, std::string parentDescriptor, std::string childDescriptor)
{
	jclass parentClass = env->FindClass(parentDescriptor.c_str());
	jclass childClass = env->FindClass(childDescriptor.c_str());
	return boolFromJboolean( env->IsAssignableFrom(parentClass, childClass) );
}


//
bool JNITool::boolFromJboolean(const jboolean& b)
{
	return b ? JNI_TRUE : JNI_FALSE;
}


//
jlong JNITool::toJlong(JNITool* tool)
{
	jlong longPtr = reinterpret_cast<jlong>(tool);
	return longPtr;	
}


//
JNITool* JNITool::fromJlong(jlong longPtr)
{
	return reinterpret_cast<JNITool*>(longPtr);
}

//
void JNITool::catchJREException(JNIEnv* env, const char* file, int line)
{
	if(env->ExceptionCheck())
	{
		std::stringstream exceptionMessage;
		exceptionMessage << file << ":" << line << " caused a JRE exception";
		throw std::runtime_error(exceptionMessage.str());
	}
}


/**
test if the passed obj is an instance of the named java class
example className = "java/lang/String"
*/
void JNITool::assertInstanceOf(JNIEnv* & env, const jobject & obj, const std::string className, const std::string& file, const int& line)
{
	if(obj == NULL)
	{
		std::stringstream exceptionMessage;
		exceptionMessage << file << ":" << line << " jobject is NULL ";
		throw std::runtime_error(exceptionMessage.str());
	}

	// see if we got the right jobject
	// get hold on the Java class for this object
	jclass classObject = env->FindClass(className.c_str());
	if(classObject == NULL)
	{
		std::stringstream exceptionMessage;
		exceptionMessage << file << ":" << line << " can not find Java class "<<className;
		throw std::runtime_error(exceptionMessage.str());
	}
	
	// test if our jobject actually is a className class
	if(env->IsInstanceOf(obj, classObject) == JNI_FALSE)
	{
		std::stringstream exceptionMessage;
		exceptionMessage << file << ":" << line << " jobject is not a "<<className;
		throw std::runtime_error(exceptionMessage.str());
	}	
}


/**
test if the passed cls is an the named java class
example className = "java/lang/String"
*/
void JNITool::assertClass(JNIEnv* & env, const jclass & cls, const std::string className, const std::string& file, const int& line)
{
	if(cls == NULL)
	{
		std::stringstream exceptionMessage;
		exceptionMessage << file << ":" << line << " class is NULL ";
		throw std::runtime_error(exceptionMessage.str());
	}

	// get hold on the Java class for this object
	jclass classObject = env->FindClass(className.c_str());
	if(classObject == NULL)
	{
		std::stringstream exceptionMessage;
		exceptionMessage << file << ":" << line << " can not find Java class "<<className;
		throw std::runtime_error(exceptionMessage.str());
	}
	
	// test if our jobject actually is a className class
	if( (env->IsSameObject(cls, classObject)) != JNI_TRUE )
//	if(cls != classObject) // this does not work
	{
		std::stringstream exceptionMessage;
		exceptionMessage << file << ":" << line << " cls is not a "<<className;
		throw std::runtime_error(exceptionMessage.str());
	}	
}


/**
returns a reference to the JNIEnv pointer belonging to the current thread
*/
JNIEnv* JNITool::getEnv(JavaVM*& jvm)
{
	JNIEnv* env;
	jint result = jvm->AttachCurrentThread((void **)&env, NULL);
	if(result != JNI_OK)
		throw std::runtime_error("error obtaining env");

	return env;
}


/**
returns the classname of a java object
*/
std::string JNITool::classNameForObject(JNIEnv*& env, const jobject& obj)
{
	jclass cls = env->GetObjectClass(obj);
	// it does not seem to be possible to call a specialized template from the same class here
	//jmethodID getNameID = env->GetMethodID(env->GetObjectClass(cls), "getCanonicalName", JNIMethod::signature(JNITool::fieldDescriptor<void>(), JNITool::fieldDescriptor("java/lang/String")).c_str());
	jmethodID getNameID = env->GetMethodID(env->GetObjectClass(cls), "getCanonicalName", "()Ljava/lang/String;");
	jstring className = (jstring)env->CallObjectMethod(cls, getNameID);
	JNITool::catchJREException(env, __FILE__, __LINE__);
	
	bool isNull;
	return stringFromJString(env, className, isNull);
}


//
std::string JNITool::stringFromJObjectArray(JNIEnv *env, jobjectArray jArray, jint index)
{
	assert( index < env->GetArrayLength(jArray) );
	jstring jText = (jstring)env->GetObjectArrayElement(jArray, index);
	
//		const char *nativeChars = env->GetStringUTFChars(jText, NULL); 
//		if (nativeChars == NULL)
//		{ 
//		  return NULL; /* OutOfMemoryError already thrown */ 
//		} 
//		std::string cppString = nativeChars;
//		env->ReleaseStringUTFChars(jText, nativeChars);
	bool isNull;
	std::string cppString = stringFromJString(env, jText, isNull);
	env->DeleteLocalRef(jText);

	return cppString;
}


//
std::string JNITool::stringFromJString(JNIEnv* env, jstring jText, bool & isNull)
{	
	if(jText == NULL)
	{ 
	  isNull = true;
	  return "";
	}
	isNull = false;
	
	const char *nativeChars = env->GetStringUTFChars(jText, NULL); 
	if(nativeChars == NULL)
	{ 
	  return NULL; /* OutOfMemoryError already thrown */ 
	}
	std::string cppString = nativeChars;
	env->ReleaseStringUTFChars(jText, nativeChars);

	return cppString;
}


//
jstring JNITool::jstringFromString(JNIEnv *env, const std::string str)
{
	if(env->EnsureLocalCapacity(2) < 0)
	{
		std::stringstream exceptionMessage;
		exceptionMessage << __FILE__ << ":" << __LINE__ << " out of memory";
		throw std::runtime_error(exceptionMessage.str());
	}
	jsize len = (jsize)str.size();
	jbyteArray bytes = env->NewByteArray(len);
	if (bytes == NULL)
	{
		std::stringstream exceptionMessage;
		exceptionMessage << __FILE__ << ":" << __LINE__ << " bytes == NULL";
		throw std::runtime_error(exceptionMessage.str());
	}

	env->SetByteArrayRegion(bytes, 0, len, (jbyte *)(str.c_str()));
	jclass stringClass = env->FindClass("java/lang/String");
	jmethodID MID_String_init = env->GetMethodID(stringClass,"<init>", "([B)V");
	jstring result = (jstring)env->NewObject(stringClass, MID_String_init, bytes);
	env->DeleteLocalRef(bytes);
	return result;
}


/**
returns a jni field descriptor for a java class name
e.g. converts "java/lang/String" to "Ljava/lang/String;"
field descriptors are used to compose method signatures
*/
std::string JNITool::fieldDescriptor(const std::string name)
{
	return "L"+name+";";
}


//
template<>
std::string JNITool::fieldDescriptor<void>()
{
	return "";
}
template<>
std::string JNITool::fieldDescriptor<jboolean>()
{
	return "Z";
}
template<>
std::string JNITool::fieldDescriptor<jbooleanArray>()
{
	return "[Z";
}
template<>
std::string JNITool::fieldDescriptor<jint>()
{
	return "I";
}
template<>
std::string JNITool::fieldDescriptor<jintArray>()
{
	return "[I";
}
template<>
std::string JNITool::fieldDescriptor<jfloat>()
{
	return "F";
}
template<>
std::string JNITool::fieldDescriptor<jfloatArray>()
{
	return "[F";
}
template<>
std::string JNITool::fieldDescriptor<jdouble>()
{
	return "D";
}
template<>
std::string JNITool::fieldDescriptor<jdoubleArray>()
{
	return "[D";
}


} // EO namespace muscle

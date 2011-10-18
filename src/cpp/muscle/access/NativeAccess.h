/**
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


#ifndef NativeAccess_DAABC73E_18CF_4601_BBA5_C7CF536C34FF
#define NativeAccess_DAABC73E_18CF_4601_BBA5_C7CF536C34FF
#include <JNITool.h>
#include <access/NativeAccessMethod.h>
#if __GNUC__ >= 4
#include <tr1/memory>
#else
#include <memory>
#endif
#include <access/AbstractMethodCaller.h>
#include <access/MethodCaller.h>
#include <access/MethodCallerStatic.h>
#include <access/CallerStaticAttributes.h>
#include <access/CallerAttributes.h>
#include <jni.h>
namespace muscle {
/**
cpp counterpart class to access the java class muscle.core.kernel.CAController
function names prefixed with an underscore _ are cpp only utility functions which do not exist in the java class
\author Jan Hegewald
*/
class muscle_core_kernel_CAController
{
/**
function which returns the classname as a cpp jni signature (e.g. java_lang_String)
*/
	public: static std::string _CLASSNAME()
	{
		return "muscle/core/kernel/CAController";
	}
	public: muscle_core_kernel_CAController(JNIEnv* newEnv, jobject newDelegate)
	: jenv(newEnv), delegate(newDelegate)
{
	// init jvm pointer
	jint result = newEnv->GetJavaVM(&jvm);
	if(result < 0)
		throw std::runtime_error("error obtaining JVM");
	// see if we got the right jobject
	muscle::JNITool::assertInstanceOf(newEnv, newDelegate, _CLASSNAME(), __FILE__, __LINE__);
}
private: JNIEnv* jenv;
private: JavaVM* jvm;
private: jobject delegate;
/**
returns a reference to the JNIEnv pointer belonging to the current thread
*/
public: JNIEnv*& _GETJNIENV()
{
	jint result = jvm->AttachCurrentThread((void **)&jenv, NULL);
	if(result != JNI_OK)
		throw std::runtime_error("error obtaining Java env");
	return jenv;
}
//
public: void main_String_(jobjectArray A0)
{
	muscle_core_kernel_CAController::_main_String__Caller caller(_GETJNIENV());
	return caller.call(A0);
}
public: static void main_String_(JNIEnv*& env, jobjectArray A0)
{
	muscle_core_kernel_CAController::_main_String__Caller caller(env);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _main_String_()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "[Ljava/lang/String;" );
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "main", true, "", argTypes);
}
public: static jmethodID _main_String_(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = true;
	jmethodID mid = _main_String_().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _main_String_(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _main_String_(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	jclass cls = env->FindClass(_CLASSNAME().c_str());
muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return std::tr1::shared_ptr<muscle::MethodCallerStatic<void> >( new muscle::MethodCallerStatic<void>(env, cls, _CLASSNAME(), mid) );
}
public: std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _main_String_(JNIEnv*& env)
{
	bool isStatic;
	jmethodID mid = _main_String_(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return std::tr1::shared_ptr<muscle::MethodCallerStatic<void> >( new muscle::MethodCallerStatic<void>(env, cls, _CLASSNAME(), mid) );
}
/**
helper class to cache a static jni method invocation procedure
this caller can be used to invoke a static method on the target java class muscle.core.kernel.CAController
it should be used if there is no object of the target class muscle_core_kernel_CAController (which is not required for static calls) and the method needs to be invoked frequently
\author Jan Hegewald
*/
class _main_String__Caller : public muscle::CallerStaticAttributes<void>
{
	public: _main_String__Caller(JNIEnv* env)
	: muscle::CallerStaticAttributes<void>(env, env->FindClass(muscle_core_kernel_CAController::_CLASSNAME().c_str()), muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_main_String_(env, isStatic))
	{
		assert(isStatic == true);
	}
	void call(jobjectArray A0)
	{
		muscle::CallerStaticAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: jboolean isNative()
{
	muscle_core_kernel_CAController::_isNative_Caller caller(_GETJNIENV());
	return caller.call();
}
public: static jboolean isNative(JNIEnv*& env)
{
	muscle_core_kernel_CAController::_isNative_Caller caller(env);
	return caller.call();
}
public: static muscle::NativeAccessMethod _isNative()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "isNative", true, "Z", argTypes);
}
public: static jmethodID _isNative(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = true;
	jmethodID mid = _isNative().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jboolean> > _isNative(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _isNative(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	jclass cls = env->FindClass(_CLASSNAME().c_str());
muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return std::tr1::shared_ptr<muscle::MethodCallerStatic<jboolean> >( new muscle::MethodCallerStatic<jboolean>(env, cls, _CLASSNAME(), mid) );
}
public: std::tr1::shared_ptr<muscle::AbstractMethodCaller<jboolean> > _isNative(JNIEnv*& env)
{
	bool isStatic;
	jmethodID mid = _isNative(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return std::tr1::shared_ptr<muscle::MethodCallerStatic<jboolean> >( new muscle::MethodCallerStatic<jboolean>(env, cls, _CLASSNAME(), mid) );
}
/**
helper class to cache a static jni method invocation procedure
this caller can be used to invoke a static method on the target java class muscle.core.kernel.CAController
it should be used if there is no object of the target class muscle_core_kernel_CAController (which is not required for static calls) and the method needs to be invoked frequently
\author Jan Hegewald
*/
class _isNative_Caller : public muscle::CallerStaticAttributes<jboolean>
{
	public: _isNative_Caller(JNIEnv* env)
	: muscle::CallerStaticAttributes<jboolean>(env, env->FindClass(muscle_core_kernel_CAController::_CLASSNAME().c_str()), muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_isNative(env, isStatic))
	{
		assert(isStatic == true);
	}
	jboolean call()
	{
		return muscle::CallerStaticAttributes<jboolean>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jobject getLogger()
{
	muscle_core_kernel_CAController::_getLogger_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getLogger()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "getLogger", false, "Ljava/util/logging/Logger;", argTypes);
}
public: static jmethodID _getLogger(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getLogger().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _getLogger(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getLogger(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _getLogger_Caller : public muscle::CallerAttributes<jobject>
{
	public: _getLogger_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_getLogger(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call()
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jstring info_Class(jclass A0)
{
	muscle_core_kernel_CAController::_info_Class_Caller caller(_GETJNIENV());
	return caller.call(A0);
}
public: static jstring info_Class(JNIEnv*& env, jclass A0)
{
	muscle_core_kernel_CAController::_info_Class_Caller caller(env);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _info_Class()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/lang/Class;" );
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "info", true, "Ljava/lang/String;", argTypes);
}
public: static jmethodID _info_Class(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = true;
	jmethodID mid = _info_Class().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jstring> > _info_Class(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _info_Class(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	jclass cls = env->FindClass(_CLASSNAME().c_str());
muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return std::tr1::shared_ptr<muscle::MethodCallerStatic<jstring> >( new muscle::MethodCallerStatic<jstring>(env, cls, _CLASSNAME(), mid) );
}
public: std::tr1::shared_ptr<muscle::AbstractMethodCaller<jstring> > _info_Class(JNIEnv*& env)
{
	bool isStatic;
	jmethodID mid = _info_Class(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return std::tr1::shared_ptr<muscle::MethodCallerStatic<jstring> >( new muscle::MethodCallerStatic<jstring>(env, cls, _CLASSNAME(), mid) );
}
/**
helper class to cache a static jni method invocation procedure
this caller can be used to invoke a static method on the target java class muscle.core.kernel.CAController
it should be used if there is no object of the target class muscle_core_kernel_CAController (which is not required for static calls) and the method needs to be invoked frequently
\author Jan Hegewald
*/
class _info_Class_Caller : public muscle::CallerStaticAttributes<jstring>
{
	public: _info_Class_Caller(JNIEnv* env)
	: muscle::CallerStaticAttributes<jstring>(env, env->FindClass(muscle_core_kernel_CAController::_CLASSNAME().c_str()), muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_info_Class(env, isStatic))
	{
		assert(isStatic == true);
	}
	jstring call(jclass A0)
	{
		return muscle::CallerStaticAttributes<jstring>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: void takeDown()
{
	muscle_core_kernel_CAController::_takeDown_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _takeDown()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "takeDown", false, "", argTypes);
}
public: static jmethodID _takeDown(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _takeDown().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _takeDown(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _takeDown(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _takeDown_Caller : public muscle::CallerAttributes<void>
{
	public: _takeDown_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_takeDown(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call()
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jobjectArray getArguments()
{
	muscle_core_kernel_CAController::_getArguments_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getArguments()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "getArguments", false, "[Ljava/lang/Object;", argTypes);
}
public: static jmethodID _getArguments(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getArguments().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobjectArray> > _getArguments(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getArguments(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobjectArray> >( new muscle::MethodCaller<jobjectArray>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _getArguments_Caller : public muscle::CallerAttributes<jobjectArray>
{
	public: _getArguments_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobjectArray>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_getArguments(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobjectArray call()
	{
		return muscle::CallerAttributes<jobjectArray>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jboolean willStop()
{
	muscle_core_kernel_CAController::_willStop_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _willStop()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "willStop", false, "Z", argTypes);
}
public: static jmethodID _willStop(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _willStop().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jboolean> > _willStop(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _willStop(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jboolean> >( new muscle::MethodCaller<jboolean>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _willStop_Caller : public muscle::CallerAttributes<jboolean>
{
	public: _willStop_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jboolean>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_willStop(env, isStatic))
	{
		assert(isStatic == false);
	}
	jboolean call()
	{
		return muscle::CallerAttributes<jboolean>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jobject stopJNIMethod()
{
	muscle_core_kernel_CAController::_stopJNIMethod_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _stopJNIMethod()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "stopJNIMethod", false, "Lutilities/jni/JNIMethod;", argTypes);
}
public: static jmethodID _stopJNIMethod(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _stopJNIMethod().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _stopJNIMethod(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _stopJNIMethod(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _stopJNIMethod_Caller : public muscle::CallerAttributes<jobject>
{
	public: _stopJNIMethod_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_stopJNIMethod(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call()
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jobject getKernelBootInfo()
{
	muscle_core_kernel_CAController::_getKernelBootInfo_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getKernelBootInfo()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "getKernelBootInfo", false, "Lmuscle/core/kernel/KernelBootInfo;", argTypes);
}
public: static jmethodID _getKernelBootInfo(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getKernelBootInfo().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _getKernelBootInfo(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getKernelBootInfo(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _getKernelBootInfo_Caller : public muscle::CallerAttributes<jobject>
{
	public: _getKernelBootInfo_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_getKernelBootInfo(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call()
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jobject getScale()
{
	muscle_core_kernel_CAController::_getScale_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getScale()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "getScale", false, "Lmuscle/core/Scale;", argTypes);
}
public: static jmethodID _getScale(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getScale().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _getScale(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getScale(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _getScale_Caller : public muscle::CallerAttributes<jobject>
{
	public: _getScale_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_getScale(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call()
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void addMessage_ACLMessage(jobject A0)
{
	muscle_core_kernel_CAController::_addMessage_ACLMessage_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _addMessage_ACLMessage()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljade/lang/acl/ACLMessage;" );
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "addMessage", false, "", argTypes);
}
public: static jmethodID _addMessage_ACLMessage(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _addMessage_ACLMessage().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _addMessage_ACLMessage(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _addMessage_ACLMessage(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _addMessage_ACLMessage_Caller : public muscle::CallerAttributes<void>
{
	public: _addMessage_ACLMessage_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_addMessage_ACLMessage(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: jstring getTmpPath()
{
	muscle_core_kernel_CAController::_getTmpPath_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getTmpPath()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "getTmpPath", false, "Ljava/lang/String;", argTypes);
}
public: static jmethodID _getTmpPath(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getTmpPath().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jstring> > _getTmpPath(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getTmpPath(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jstring> >( new muscle::MethodCaller<jstring>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _getTmpPath_Caller : public muscle::CallerAttributes<jstring>
{
	public: _getTmpPath_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jstring>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_getTmpPath(env, isStatic))
	{
		assert(isStatic == false);
	}
	jstring call()
	{
		return muscle::CallerAttributes<jstring>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jstring getCxAPath()
{
	muscle_core_kernel_CAController::_getCxAPath_Caller caller(_GETJNIENV());
	return caller.call();
}
public: static jstring getCxAPath(JNIEnv*& env)
{
	muscle_core_kernel_CAController::_getCxAPath_Caller caller(env);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getCxAPath()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "getCxAPath", true, "Ljava/lang/String;", argTypes);
}
public: static jmethodID _getCxAPath(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = true;
	jmethodID mid = _getCxAPath().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jstring> > _getCxAPath(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getCxAPath(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	jclass cls = env->FindClass(_CLASSNAME().c_str());
muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return std::tr1::shared_ptr<muscle::MethodCallerStatic<jstring> >( new muscle::MethodCallerStatic<jstring>(env, cls, _CLASSNAME(), mid) );
}
public: std::tr1::shared_ptr<muscle::AbstractMethodCaller<jstring> > _getCxAPath(JNIEnv*& env)
{
	bool isStatic;
	jmethodID mid = _getCxAPath(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return std::tr1::shared_ptr<muscle::MethodCallerStatic<jstring> >( new muscle::MethodCallerStatic<jstring>(env, cls, _CLASSNAME(), mid) );
}
/**
helper class to cache a static jni method invocation procedure
this caller can be used to invoke a static method on the target java class muscle.core.kernel.CAController
it should be used if there is no object of the target class muscle_core_kernel_CAController (which is not required for static calls) and the method needs to be invoked frequently
\author Jan Hegewald
*/
class _getCxAPath_Caller : public muscle::CallerStaticAttributes<jstring>
{
	public: _getCxAPath_Caller(JNIEnv* env)
	: muscle::CallerStaticAttributes<jstring>(env, env->FindClass(muscle_core_kernel_CAController::_CLASSNAME().c_str()), muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_getCxAPath(env, isStatic))
	{
		assert(isStatic == true);
	}
	jstring call()
	{
		return muscle::CallerStaticAttributes<jstring>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jstring getKernelPath()
{
	muscle_core_kernel_CAController::_getKernelPath_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getKernelPath()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "getKernelPath", false, "Ljava/lang/String;", argTypes);
}
public: static jmethodID _getKernelPath(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getKernelPath().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jstring> > _getKernelPath(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getKernelPath(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jstring> >( new muscle::MethodCaller<jstring>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _getKernelPath_Caller : public muscle::CallerAttributes<jstring>
{
	public: _getKernelPath_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jstring>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_getKernelPath(env, isStatic))
	{
		assert(isStatic == false);
	}
	jstring call()
	{
		return muscle::CallerAttributes<jstring>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jstring getKernelPath_Class(jclass A0)
{
	muscle_core_kernel_CAController::_getKernelPath_Class_Caller caller(_GETJNIENV());
	return caller.call(A0);
}
public: static jstring getKernelPath_Class(JNIEnv*& env, jclass A0)
{
	muscle_core_kernel_CAController::_getKernelPath_Class_Caller caller(env);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _getKernelPath_Class()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/lang/Class;" );
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "getKernelPath", true, "Ljava/lang/String;", argTypes);
}
public: static jmethodID _getKernelPath_Class(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = true;
	jmethodID mid = _getKernelPath_Class().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jstring> > _getKernelPath_Class(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getKernelPath_Class(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	jclass cls = env->FindClass(_CLASSNAME().c_str());
muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return std::tr1::shared_ptr<muscle::MethodCallerStatic<jstring> >( new muscle::MethodCallerStatic<jstring>(env, cls, _CLASSNAME(), mid) );
}
public: std::tr1::shared_ptr<muscle::AbstractMethodCaller<jstring> > _getKernelPath_Class(JNIEnv*& env)
{
	bool isStatic;
	jmethodID mid = _getKernelPath_Class(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return std::tr1::shared_ptr<muscle::MethodCallerStatic<jstring> >( new muscle::MethodCallerStatic<jstring>(env, cls, _CLASSNAME(), mid) );
}
/**
helper class to cache a static jni method invocation procedure
this caller can be used to invoke a static method on the target java class muscle.core.kernel.CAController
it should be used if there is no object of the target class muscle_core_kernel_CAController (which is not required for static calls) and the method needs to be invoked frequently
\author Jan Hegewald
*/
class _getKernelPath_Class_Caller : public muscle::CallerStaticAttributes<jstring>
{
	public: _getKernelPath_Class_Caller(JNIEnv* env)
	: muscle::CallerStaticAttributes<jstring>(env, env->FindClass(muscle_core_kernel_CAController::_CLASSNAME().c_str()), muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_getKernelPath_Class(env, isStatic))
	{
		assert(isStatic == true);
	}
	jstring call(jclass A0)
	{
		return muscle::CallerStaticAttributes<jstring>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: jstring getLegacyProperties()
{
	muscle_core_kernel_CAController::_getLegacyProperties_Caller caller(_GETJNIENV());
	return caller.call();
}
public: static jstring getLegacyProperties(JNIEnv*& env)
{
	muscle_core_kernel_CAController::_getLegacyProperties_Caller caller(env);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getLegacyProperties()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "getLegacyProperties", true, "Ljava/lang/String;", argTypes);
}
public: static jmethodID _getLegacyProperties(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = true;
	jmethodID mid = _getLegacyProperties().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jstring> > _getLegacyProperties(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getLegacyProperties(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	jclass cls = env->FindClass(_CLASSNAME().c_str());
muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return std::tr1::shared_ptr<muscle::MethodCallerStatic<jstring> >( new muscle::MethodCallerStatic<jstring>(env, cls, _CLASSNAME(), mid) );
}
public: std::tr1::shared_ptr<muscle::AbstractMethodCaller<jstring> > _getLegacyProperties(JNIEnv*& env)
{
	bool isStatic;
	jmethodID mid = _getLegacyProperties(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return std::tr1::shared_ptr<muscle::MethodCallerStatic<jstring> >( new muscle::MethodCallerStatic<jstring>(env, cls, _CLASSNAME(), mid) );
}
/**
helper class to cache a static jni method invocation procedure
this caller can be used to invoke a static method on the target java class muscle.core.kernel.CAController
it should be used if there is no object of the target class muscle_core_kernel_CAController (which is not required for static calls) and the method needs to be invoked frequently
\author Jan Hegewald
*/
class _getLegacyProperties_Caller : public muscle::CallerStaticAttributes<jstring>
{
	public: _getLegacyProperties_Caller(JNIEnv* env)
	: muscle::CallerStaticAttributes<jstring>(env, env->FindClass(muscle_core_kernel_CAController::_CLASSNAME().c_str()), muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_getLegacyProperties(env, isStatic))
	{
		assert(isStatic == true);
	}
	jstring call()
	{
		return muscle::CallerStaticAttributes<jstring>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jstring getCxAProperty_String(jstring A0)
{
	muscle_core_kernel_CAController::_getCxAProperty_String_Caller caller(_GETJNIENV());
	return caller.call(A0);
}
public: static jstring getCxAProperty_String(JNIEnv*& env, jstring A0)
{
	muscle_core_kernel_CAController::_getCxAProperty_String_Caller caller(env);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _getCxAProperty_String()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/lang/String;" );
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "getCxAProperty", true, "Ljava/lang/String;", argTypes);
}
public: static jmethodID _getCxAProperty_String(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = true;
	jmethodID mid = _getCxAProperty_String().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jstring> > _getCxAProperty_String(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getCxAProperty_String(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	jclass cls = env->FindClass(_CLASSNAME().c_str());
muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return std::tr1::shared_ptr<muscle::MethodCallerStatic<jstring> >( new muscle::MethodCallerStatic<jstring>(env, cls, _CLASSNAME(), mid) );
}
public: std::tr1::shared_ptr<muscle::AbstractMethodCaller<jstring> > _getCxAProperty_String(JNIEnv*& env)
{
	bool isStatic;
	jmethodID mid = _getCxAProperty_String(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return std::tr1::shared_ptr<muscle::MethodCallerStatic<jstring> >( new muscle::MethodCallerStatic<jstring>(env, cls, _CLASSNAME(), mid) );
}
/**
helper class to cache a static jni method invocation procedure
this caller can be used to invoke a static method on the target java class muscle.core.kernel.CAController
it should be used if there is no object of the target class muscle_core_kernel_CAController (which is not required for static calls) and the method needs to be invoked frequently
\author Jan Hegewald
*/
class _getCxAProperty_String_Caller : public muscle::CallerStaticAttributes<jstring>
{
	public: _getCxAProperty_String_Caller(JNIEnv* env)
	: muscle::CallerStaticAttributes<jstring>(env, env->FindClass(muscle_core_kernel_CAController::_CLASSNAME().c_str()), muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_getCxAProperty_String(env, isStatic))
	{
		assert(isStatic == true);
	}
	jstring call(jstring A0)
	{
		return muscle::CallerStaticAttributes<jstring>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: jobject getCxA()
{
	muscle_core_kernel_CAController::_getCxA_Caller caller(_GETJNIENV());
	return caller.call();
}
public: static jobject getCxA(JNIEnv*& env)
{
	muscle_core_kernel_CAController::_getCxA_Caller caller(env);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getCxA()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "getCxA", true, "Lmuscle/core/CxADescription;", argTypes);
}
public: static jmethodID _getCxA(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = true;
	jmethodID mid = _getCxA().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _getCxA(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getCxA(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	jclass cls = env->FindClass(_CLASSNAME().c_str());
muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return std::tr1::shared_ptr<muscle::MethodCallerStatic<jobject> >( new muscle::MethodCallerStatic<jobject>(env, cls, _CLASSNAME(), mid) );
}
public: std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _getCxA(JNIEnv*& env)
{
	bool isStatic;
	jmethodID mid = _getCxA(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return std::tr1::shared_ptr<muscle::MethodCallerStatic<jobject> >( new muscle::MethodCallerStatic<jobject>(env, cls, _CLASSNAME(), mid) );
}
/**
helper class to cache a static jni method invocation procedure
this caller can be used to invoke a static method on the target java class muscle.core.kernel.CAController
it should be used if there is no object of the target class muscle_core_kernel_CAController (which is not required for static calls) and the method needs to be invoked frequently
\author Jan Hegewald
*/
class _getCxA_Caller : public muscle::CallerStaticAttributes<jobject>
{
	public: _getCxA_Caller(JNIEnv* env)
	: muscle::CallerStaticAttributes<jobject>(env, env->FindClass(muscle_core_kernel_CAController::_CLASSNAME().c_str()), muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_getCxA(env, isStatic))
	{
		assert(isStatic == true);
	}
	jobject call()
	{
		return muscle::CallerStaticAttributes<jobject>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void doMove_Location(jobject A0)
{
	muscle_core_kernel_CAController::_doMove_Location_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _doMove_Location()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Linterface jade/core/Location;" );
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "doMove", false, "", argTypes);
}
public: static jmethodID _doMove_Location(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _doMove_Location().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _doMove_Location(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _doMove_Location(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _doMove_Location_Caller : public muscle::CallerAttributes<void>
{
	public: _doMove_Location_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_doMove_Location(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: void doClone_Location_String(jobject A0, jstring A1)
{
	muscle_core_kernel_CAController::_doClone_Location_String_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0, A1);
}
public: static muscle::NativeAccessMethod _doClone_Location_String()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Linterface jade/core/Location;" );
argTypes.push_back( "Ljava/lang/String;" );
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "doClone", false, "", argTypes);
}
public: static jmethodID _doClone_Location_String(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _doClone_Location_String().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _doClone_Location_String(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _doClone_Location_String(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _doClone_Location_String_Caller : public muscle::CallerAttributes<void>
{
	public: _doClone_Location_String_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_doClone_Location_String(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0, jstring A1)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0, A1);
	}
	private: bool isStatic;
};
//
public: void run()
{
	muscle_core_kernel_CAController::_run_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _run()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "run", false, "", argTypes);
}
public: static jmethodID _run(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _run().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _run(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _run(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _run_Caller : public muscle::CallerAttributes<void>
{
	public: _run_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_run(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call()
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jstring getProperty_String_String(jstring A0, jstring A1)
{
	muscle_core_kernel_CAController::_getProperty_String_String_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0, A1);
}
public: static muscle::NativeAccessMethod _getProperty_String_String()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "Ljava/lang/String;" );
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "getProperty", false, "Ljava/lang/String;", argTypes);
}
public: static jmethodID _getProperty_String_String(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getProperty_String_String().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jstring> > _getProperty_String_String(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getProperty_String_String(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jstring> >( new muscle::MethodCaller<jstring>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _getProperty_String_String_Caller : public muscle::CallerAttributes<jstring>
{
	public: _getProperty_String_String_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jstring>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_getProperty_String_String(env, isStatic))
	{
		assert(isStatic == false);
	}
	jstring call(jstring A0, jstring A1)
	{
		return muscle::CallerAttributes<jstring>::call(muscle::VABEGIN::FIRST, A0, A1);
	}
	private: bool isStatic;
};
//
public: jstring getName()
{
	muscle_core_kernel_CAController::_getName_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getName()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "getName", false, "Ljava/lang/String;", argTypes);
}
public: static jmethodID _getName(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getName().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jstring> > _getName(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getName(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jstring> >( new muscle::MethodCaller<jstring>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _getName_Caller : public muscle::CallerAttributes<jstring>
{
	public: _getName_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jstring>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_getName(env, isStatic))
	{
		assert(isStatic == false);
	}
	jstring call()
	{
		return muscle::CallerAttributes<jstring>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void write_OutputStream(jobject A0)
{
	muscle_core_kernel_CAController::_write_OutputStream_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _write_OutputStream()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/io/OutputStream;" );
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "write", false, "", argTypes);
}
public: static jmethodID _write_OutputStream(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _write_OutputStream().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _write_OutputStream(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _write_OutputStream(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _write_OutputStream_Caller : public muscle::CallerAttributes<void>
{
	public: _write_OutputStream_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_write_OutputStream(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: void join()
{
	muscle_core_kernel_CAController::_join_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _join()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "join", false, "", argTypes);
}
public: static jmethodID _join(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _join().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _join(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _join(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _join_Caller : public muscle::CallerAttributes<void>
{
	public: _join_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_join(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call()
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jint getState()
{
	muscle_core_kernel_CAController::_getState_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getState()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "getState", false, "I", argTypes);
}
public: static jmethodID _getState(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getState().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jint> > _getState(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getState(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jint> >( new muscle::MethodCaller<jint>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _getState_Caller : public muscle::CallerAttributes<jint>
{
	public: _getState_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jint>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_getState(env, isStatic))
	{
		assert(isStatic == false);
	}
	jint call()
	{
		return muscle::CallerAttributes<jint>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void clean_boolean(jboolean A0)
{
	muscle_core_kernel_CAController::_clean_boolean_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _clean_boolean()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Z" );
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "clean", false, "", argTypes);
}
public: static jmethodID _clean_boolean(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _clean_boolean().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _clean_boolean(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _clean_boolean(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _clean_boolean_Caller : public muscle::CallerAttributes<void>
{
	public: _clean_boolean_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_clean_boolean(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jboolean A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: void send_ACLMessage(jobject A0)
{
	muscle_core_kernel_CAController::_send_ACLMessage_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _send_ACLMessage()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljade/lang/acl/ACLMessage;" );
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "send", false, "", argTypes);
}
public: static jmethodID _send_ACLMessage(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _send_ACLMessage().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _send_ACLMessage(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _send_ACLMessage(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _send_ACLMessage_Caller : public muscle::CallerAttributes<void>
{
	public: _send_ACLMessage_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_send_ACLMessage(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: jint getCurQueueSize()
{
	muscle_core_kernel_CAController::_getCurQueueSize_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getCurQueueSize()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "getCurQueueSize", false, "I", argTypes);
}
public: static jmethodID _getCurQueueSize(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getCurQueueSize().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jint> > _getCurQueueSize(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getCurQueueSize(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jint> >( new muscle::MethodCaller<jint>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _getCurQueueSize_Caller : public muscle::CallerAttributes<jint>
{
	public: _getCurQueueSize_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jint>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_getCurQueueSize(env, isStatic))
	{
		assert(isStatic == false);
	}
	jint call()
	{
		return muscle::CallerAttributes<jint>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void doDelete()
{
	muscle_core_kernel_CAController::_doDelete_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _doDelete()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "doDelete", false, "", argTypes);
}
public: static jmethodID _doDelete(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _doDelete().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _doDelete(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _doDelete(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _doDelete_Caller : public muscle::CallerAttributes<void>
{
	public: _doDelete_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_doDelete(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call()
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jstring getLocalName()
{
	muscle_core_kernel_CAController::_getLocalName_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getLocalName()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "getLocalName", false, "Ljava/lang/String;", argTypes);
}
public: static jmethodID _getLocalName(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getLocalName().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jstring> > _getLocalName(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getLocalName(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jstring> >( new muscle::MethodCaller<jstring>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _getLocalName_Caller : public muscle::CallerAttributes<jstring>
{
	public: _getLocalName_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jstring>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_getLocalName(env, isStatic))
	{
		assert(isStatic == false);
	}
	jstring call()
	{
		return muscle::CallerAttributes<jstring>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jobject getAID()
{
	muscle_core_kernel_CAController::_getAID_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getAID()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "getAID", false, "Ljade/core/AID;", argTypes);
}
public: static jmethodID _getAID(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getAID().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _getAID(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getAID(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _getAID_Caller : public muscle::CallerAttributes<jobject>
{
	public: _getAID_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_getAID(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call()
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jobject blockingReceive_MessageTemplate_long(jobject A0, jlong A1)
{
	muscle_core_kernel_CAController::_blockingReceive_MessageTemplate_long_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0, A1);
}
public: static muscle::NativeAccessMethod _blockingReceive_MessageTemplate_long()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljade/lang/acl/MessageTemplate;" );
argTypes.push_back( "J" );
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "blockingReceive", false, "Ljade/lang/acl/ACLMessage;", argTypes);
}
public: static jmethodID _blockingReceive_MessageTemplate_long(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _blockingReceive_MessageTemplate_long().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _blockingReceive_MessageTemplate_long(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _blockingReceive_MessageTemplate_long(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _blockingReceive_MessageTemplate_long_Caller : public muscle::CallerAttributes<jobject>
{
	public: _blockingReceive_MessageTemplate_long_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_blockingReceive_MessageTemplate_long(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call(jobject A0, jlong A1)
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST, A0, A1);
	}
	private: bool isStatic;
};
//
public: jobject blockingReceive()
{
	muscle_core_kernel_CAController::_blockingReceive_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _blockingReceive()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "blockingReceive", false, "Ljade/lang/acl/ACLMessage;", argTypes);
}
public: static jmethodID _blockingReceive(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _blockingReceive().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _blockingReceive(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _blockingReceive(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _blockingReceive_Caller : public muscle::CallerAttributes<jobject>
{
	public: _blockingReceive_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_blockingReceive(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call()
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jobject blockingReceive_long(jlong A0)
{
	muscle_core_kernel_CAController::_blockingReceive_long_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _blockingReceive_long()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "J" );
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "blockingReceive", false, "Ljade/lang/acl/ACLMessage;", argTypes);
}
public: static jmethodID _blockingReceive_long(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _blockingReceive_long().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _blockingReceive_long(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _blockingReceive_long(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _blockingReceive_long_Caller : public muscle::CallerAttributes<jobject>
{
	public: _blockingReceive_long_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_blockingReceive_long(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call(jlong A0)
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: jobject blockingReceive_MessageTemplate(jobject A0)
{
	muscle_core_kernel_CAController::_blockingReceive_MessageTemplate_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _blockingReceive_MessageTemplate()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljade/lang/acl/MessageTemplate;" );
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "blockingReceive", false, "Ljade/lang/acl/ACLMessage;", argTypes);
}
public: static jmethodID _blockingReceive_MessageTemplate(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _blockingReceive_MessageTemplate().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _blockingReceive_MessageTemplate(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _blockingReceive_MessageTemplate(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _blockingReceive_MessageTemplate_Caller : public muscle::CallerAttributes<jobject>
{
	public: _blockingReceive_MessageTemplate_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_blockingReceive_MessageTemplate(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call(jobject A0)
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: void restartLater_Behaviour_long(jobject A0, jlong A1)
{
	muscle_core_kernel_CAController::_restartLater_Behaviour_long_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0, A1);
}
public: static muscle::NativeAccessMethod _restartLater_Behaviour_long()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljade/core/behaviours/Behaviour;" );
argTypes.push_back( "J" );
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "restartLater", false, "", argTypes);
}
public: static jmethodID _restartLater_Behaviour_long(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _restartLater_Behaviour_long().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _restartLater_Behaviour_long(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _restartLater_Behaviour_long(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _restartLater_Behaviour_long_Caller : public muscle::CallerAttributes<void>
{
	public: _restartLater_Behaviour_long_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_restartLater_Behaviour_long(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0, jlong A1)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0, A1);
	}
	private: bool isStatic;
};
//
public: void doTimeOut_Timer(jobject A0)
{
	muscle_core_kernel_CAController::_doTimeOut_Timer_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _doTimeOut_Timer()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljade/core/Timer;" );
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "doTimeOut", false, "", argTypes);
}
public: static jmethodID _doTimeOut_Timer(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _doTimeOut_Timer().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _doTimeOut_Timer(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _doTimeOut_Timer(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _doTimeOut_Timer_Caller : public muscle::CallerAttributes<void>
{
	public: _doTimeOut_Timer_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_doTimeOut_Timer(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: void notifyRestarted_Behaviour(jobject A0)
{
	muscle_core_kernel_CAController::_notifyRestarted_Behaviour_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _notifyRestarted_Behaviour()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljade/core/behaviours/Behaviour;" );
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "notifyRestarted", false, "", argTypes);
}
public: static jmethodID _notifyRestarted_Behaviour(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _notifyRestarted_Behaviour().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _notifyRestarted_Behaviour(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _notifyRestarted_Behaviour(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _notifyRestarted_Behaviour_Caller : public muscle::CallerAttributes<void>
{
	public: _notifyRestarted_Behaviour_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_notifyRestarted_Behaviour(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: void removeTimer_Behaviour(jobject A0)
{
	muscle_core_kernel_CAController::_removeTimer_Behaviour_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _removeTimer_Behaviour()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljade/core/behaviours/Behaviour;" );
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "removeTimer", false, "", argTypes);
}
public: static jmethodID _removeTimer_Behaviour(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _removeTimer_Behaviour().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _removeTimer_Behaviour(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _removeTimer_Behaviour(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _removeTimer_Behaviour_Caller : public muscle::CallerAttributes<void>
{
	public: _removeTimer_Behaviour_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_removeTimer_Behaviour(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: jobject getContainerController()
{
	muscle_core_kernel_CAController::_getContainerController_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getContainerController()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "getContainerController", false, "Ljade/wrapper/AgentContainer;", argTypes);
}
public: static jmethodID _getContainerController(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getContainerController().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _getContainerController(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getContainerController(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _getContainerController_Caller : public muscle::CallerAttributes<jobject>
{
	public: _getContainerController_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_getContainerController(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call()
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void setArguments_Object_(jobjectArray A0)
{
	muscle_core_kernel_CAController::_setArguments_Object__Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _setArguments_Object_()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "[Ljava/lang/Object;" );
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "setArguments", false, "", argTypes);
}
public: static jmethodID _setArguments_Object_(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _setArguments_Object_().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _setArguments_Object_(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _setArguments_Object_(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _setArguments_Object__Caller : public muscle::CallerAttributes<void>
{
	public: _setArguments_Object__Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_setArguments_Object_(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobjectArray A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: jboolean isRestarting()
{
	muscle_core_kernel_CAController::_isRestarting_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _isRestarting()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "isRestarting", false, "Z", argTypes);
}
public: static jmethodID _isRestarting(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _isRestarting().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jboolean> > _isRestarting(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _isRestarting(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jboolean> >( new muscle::MethodCaller<jboolean>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _isRestarting_Caller : public muscle::CallerAttributes<jboolean>
{
	public: _isRestarting_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jboolean>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_isRestarting(env, isStatic))
	{
		assert(isStatic == false);
	}
	jboolean call()
	{
		return muscle::CallerAttributes<jboolean>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jobject getAMS()
{
	muscle_core_kernel_CAController::_getAMS_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getAMS()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "getAMS", false, "Ljade/core/AID;", argTypes);
}
public: static jmethodID _getAMS(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getAMS().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _getAMS(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getAMS(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _getAMS_Caller : public muscle::CallerAttributes<jobject>
{
	public: _getAMS_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_getAMS(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call()
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jobject getDefaultDF()
{
	muscle_core_kernel_CAController::_getDefaultDF_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getDefaultDF()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "getDefaultDF", false, "Ljade/core/AID;", argTypes);
}
public: static jmethodID _getDefaultDF(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getDefaultDF().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _getDefaultDF(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getDefaultDF(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _getDefaultDF_Caller : public muscle::CallerAttributes<jobject>
{
	public: _getDefaultDF_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_getDefaultDF(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call()
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jstring getHap()
{
	muscle_core_kernel_CAController::_getHap_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getHap()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "getHap", false, "Ljava/lang/String;", argTypes);
}
public: static jmethodID _getHap(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getHap().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jstring> > _getHap(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getHap(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jstring> >( new muscle::MethodCaller<jstring>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _getHap_Caller : public muscle::CallerAttributes<jstring>
{
	public: _getHap_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jstring>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_getHap(env, isStatic))
	{
		assert(isStatic == false);
	}
	jstring call()
	{
		return muscle::CallerAttributes<jstring>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jobject here()
{
	muscle_core_kernel_CAController::_here_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _here()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "here", false, "Linterface jade/core/Location;", argTypes);
}
public: static jmethodID _here(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _here().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _here(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _here(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _here_Caller : public muscle::CallerAttributes<jobject>
{
	public: _here_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_here(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call()
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void setQueueSize_int(jint A0)
{
	muscle_core_kernel_CAController::_setQueueSize_int_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _setQueueSize_int()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "I" );
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "setQueueSize", false, "", argTypes);
}
public: static jmethodID _setQueueSize_int(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _setQueueSize_int().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _setQueueSize_int(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _setQueueSize_int(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _setQueueSize_int_Caller : public muscle::CallerAttributes<void>
{
	public: _setQueueSize_int_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_setQueueSize_int(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jint A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: jint getQueueSize()
{
	muscle_core_kernel_CAController::_getQueueSize_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getQueueSize()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "getQueueSize", false, "I", argTypes);
}
public: static jmethodID _getQueueSize(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getQueueSize().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jint> > _getQueueSize(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getQueueSize(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jint> >( new muscle::MethodCaller<jint>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _getQueueSize_Caller : public muscle::CallerAttributes<jint>
{
	public: _getQueueSize_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jint>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_getQueueSize(env, isStatic))
	{
		assert(isStatic == false);
	}
	jint call()
	{
		return muscle::CallerAttributes<jint>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void changeStateTo_LifeCycle(jobject A0)
{
	muscle_core_kernel_CAController::_changeStateTo_LifeCycle_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _changeStateTo_LifeCycle()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljade/core/LifeCycle;" );
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "changeStateTo", false, "", argTypes);
}
public: static jmethodID _changeStateTo_LifeCycle(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _changeStateTo_LifeCycle().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _changeStateTo_LifeCycle(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _changeStateTo_LifeCycle(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _changeStateTo_LifeCycle_Caller : public muscle::CallerAttributes<void>
{
	public: _changeStateTo_LifeCycle_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_changeStateTo_LifeCycle(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: void restoreBufferedState()
{
	muscle_core_kernel_CAController::_restoreBufferedState_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _restoreBufferedState()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "restoreBufferedState", false, "", argTypes);
}
public: static jmethodID _restoreBufferedState(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _restoreBufferedState().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _restoreBufferedState(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _restoreBufferedState(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _restoreBufferedState_Caller : public muscle::CallerAttributes<void>
{
	public: _restoreBufferedState_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_restoreBufferedState(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call()
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jobject getAgentState()
{
	muscle_core_kernel_CAController::_getAgentState_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getAgentState()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "getAgentState", false, "Ljade/core/AgentState;", argTypes);
}
public: static jmethodID _getAgentState(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getAgentState().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _getAgentState(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getAgentState(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _getAgentState_Caller : public muscle::CallerAttributes<jobject>
{
	public: _getAgentState_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_getAgentState(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call()
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void doSuspend()
{
	muscle_core_kernel_CAController::_doSuspend_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _doSuspend()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "doSuspend", false, "", argTypes);
}
public: static jmethodID _doSuspend(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _doSuspend().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _doSuspend(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _doSuspend(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _doSuspend_Caller : public muscle::CallerAttributes<void>
{
	public: _doSuspend_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_doSuspend(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call()
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void doActivate()
{
	muscle_core_kernel_CAController::_doActivate_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _doActivate()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "doActivate", false, "", argTypes);
}
public: static jmethodID _doActivate(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _doActivate().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _doActivate(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _doActivate(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _doActivate_Caller : public muscle::CallerAttributes<void>
{
	public: _doActivate_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_doActivate(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call()
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void doWait()
{
	muscle_core_kernel_CAController::_doWait_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _doWait()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "doWait", false, "", argTypes);
}
public: static jmethodID _doWait(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _doWait().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _doWait(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _doWait(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _doWait_Caller : public muscle::CallerAttributes<void>
{
	public: _doWait_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_doWait(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call()
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void doWait_long(jlong A0)
{
	muscle_core_kernel_CAController::_doWait_long_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _doWait_long()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "J" );
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "doWait", false, "", argTypes);
}
public: static jmethodID _doWait_long(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _doWait_long().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _doWait_long(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _doWait_long(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _doWait_long_Caller : public muscle::CallerAttributes<void>
{
	public: _doWait_long_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_doWait_long(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jlong A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: void doWake()
{
	muscle_core_kernel_CAController::_doWake_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _doWake()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "doWake", false, "", argTypes);
}
public: static jmethodID _doWake(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _doWake().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _doWake(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _doWake(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _doWake_Caller : public muscle::CallerAttributes<void>
{
	public: _doWake_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_doWake(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call()
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void restore_InputStream(jobject A0)
{
	muscle_core_kernel_CAController::_restore_InputStream_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _restore_InputStream()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/io/InputStream;" );
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "restore", false, "", argTypes);
}
public: static jmethodID _restore_InputStream(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _restore_InputStream().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _restore_InputStream(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _restore_InputStream(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _restore_InputStream_Caller : public muscle::CallerAttributes<void>
{
	public: _restore_InputStream_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_restore_InputStream(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: void putO2AObject_Object_boolean(jobject A0, jboolean A1)
{
	muscle_core_kernel_CAController::_putO2AObject_Object_boolean_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0, A1);
}
public: static muscle::NativeAccessMethod _putO2AObject_Object_boolean()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/lang/Object;" );
argTypes.push_back( "Z" );
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "putO2AObject", false, "", argTypes);
}
public: static jmethodID _putO2AObject_Object_boolean(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _putO2AObject_Object_boolean().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _putO2AObject_Object_boolean(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _putO2AObject_Object_boolean(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _putO2AObject_Object_boolean_Caller : public muscle::CallerAttributes<void>
{
	public: _putO2AObject_Object_boolean_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_putO2AObject_Object_boolean(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0, jboolean A1)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0, A1);
	}
	private: bool isStatic;
};
//
public: jobject getO2AObject()
{
	muscle_core_kernel_CAController::_getO2AObject_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getO2AObject()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "getO2AObject", false, "Ljava/lang/Object;", argTypes);
}
public: static jmethodID _getO2AObject(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getO2AObject().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _getO2AObject(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getO2AObject(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _getO2AObject_Caller : public muscle::CallerAttributes<jobject>
{
	public: _getO2AObject_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_getO2AObject(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call()
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void setEnabledO2ACommunication_boolean_int(jboolean A0, jint A1)
{
	muscle_core_kernel_CAController::_setEnabledO2ACommunication_boolean_int_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0, A1);
}
public: static muscle::NativeAccessMethod _setEnabledO2ACommunication_boolean_int()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Z" );
argTypes.push_back( "I" );
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "setEnabledO2ACommunication", false, "", argTypes);
}
public: static jmethodID _setEnabledO2ACommunication_boolean_int(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _setEnabledO2ACommunication_boolean_int().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _setEnabledO2ACommunication_boolean_int(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _setEnabledO2ACommunication_boolean_int(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _setEnabledO2ACommunication_boolean_int_Caller : public muscle::CallerAttributes<void>
{
	public: _setEnabledO2ACommunication_boolean_int_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_setEnabledO2ACommunication_boolean_int(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jboolean A0, jint A1)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0, A1);
	}
	private: bool isStatic;
};
//
public: void setO2AManager_Behaviour(jobject A0)
{
	muscle_core_kernel_CAController::_setO2AManager_Behaviour_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _setO2AManager_Behaviour()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljade/core/behaviours/Behaviour;" );
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "setO2AManager", false, "", argTypes);
}
public: static jmethodID _setO2AManager_Behaviour(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _setO2AManager_Behaviour().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _setO2AManager_Behaviour(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _setO2AManager_Behaviour(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _setO2AManager_Behaviour_Caller : public muscle::CallerAttributes<void>
{
	public: _setO2AManager_Behaviour_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_setO2AManager_Behaviour(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: void addBehaviour_Behaviour(jobject A0)
{
	muscle_core_kernel_CAController::_addBehaviour_Behaviour_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _addBehaviour_Behaviour()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljade/core/behaviours/Behaviour;" );
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "addBehaviour", false, "", argTypes);
}
public: static jmethodID _addBehaviour_Behaviour(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _addBehaviour_Behaviour().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _addBehaviour_Behaviour(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _addBehaviour_Behaviour(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _addBehaviour_Behaviour_Caller : public muscle::CallerAttributes<void>
{
	public: _addBehaviour_Behaviour_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_addBehaviour_Behaviour(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: void removeBehaviour_Behaviour(jobject A0)
{
	muscle_core_kernel_CAController::_removeBehaviour_Behaviour_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _removeBehaviour_Behaviour()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljade/core/behaviours/Behaviour;" );
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "removeBehaviour", false, "", argTypes);
}
public: static jmethodID _removeBehaviour_Behaviour(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _removeBehaviour_Behaviour().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _removeBehaviour_Behaviour(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _removeBehaviour_Behaviour(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _removeBehaviour_Behaviour_Caller : public muscle::CallerAttributes<void>
{
	public: _removeBehaviour_Behaviour_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_removeBehaviour_Behaviour(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: jobject receive()
{
	muscle_core_kernel_CAController::_receive_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _receive()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "receive", false, "Ljade/lang/acl/ACLMessage;", argTypes);
}
public: static jmethodID _receive(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _receive().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _receive(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _receive(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _receive_Caller : public muscle::CallerAttributes<jobject>
{
	public: _receive_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_receive(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call()
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jobject receive_MessageTemplate(jobject A0)
{
	muscle_core_kernel_CAController::_receive_MessageTemplate_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _receive_MessageTemplate()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljade/lang/acl/MessageTemplate;" );
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "receive", false, "Ljade/lang/acl/ACLMessage;", argTypes);
}
public: static jmethodID _receive_MessageTemplate(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _receive_MessageTemplate().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _receive_MessageTemplate(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _receive_MessageTemplate(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _receive_MessageTemplate_Caller : public muscle::CallerAttributes<jobject>
{
	public: _receive_MessageTemplate_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_receive_MessageTemplate(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call(jobject A0)
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: void putBack_ACLMessage(jobject A0)
{
	muscle_core_kernel_CAController::_putBack_ACLMessage_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _putBack_ACLMessage()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljade/lang/acl/ACLMessage;" );
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "putBack", false, "", argTypes);
}
public: static jmethodID _putBack_ACLMessage(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _putBack_ACLMessage().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _putBack_ACLMessage(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _putBack_ACLMessage(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _putBack_ACLMessage_Caller : public muscle::CallerAttributes<void>
{
	public: _putBack_ACLMessage_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_putBack_ACLMessage(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: void waitUntilStarted()
{
	muscle_core_kernel_CAController::_waitUntilStarted_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _waitUntilStarted()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "waitUntilStarted", false, "", argTypes);
}
public: static jmethodID _waitUntilStarted(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _waitUntilStarted().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _waitUntilStarted(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _waitUntilStarted(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _waitUntilStarted_Caller : public muscle::CallerAttributes<void>
{
	public: _waitUntilStarted_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_waitUntilStarted(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call()
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void notifyChangeBehaviourState_Behaviour_String_String(jobject A0, jstring A1, jstring A2)
{
	muscle_core_kernel_CAController::_notifyChangeBehaviourState_Behaviour_String_String_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0, A1, A2);
}
public: static muscle::NativeAccessMethod _notifyChangeBehaviourState_Behaviour_String_String()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljade/core/behaviours/Behaviour;" );
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "Ljava/lang/String;" );
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "notifyChangeBehaviourState", false, "", argTypes);
}
public: static jmethodID _notifyChangeBehaviourState_Behaviour_String_String(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _notifyChangeBehaviourState_Behaviour_String_String().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _notifyChangeBehaviourState_Behaviour_String_String(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _notifyChangeBehaviourState_Behaviour_String_String(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _notifyChangeBehaviourState_Behaviour_String_String_Caller : public muscle::CallerAttributes<void>
{
	public: _notifyChangeBehaviourState_Behaviour_String_String_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_notifyChangeBehaviourState_Behaviour_String_String(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0, jstring A1, jstring A2)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0, A1, A2);
	}
	private: bool isStatic;
};
//
public: void setGenerateBehaviourEvents_boolean(jboolean A0)
{
	muscle_core_kernel_CAController::_setGenerateBehaviourEvents_boolean_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _setGenerateBehaviourEvents_boolean()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Z" );
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "setGenerateBehaviourEvents", false, "", argTypes);
}
public: static jmethodID _setGenerateBehaviourEvents_boolean(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _setGenerateBehaviourEvents_boolean().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _setGenerateBehaviourEvents_boolean(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _setGenerateBehaviourEvents_boolean(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _setGenerateBehaviourEvents_boolean_Caller : public muscle::CallerAttributes<void>
{
	public: _setGenerateBehaviourEvents_boolean_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_setGenerateBehaviourEvents_boolean(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jboolean A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: void postMessage_ACLMessage(jobject A0)
{
	muscle_core_kernel_CAController::_postMessage_ACLMessage_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _postMessage_ACLMessage()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljade/lang/acl/ACLMessage;" );
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "postMessage", false, "", argTypes);
}
public: static jmethodID _postMessage_ACLMessage(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _postMessage_ACLMessage().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _postMessage_ACLMessage(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _postMessage_ACLMessage(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _postMessage_ACLMessage_Caller : public muscle::CallerAttributes<void>
{
	public: _postMessage_ACLMessage_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_postMessage_ACLMessage(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: jobject getContentManager()
{
	muscle_core_kernel_CAController::_getContentManager_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getContentManager()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "getContentManager", false, "Ljade/content/ContentManager;", argTypes);
}
public: static jmethodID _getContentManager(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getContentManager().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _getContentManager(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getContentManager(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _getContentManager_Caller : public muscle::CallerAttributes<jobject>
{
	public: _getContentManager_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_getContentManager(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call()
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jobject getHelper_String(jstring A0)
{
	muscle_core_kernel_CAController::_getHelper_String_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _getHelper_String()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/lang/String;" );
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "getHelper", false, "Linterface jade/core/ServiceHelper;", argTypes);
}
public: static jmethodID _getHelper_String(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getHelper_String().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _getHelper_String(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getHelper_String(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _getHelper_String_Caller : public muscle::CallerAttributes<jobject>
{
	public: _getHelper_String_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_getHelper_String(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call(jstring A0)
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: jobject getBootProperties()
{
	muscle_core_kernel_CAController::_getBootProperties_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getBootProperties()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "getBootProperties", false, "Ljade/util/leap/Properties;", argTypes);
}
public: static jmethodID _getBootProperties(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getBootProperties().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _getBootProperties(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getBootProperties(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _getBootProperties_Caller : public muscle::CallerAttributes<jobject>
{
	public: _getBootProperties_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_getBootProperties(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call()
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void wait_long_int(jlong A0, jint A1)
{
	muscle_core_kernel_CAController::_wait_long_int_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0, A1);
}
public: static muscle::NativeAccessMethod _wait_long_int()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "J" );
argTypes.push_back( "I" );
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "wait", false, "", argTypes);
}
public: static jmethodID _wait_long_int(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _wait_long_int().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _wait_long_int(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _wait_long_int(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _wait_long_int_Caller : public muscle::CallerAttributes<void>
{
	public: _wait_long_int_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_wait_long_int(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jlong A0, jint A1)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0, A1);
	}
	private: bool isStatic;
};
//
public: void wait()
{
	muscle_core_kernel_CAController::_wait_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _wait()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "wait", false, "", argTypes);
}
public: static jmethodID _wait(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _wait().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _wait(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _wait(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _wait_Caller : public muscle::CallerAttributes<void>
{
	public: _wait_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_wait(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call()
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void wait_long(jlong A0)
{
	muscle_core_kernel_CAController::_wait_long_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _wait_long()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "J" );
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "wait", false, "", argTypes);
}
public: static jmethodID _wait_long(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _wait_long().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _wait_long(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _wait_long(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _wait_long_Caller : public muscle::CallerAttributes<void>
{
	public: _wait_long_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_wait_long(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jlong A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: jint hashCode()
{
	muscle_core_kernel_CAController::_hashCode_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _hashCode()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "hashCode", false, "I", argTypes);
}
public: static jmethodID _hashCode(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _hashCode().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jint> > _hashCode(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _hashCode(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jint> >( new muscle::MethodCaller<jint>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _hashCode_Caller : public muscle::CallerAttributes<jint>
{
	public: _hashCode_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jint>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_hashCode(env, isStatic))
	{
		assert(isStatic == false);
	}
	jint call()
	{
		return muscle::CallerAttributes<jint>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jclass getClass()
{
	muscle_core_kernel_CAController::_getClass_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getClass()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "getClass", false, "Ljava/lang/Class;", argTypes);
}
public: static jmethodID _getClass(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getClass().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jclass> > _getClass(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getClass(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jclass> >( new muscle::MethodCaller<jclass>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _getClass_Caller : public muscle::CallerAttributes<jclass>
{
	public: _getClass_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jclass>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_getClass(env, isStatic))
	{
		assert(isStatic == false);
	}
	jclass call()
	{
		return muscle::CallerAttributes<jclass>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jboolean equals_Object(jobject A0)
{
	muscle_core_kernel_CAController::_equals_Object_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _equals_Object()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/lang/Object;" );
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "equals", false, "Z", argTypes);
}
public: static jmethodID _equals_Object(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _equals_Object().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jboolean> > _equals_Object(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _equals_Object(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jboolean> >( new muscle::MethodCaller<jboolean>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _equals_Object_Caller : public muscle::CallerAttributes<jboolean>
{
	public: _equals_Object_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jboolean>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_equals_Object(env, isStatic))
	{
		assert(isStatic == false);
	}
	jboolean call(jobject A0)
	{
		return muscle::CallerAttributes<jboolean>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: jstring toString()
{
	muscle_core_kernel_CAController::_toString_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _toString()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "toString", false, "Ljava/lang/String;", argTypes);
}
public: static jmethodID _toString(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _toString().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jstring> > _toString(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _toString(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jstring> >( new muscle::MethodCaller<jstring>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _toString_Caller : public muscle::CallerAttributes<jstring>
{
	public: _toString_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jstring>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_toString(env, isStatic))
	{
		assert(isStatic == false);
	}
	jstring call()
	{
		return muscle::CallerAttributes<jstring>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void notify()
{
	muscle_core_kernel_CAController::_notify_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _notify()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "notify", false, "", argTypes);
}
public: static jmethodID _notify(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _notify().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _notify(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _notify(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _notify_Caller : public muscle::CallerAttributes<void>
{
	public: _notify_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_notify(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call()
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void notifyAll()
{
	muscle_core_kernel_CAController::_notifyAll_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _notifyAll()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/kernel/CAController", "notifyAll", false, "", argTypes);
}
public: static jmethodID _notifyAll(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _notifyAll().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _notifyAll(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _notifyAll(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.kernel.CAController
do not instantiate this class manually, better use a muscle_core_kernel_CAController objectand call this function directly
\author Jan Hegewald
*/
class _notifyAll_Caller : public muscle::CallerAttributes<void>
{
	public: _notifyAll_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_kernel_CAController::_CLASSNAME(), muscle_core_kernel_CAController::_notifyAll(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call()
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
};
/**
cpp counterpart class to access the java class javatool.LoggerTool
function names prefixed with an underscore _ are cpp only utility functions which do not exist in the java class
\author Jan Hegewald
*/
class javatool_LoggerTool
{
/**
function which returns the classname as a cpp jni signature (e.g. java_lang_String)
*/
	public: static std::string _CLASSNAME()
	{
		return "javatool/LoggerTool";
	}
	public: javatool_LoggerTool(JNIEnv* newEnv, jobject newDelegate)
	: jenv(newEnv), delegate(newDelegate)
{
	// init jvm pointer
	jint result = newEnv->GetJavaVM(&jvm);
	if(result < 0)
		throw std::runtime_error("error obtaining JVM");
	// see if we got the right jobject
	muscle::JNITool::assertInstanceOf(newEnv, newDelegate, _CLASSNAME(), __FILE__, __LINE__);
}
private: JNIEnv* jenv;
private: JavaVM* jvm;
private: jobject delegate;
/**
returns a reference to the JNIEnv pointer belonging to the current thread
*/
public: JNIEnv*& _GETJNIENV()
{
	jint result = jvm->AttachCurrentThread((void **)&jenv, NULL);
	if(result != JNI_OK)
		throw std::runtime_error("error obtaining Java env");
	return jenv;
}
//
public: jobject loggableLevel_Logger(jobject A0)
{
	javatool_LoggerTool::_loggableLevel_Logger_Caller caller(_GETJNIENV());
	return caller.call(A0);
}
public: static jobject loggableLevel_Logger(JNIEnv*& env, jobject A0)
{
	javatool_LoggerTool::_loggableLevel_Logger_Caller caller(env);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _loggableLevel_Logger()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/util/logging/Logger;" );
return muscle::NativeAccessMethod("javatool/LoggerTool", "loggableLevel", true, "Ljava/util/logging/Level;", argTypes);
}
public: static jmethodID _loggableLevel_Logger(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = true;
	jmethodID mid = _loggableLevel_Logger().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _loggableLevel_Logger(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _loggableLevel_Logger(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	jclass cls = env->FindClass(_CLASSNAME().c_str());
muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return std::tr1::shared_ptr<muscle::MethodCallerStatic<jobject> >( new muscle::MethodCallerStatic<jobject>(env, cls, _CLASSNAME(), mid) );
}
public: std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _loggableLevel_Logger(JNIEnv*& env)
{
	bool isStatic;
	jmethodID mid = _loggableLevel_Logger(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return std::tr1::shared_ptr<muscle::MethodCallerStatic<jobject> >( new muscle::MethodCallerStatic<jobject>(env, cls, _CLASSNAME(), mid) );
}
/**
helper class to cache a static jni method invocation procedure
this caller can be used to invoke a static method on the target java class javatool.LoggerTool
it should be used if there is no object of the target class javatool_LoggerTool (which is not required for static calls) and the method needs to be invoked frequently
\author Jan Hegewald
*/
class _loggableLevel_Logger_Caller : public muscle::CallerStaticAttributes<jobject>
{
	public: _loggableLevel_Logger_Caller(JNIEnv* env)
	: muscle::CallerStaticAttributes<jobject>(env, env->FindClass(javatool_LoggerTool::_CLASSNAME().c_str()), javatool_LoggerTool::_CLASSNAME(), javatool_LoggerTool::_loggableLevel_Logger(env, isStatic))
	{
		assert(isStatic == true);
	}
	jobject call(jobject A0)
	{
		return muscle::CallerStaticAttributes<jobject>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: void wait_long_int(jlong A0, jint A1)
{
	javatool_LoggerTool::_wait_long_int_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0, A1);
}
public: static muscle::NativeAccessMethod _wait_long_int()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "J" );
argTypes.push_back( "I" );
return muscle::NativeAccessMethod("javatool/LoggerTool", "wait", false, "", argTypes);
}
public: static jmethodID _wait_long_int(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _wait_long_int().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _wait_long_int(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _wait_long_int(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type javatool.LoggerTool
do not instantiate this class manually, better use a javatool_LoggerTool objectand call this function directly
\author Jan Hegewald
*/
class _wait_long_int_Caller : public muscle::CallerAttributes<void>
{
	public: _wait_long_int_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, javatool_LoggerTool::_CLASSNAME(), javatool_LoggerTool::_wait_long_int(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jlong A0, jint A1)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0, A1);
	}
	private: bool isStatic;
};
//
public: void wait()
{
	javatool_LoggerTool::_wait_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _wait()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("javatool/LoggerTool", "wait", false, "", argTypes);
}
public: static jmethodID _wait(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _wait().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _wait(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _wait(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type javatool.LoggerTool
do not instantiate this class manually, better use a javatool_LoggerTool objectand call this function directly
\author Jan Hegewald
*/
class _wait_Caller : public muscle::CallerAttributes<void>
{
	public: _wait_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, javatool_LoggerTool::_CLASSNAME(), javatool_LoggerTool::_wait(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call()
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void wait_long(jlong A0)
{
	javatool_LoggerTool::_wait_long_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _wait_long()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "J" );
return muscle::NativeAccessMethod("javatool/LoggerTool", "wait", false, "", argTypes);
}
public: static jmethodID _wait_long(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _wait_long().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _wait_long(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _wait_long(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type javatool.LoggerTool
do not instantiate this class manually, better use a javatool_LoggerTool objectand call this function directly
\author Jan Hegewald
*/
class _wait_long_Caller : public muscle::CallerAttributes<void>
{
	public: _wait_long_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, javatool_LoggerTool::_CLASSNAME(), javatool_LoggerTool::_wait_long(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jlong A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: jint hashCode()
{
	javatool_LoggerTool::_hashCode_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _hashCode()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("javatool/LoggerTool", "hashCode", false, "I", argTypes);
}
public: static jmethodID _hashCode(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _hashCode().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jint> > _hashCode(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _hashCode(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jint> >( new muscle::MethodCaller<jint>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type javatool.LoggerTool
do not instantiate this class manually, better use a javatool_LoggerTool objectand call this function directly
\author Jan Hegewald
*/
class _hashCode_Caller : public muscle::CallerAttributes<jint>
{
	public: _hashCode_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jint>(env, obj, javatool_LoggerTool::_CLASSNAME(), javatool_LoggerTool::_hashCode(env, isStatic))
	{
		assert(isStatic == false);
	}
	jint call()
	{
		return muscle::CallerAttributes<jint>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jclass getClass()
{
	javatool_LoggerTool::_getClass_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getClass()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("javatool/LoggerTool", "getClass", false, "Ljava/lang/Class;", argTypes);
}
public: static jmethodID _getClass(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getClass().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jclass> > _getClass(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getClass(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jclass> >( new muscle::MethodCaller<jclass>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type javatool.LoggerTool
do not instantiate this class manually, better use a javatool_LoggerTool objectand call this function directly
\author Jan Hegewald
*/
class _getClass_Caller : public muscle::CallerAttributes<jclass>
{
	public: _getClass_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jclass>(env, obj, javatool_LoggerTool::_CLASSNAME(), javatool_LoggerTool::_getClass(env, isStatic))
	{
		assert(isStatic == false);
	}
	jclass call()
	{
		return muscle::CallerAttributes<jclass>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jboolean equals_Object(jobject A0)
{
	javatool_LoggerTool::_equals_Object_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _equals_Object()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/lang/Object;" );
return muscle::NativeAccessMethod("javatool/LoggerTool", "equals", false, "Z", argTypes);
}
public: static jmethodID _equals_Object(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _equals_Object().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jboolean> > _equals_Object(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _equals_Object(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jboolean> >( new muscle::MethodCaller<jboolean>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type javatool.LoggerTool
do not instantiate this class manually, better use a javatool_LoggerTool objectand call this function directly
\author Jan Hegewald
*/
class _equals_Object_Caller : public muscle::CallerAttributes<jboolean>
{
	public: _equals_Object_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jboolean>(env, obj, javatool_LoggerTool::_CLASSNAME(), javatool_LoggerTool::_equals_Object(env, isStatic))
	{
		assert(isStatic == false);
	}
	jboolean call(jobject A0)
	{
		return muscle::CallerAttributes<jboolean>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: jstring toString()
{
	javatool_LoggerTool::_toString_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _toString()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("javatool/LoggerTool", "toString", false, "Ljava/lang/String;", argTypes);
}
public: static jmethodID _toString(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _toString().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jstring> > _toString(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _toString(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jstring> >( new muscle::MethodCaller<jstring>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type javatool.LoggerTool
do not instantiate this class manually, better use a javatool_LoggerTool objectand call this function directly
\author Jan Hegewald
*/
class _toString_Caller : public muscle::CallerAttributes<jstring>
{
	public: _toString_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jstring>(env, obj, javatool_LoggerTool::_CLASSNAME(), javatool_LoggerTool::_toString(env, isStatic))
	{
		assert(isStatic == false);
	}
	jstring call()
	{
		return muscle::CallerAttributes<jstring>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void notify()
{
	javatool_LoggerTool::_notify_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _notify()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("javatool/LoggerTool", "notify", false, "", argTypes);
}
public: static jmethodID _notify(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _notify().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _notify(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _notify(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type javatool.LoggerTool
do not instantiate this class manually, better use a javatool_LoggerTool objectand call this function directly
\author Jan Hegewald
*/
class _notify_Caller : public muscle::CallerAttributes<void>
{
	public: _notify_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, javatool_LoggerTool::_CLASSNAME(), javatool_LoggerTool::_notify(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call()
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void notifyAll()
{
	javatool_LoggerTool::_notifyAll_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _notifyAll()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("javatool/LoggerTool", "notifyAll", false, "", argTypes);
}
public: static jmethodID _notifyAll(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _notifyAll().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _notifyAll(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _notifyAll(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type javatool.LoggerTool
do not instantiate this class manually, better use a javatool_LoggerTool objectand call this function directly
\author Jan Hegewald
*/
class _notifyAll_Caller : public muscle::CallerAttributes<void>
{
	public: _notifyAll_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, javatool_LoggerTool::_CLASSNAME(), javatool_LoggerTool::_notifyAll(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call()
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
};
/**
cpp counterpart class to access the java class java.util.logging.Logger
function names prefixed with an underscore _ are cpp only utility functions which do not exist in the java class
\author Jan Hegewald
*/
class java_util_logging_Logger
{
/**
function which returns the classname as a cpp jni signature (e.g. java_lang_String)
*/
	public: static std::string _CLASSNAME()
	{
		return "java/util/logging/Logger";
	}
	public: java_util_logging_Logger(JNIEnv* newEnv, jobject newDelegate)
	: jenv(newEnv), delegate(newDelegate)
{
	// init jvm pointer
	jint result = newEnv->GetJavaVM(&jvm);
	if(result < 0)
		throw std::runtime_error("error obtaining JVM");
	// see if we got the right jobject
	muscle::JNITool::assertInstanceOf(newEnv, newDelegate, _CLASSNAME(), __FILE__, __LINE__);
}
private: JNIEnv* jenv;
private: JavaVM* jvm;
private: jobject delegate;
/**
returns a reference to the JNIEnv pointer belonging to the current thread
*/
public: JNIEnv*& _GETJNIENV()
{
	jint result = jvm->AttachCurrentThread((void **)&jenv, NULL);
	if(result != JNI_OK)
		throw std::runtime_error("error obtaining Java env");
	return jenv;
}
//
public: void log_Level_String_Object_(jobject A0, jstring A1, jobjectArray A2)
{
	java_util_logging_Logger::_log_Level_String_Object__Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0, A1, A2);
}
public: static muscle::NativeAccessMethod _log_Level_String_Object_()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/util/logging/Level;" );
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "[Ljava/lang/Object;" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "log", false, "", argTypes);
}
public: static jmethodID _log_Level_String_Object_(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _log_Level_String_Object_().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _log_Level_String_Object_(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _log_Level_String_Object_(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _log_Level_String_Object__Caller : public muscle::CallerAttributes<void>
{
	public: _log_Level_String_Object__Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_log_Level_String_Object_(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0, jstring A1, jobjectArray A2)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0, A1, A2);
	}
	private: bool isStatic;
};
//
public: void log_LogRecord(jobject A0)
{
	java_util_logging_Logger::_log_LogRecord_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _log_LogRecord()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/util/logging/LogRecord;" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "log", false, "", argTypes);
}
public: static jmethodID _log_LogRecord(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _log_LogRecord().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _log_LogRecord(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _log_LogRecord(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _log_LogRecord_Caller : public muscle::CallerAttributes<void>
{
	public: _log_LogRecord_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_log_LogRecord(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: void log_Level_String_Throwable(jobject A0, jstring A1, jthrowable A2)
{
	java_util_logging_Logger::_log_Level_String_Throwable_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0, A1, A2);
}
public: static muscle::NativeAccessMethod _log_Level_String_Throwable()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/util/logging/Level;" );
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "Ljava/lang/Throwable;" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "log", false, "", argTypes);
}
public: static jmethodID _log_Level_String_Throwable(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _log_Level_String_Throwable().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _log_Level_String_Throwable(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _log_Level_String_Throwable(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _log_Level_String_Throwable_Caller : public muscle::CallerAttributes<void>
{
	public: _log_Level_String_Throwable_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_log_Level_String_Throwable(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0, jstring A1, jthrowable A2)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0, A1, A2);
	}
	private: bool isStatic;
};
//
public: void log_Level_String(jobject A0, jstring A1)
{
	java_util_logging_Logger::_log_Level_String_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0, A1);
}
public: static muscle::NativeAccessMethod _log_Level_String()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/util/logging/Level;" );
argTypes.push_back( "Ljava/lang/String;" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "log", false, "", argTypes);
}
public: static jmethodID _log_Level_String(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _log_Level_String().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _log_Level_String(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _log_Level_String(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _log_Level_String_Caller : public muscle::CallerAttributes<void>
{
	public: _log_Level_String_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_log_Level_String(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0, jstring A1)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0, A1);
	}
	private: bool isStatic;
};
//
public: void log_Level_String_Object(jobject A0, jstring A1, jobject A2)
{
	java_util_logging_Logger::_log_Level_String_Object_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0, A1, A2);
}
public: static muscle::NativeAccessMethod _log_Level_String_Object()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/util/logging/Level;" );
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "Ljava/lang/Object;" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "log", false, "", argTypes);
}
public: static jmethodID _log_Level_String_Object(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _log_Level_String_Object().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _log_Level_String_Object(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _log_Level_String_Object(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _log_Level_String_Object_Caller : public muscle::CallerAttributes<void>
{
	public: _log_Level_String_Object_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_log_Level_String_Object(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0, jstring A1, jobject A2)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0, A1, A2);
	}
	private: bool isStatic;
};
//
public: jstring getName()
{
	java_util_logging_Logger::_getName_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getName()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("java/util/logging/Logger", "getName", false, "Ljava/lang/String;", argTypes);
}
public: static jmethodID _getName(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getName().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jstring> > _getName(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getName(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jstring> >( new muscle::MethodCaller<jstring>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _getName_Caller : public muscle::CallerAttributes<jstring>
{
	public: _getName_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jstring>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_getName(env, isStatic))
	{
		assert(isStatic == false);
	}
	jstring call()
	{
		return muscle::CallerAttributes<jstring>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jobject getParent()
{
	java_util_logging_Logger::_getParent_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getParent()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("java/util/logging/Logger", "getParent", false, "Ljava/util/logging/Logger;", argTypes);
}
public: static jmethodID _getParent(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getParent().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _getParent(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getParent(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _getParent_Caller : public muscle::CallerAttributes<jobject>
{
	public: _getParent_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_getParent(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call()
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void setParent_Logger(jobject A0)
{
	java_util_logging_Logger::_setParent_Logger_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _setParent_Logger()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/util/logging/Logger;" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "setParent", false, "", argTypes);
}
public: static jmethodID _setParent_Logger(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _setParent_Logger().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _setParent_Logger(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _setParent_Logger(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _setParent_Logger_Caller : public muscle::CallerAttributes<void>
{
	public: _setParent_Logger_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_setParent_Logger(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: jobject getLogger_String(jstring A0)
{
	java_util_logging_Logger::_getLogger_String_Caller caller(_GETJNIENV());
	return caller.call(A0);
}
public: static jobject getLogger_String(JNIEnv*& env, jstring A0)
{
	java_util_logging_Logger::_getLogger_String_Caller caller(env);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _getLogger_String()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/lang/String;" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "getLogger", true, "Ljava/util/logging/Logger;", argTypes);
}
public: static jmethodID _getLogger_String(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = true;
	jmethodID mid = _getLogger_String().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _getLogger_String(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getLogger_String(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	jclass cls = env->FindClass(_CLASSNAME().c_str());
muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return std::tr1::shared_ptr<muscle::MethodCallerStatic<jobject> >( new muscle::MethodCallerStatic<jobject>(env, cls, _CLASSNAME(), mid) );
}
public: std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _getLogger_String(JNIEnv*& env)
{
	bool isStatic;
	jmethodID mid = _getLogger_String(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return std::tr1::shared_ptr<muscle::MethodCallerStatic<jobject> >( new muscle::MethodCallerStatic<jobject>(env, cls, _CLASSNAME(), mid) );
}
/**
helper class to cache a static jni method invocation procedure
this caller can be used to invoke a static method on the target java class java.util.logging.Logger
it should be used if there is no object of the target class java_util_logging_Logger (which is not required for static calls) and the method needs to be invoked frequently
\author Jan Hegewald
*/
class _getLogger_String_Caller : public muscle::CallerStaticAttributes<jobject>
{
	public: _getLogger_String_Caller(JNIEnv* env)
	: muscle::CallerStaticAttributes<jobject>(env, env->FindClass(java_util_logging_Logger::_CLASSNAME().c_str()), java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_getLogger_String(env, isStatic))
	{
		assert(isStatic == true);
	}
	jobject call(jstring A0)
	{
		return muscle::CallerStaticAttributes<jobject>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: jobject getLogger_String_String(jstring A0, jstring A1)
{
	java_util_logging_Logger::_getLogger_String_String_Caller caller(_GETJNIENV());
	return caller.call(A0, A1);
}
public: static jobject getLogger_String_String(JNIEnv*& env, jstring A0, jstring A1)
{
	java_util_logging_Logger::_getLogger_String_String_Caller caller(env);
	return caller.call(A0, A1);
}
public: static muscle::NativeAccessMethod _getLogger_String_String()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "Ljava/lang/String;" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "getLogger", true, "Ljava/util/logging/Logger;", argTypes);
}
public: static jmethodID _getLogger_String_String(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = true;
	jmethodID mid = _getLogger_String_String().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _getLogger_String_String(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getLogger_String_String(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	jclass cls = env->FindClass(_CLASSNAME().c_str());
muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return std::tr1::shared_ptr<muscle::MethodCallerStatic<jobject> >( new muscle::MethodCallerStatic<jobject>(env, cls, _CLASSNAME(), mid) );
}
public: std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _getLogger_String_String(JNIEnv*& env)
{
	bool isStatic;
	jmethodID mid = _getLogger_String_String(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return std::tr1::shared_ptr<muscle::MethodCallerStatic<jobject> >( new muscle::MethodCallerStatic<jobject>(env, cls, _CLASSNAME(), mid) );
}
/**
helper class to cache a static jni method invocation procedure
this caller can be used to invoke a static method on the target java class java.util.logging.Logger
it should be used if there is no object of the target class java_util_logging_Logger (which is not required for static calls) and the method needs to be invoked frequently
\author Jan Hegewald
*/
class _getLogger_String_String_Caller : public muscle::CallerStaticAttributes<jobject>
{
	public: _getLogger_String_String_Caller(JNIEnv* env)
	: muscle::CallerStaticAttributes<jobject>(env, env->FindClass(java_util_logging_Logger::_CLASSNAME().c_str()), java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_getLogger_String_String(env, isStatic))
	{
		assert(isStatic == true);
	}
	jobject call(jstring A0, jstring A1)
	{
		return muscle::CallerStaticAttributes<jobject>::call(muscle::VABEGIN::FIRST, A0, A1);
	}
	private: bool isStatic;
};
//
public: void warning_String(jstring A0)
{
	java_util_logging_Logger::_warning_String_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _warning_String()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/lang/String;" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "warning", false, "", argTypes);
}
public: static jmethodID _warning_String(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _warning_String().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _warning_String(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _warning_String(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _warning_String_Caller : public muscle::CallerAttributes<void>
{
	public: _warning_String_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_warning_String(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jstring A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: void info_String(jstring A0)
{
	java_util_logging_Logger::_info_String_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _info_String()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/lang/String;" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "info", false, "", argTypes);
}
public: static jmethodID _info_String(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _info_String().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _info_String(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _info_String(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _info_String_Caller : public muscle::CallerAttributes<void>
{
	public: _info_String_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_info_String(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jstring A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: jboolean isLoggable_Level(jobject A0)
{
	java_util_logging_Logger::_isLoggable_Level_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _isLoggable_Level()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/util/logging/Level;" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "isLoggable", false, "Z", argTypes);
}
public: static jmethodID _isLoggable_Level(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _isLoggable_Level().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jboolean> > _isLoggable_Level(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _isLoggable_Level(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jboolean> >( new muscle::MethodCaller<jboolean>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _isLoggable_Level_Caller : public muscle::CallerAttributes<jboolean>
{
	public: _isLoggable_Level_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jboolean>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_isLoggable_Level(env, isStatic))
	{
		assert(isStatic == false);
	}
	jboolean call(jobject A0)
	{
		return muscle::CallerAttributes<jboolean>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: void fine_String(jstring A0)
{
	java_util_logging_Logger::_fine_String_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _fine_String()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/lang/String;" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "fine", false, "", argTypes);
}
public: static jmethodID _fine_String(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _fine_String().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _fine_String(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _fine_String(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _fine_String_Caller : public muscle::CallerAttributes<void>
{
	public: _fine_String_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_fine_String(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jstring A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: void severe_String(jstring A0)
{
	java_util_logging_Logger::_severe_String_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _severe_String()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/lang/String;" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "severe", false, "", argTypes);
}
public: static jmethodID _severe_String(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _severe_String().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _severe_String(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _severe_String(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _severe_String_Caller : public muscle::CallerAttributes<void>
{
	public: _severe_String_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_severe_String(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jstring A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: jobject getAnonymousLogger()
{
	java_util_logging_Logger::_getAnonymousLogger_Caller caller(_GETJNIENV());
	return caller.call();
}
public: static jobject getAnonymousLogger(JNIEnv*& env)
{
	java_util_logging_Logger::_getAnonymousLogger_Caller caller(env);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getAnonymousLogger()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("java/util/logging/Logger", "getAnonymousLogger", true, "Ljava/util/logging/Logger;", argTypes);
}
public: static jmethodID _getAnonymousLogger(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = true;
	jmethodID mid = _getAnonymousLogger().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _getAnonymousLogger(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getAnonymousLogger(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	jclass cls = env->FindClass(_CLASSNAME().c_str());
muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return std::tr1::shared_ptr<muscle::MethodCallerStatic<jobject> >( new muscle::MethodCallerStatic<jobject>(env, cls, _CLASSNAME(), mid) );
}
public: std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _getAnonymousLogger(JNIEnv*& env)
{
	bool isStatic;
	jmethodID mid = _getAnonymousLogger(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return std::tr1::shared_ptr<muscle::MethodCallerStatic<jobject> >( new muscle::MethodCallerStatic<jobject>(env, cls, _CLASSNAME(), mid) );
}
/**
helper class to cache a static jni method invocation procedure
this caller can be used to invoke a static method on the target java class java.util.logging.Logger
it should be used if there is no object of the target class java_util_logging_Logger (which is not required for static calls) and the method needs to be invoked frequently
\author Jan Hegewald
*/
class _getAnonymousLogger_Caller : public muscle::CallerStaticAttributes<jobject>
{
	public: _getAnonymousLogger_Caller(JNIEnv* env)
	: muscle::CallerStaticAttributes<jobject>(env, env->FindClass(java_util_logging_Logger::_CLASSNAME().c_str()), java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_getAnonymousLogger(env, isStatic))
	{
		assert(isStatic == true);
	}
	jobject call()
	{
		return muscle::CallerStaticAttributes<jobject>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jobject getAnonymousLogger_String(jstring A0)
{
	java_util_logging_Logger::_getAnonymousLogger_String_Caller caller(_GETJNIENV());
	return caller.call(A0);
}
public: static jobject getAnonymousLogger_String(JNIEnv*& env, jstring A0)
{
	java_util_logging_Logger::_getAnonymousLogger_String_Caller caller(env);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _getAnonymousLogger_String()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/lang/String;" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "getAnonymousLogger", true, "Ljava/util/logging/Logger;", argTypes);
}
public: static jmethodID _getAnonymousLogger_String(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = true;
	jmethodID mid = _getAnonymousLogger_String().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _getAnonymousLogger_String(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getAnonymousLogger_String(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	jclass cls = env->FindClass(_CLASSNAME().c_str());
muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return std::tr1::shared_ptr<muscle::MethodCallerStatic<jobject> >( new muscle::MethodCallerStatic<jobject>(env, cls, _CLASSNAME(), mid) );
}
public: std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _getAnonymousLogger_String(JNIEnv*& env)
{
	bool isStatic;
	jmethodID mid = _getAnonymousLogger_String(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return std::tr1::shared_ptr<muscle::MethodCallerStatic<jobject> >( new muscle::MethodCallerStatic<jobject>(env, cls, _CLASSNAME(), mid) );
}
/**
helper class to cache a static jni method invocation procedure
this caller can be used to invoke a static method on the target java class java.util.logging.Logger
it should be used if there is no object of the target class java_util_logging_Logger (which is not required for static calls) and the method needs to be invoked frequently
\author Jan Hegewald
*/
class _getAnonymousLogger_String_Caller : public muscle::CallerStaticAttributes<jobject>
{
	public: _getAnonymousLogger_String_Caller(JNIEnv* env)
	: muscle::CallerStaticAttributes<jobject>(env, env->FindClass(java_util_logging_Logger::_CLASSNAME().c_str()), java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_getAnonymousLogger_String(env, isStatic))
	{
		assert(isStatic == true);
	}
	jobject call(jstring A0)
	{
		return muscle::CallerStaticAttributes<jobject>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: jobject getResourceBundle()
{
	java_util_logging_Logger::_getResourceBundle_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getResourceBundle()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("java/util/logging/Logger", "getResourceBundle", false, "Ljava/util/ResourceBundle;", argTypes);
}
public: static jmethodID _getResourceBundle(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getResourceBundle().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _getResourceBundle(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getResourceBundle(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _getResourceBundle_Caller : public muscle::CallerAttributes<jobject>
{
	public: _getResourceBundle_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_getResourceBundle(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call()
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jstring getResourceBundleName()
{
	java_util_logging_Logger::_getResourceBundleName_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getResourceBundleName()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("java/util/logging/Logger", "getResourceBundleName", false, "Ljava/lang/String;", argTypes);
}
public: static jmethodID _getResourceBundleName(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getResourceBundleName().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jstring> > _getResourceBundleName(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getResourceBundleName(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jstring> >( new muscle::MethodCaller<jstring>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _getResourceBundleName_Caller : public muscle::CallerAttributes<jstring>
{
	public: _getResourceBundleName_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jstring>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_getResourceBundleName(env, isStatic))
	{
		assert(isStatic == false);
	}
	jstring call()
	{
		return muscle::CallerAttributes<jstring>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void setFilter_Filter(jobject A0)
{
	java_util_logging_Logger::_setFilter_Filter_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _setFilter_Filter()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Linterface java/util/logging/Filter;" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "setFilter", false, "", argTypes);
}
public: static jmethodID _setFilter_Filter(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _setFilter_Filter().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _setFilter_Filter(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _setFilter_Filter(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _setFilter_Filter_Caller : public muscle::CallerAttributes<void>
{
	public: _setFilter_Filter_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_setFilter_Filter(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: jobject getFilter()
{
	java_util_logging_Logger::_getFilter_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getFilter()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("java/util/logging/Logger", "getFilter", false, "Linterface java/util/logging/Filter;", argTypes);
}
public: static jmethodID _getFilter(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getFilter().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _getFilter(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getFilter(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _getFilter_Caller : public muscle::CallerAttributes<jobject>
{
	public: _getFilter_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_getFilter(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call()
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void logp_Level_String_String_String_Object_(jobject A0, jstring A1, jstring A2, jstring A3, jobjectArray A4)
{
	java_util_logging_Logger::_logp_Level_String_String_String_Object__Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0, A1, A2, A3, A4);
}
public: static muscle::NativeAccessMethod _logp_Level_String_String_String_Object_()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/util/logging/Level;" );
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "[Ljava/lang/Object;" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "logp", false, "", argTypes);
}
public: static jmethodID _logp_Level_String_String_String_Object_(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _logp_Level_String_String_String_Object_().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _logp_Level_String_String_String_Object_(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _logp_Level_String_String_String_Object_(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _logp_Level_String_String_String_Object__Caller : public muscle::CallerAttributes<void>
{
	public: _logp_Level_String_String_String_Object__Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_logp_Level_String_String_String_Object_(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0, jstring A1, jstring A2, jstring A3, jobjectArray A4)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0, A1, A2, A3, A4);
	}
	private: bool isStatic;
};
//
public: void logp_Level_String_String_String_Throwable(jobject A0, jstring A1, jstring A2, jstring A3, jthrowable A4)
{
	java_util_logging_Logger::_logp_Level_String_String_String_Throwable_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0, A1, A2, A3, A4);
}
public: static muscle::NativeAccessMethod _logp_Level_String_String_String_Throwable()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/util/logging/Level;" );
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "Ljava/lang/Throwable;" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "logp", false, "", argTypes);
}
public: static jmethodID _logp_Level_String_String_String_Throwable(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _logp_Level_String_String_String_Throwable().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _logp_Level_String_String_String_Throwable(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _logp_Level_String_String_String_Throwable(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _logp_Level_String_String_String_Throwable_Caller : public muscle::CallerAttributes<void>
{
	public: _logp_Level_String_String_String_Throwable_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_logp_Level_String_String_String_Throwable(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0, jstring A1, jstring A2, jstring A3, jthrowable A4)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0, A1, A2, A3, A4);
	}
	private: bool isStatic;
};
//
public: void logp_Level_String_String_String_Object(jobject A0, jstring A1, jstring A2, jstring A3, jobject A4)
{
	java_util_logging_Logger::_logp_Level_String_String_String_Object_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0, A1, A2, A3, A4);
}
public: static muscle::NativeAccessMethod _logp_Level_String_String_String_Object()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/util/logging/Level;" );
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "Ljava/lang/Object;" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "logp", false, "", argTypes);
}
public: static jmethodID _logp_Level_String_String_String_Object(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _logp_Level_String_String_String_Object().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _logp_Level_String_String_String_Object(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _logp_Level_String_String_String_Object(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _logp_Level_String_String_String_Object_Caller : public muscle::CallerAttributes<void>
{
	public: _logp_Level_String_String_String_Object_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_logp_Level_String_String_String_Object(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0, jstring A1, jstring A2, jstring A3, jobject A4)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0, A1, A2, A3, A4);
	}
	private: bool isStatic;
};
//
public: void logp_Level_String_String_String(jobject A0, jstring A1, jstring A2, jstring A3)
{
	java_util_logging_Logger::_logp_Level_String_String_String_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0, A1, A2, A3);
}
public: static muscle::NativeAccessMethod _logp_Level_String_String_String()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/util/logging/Level;" );
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "Ljava/lang/String;" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "logp", false, "", argTypes);
}
public: static jmethodID _logp_Level_String_String_String(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _logp_Level_String_String_String().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _logp_Level_String_String_String(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _logp_Level_String_String_String(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _logp_Level_String_String_String_Caller : public muscle::CallerAttributes<void>
{
	public: _logp_Level_String_String_String_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_logp_Level_String_String_String(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0, jstring A1, jstring A2, jstring A3)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0, A1, A2, A3);
	}
	private: bool isStatic;
};
//
public: void logrb_Level_String_String_String_String_Object_(jobject A0, jstring A1, jstring A2, jstring A3, jstring A4, jobjectArray A5)
{
	java_util_logging_Logger::_logrb_Level_String_String_String_String_Object__Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0, A1, A2, A3, A4, A5);
}
public: static muscle::NativeAccessMethod _logrb_Level_String_String_String_String_Object_()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/util/logging/Level;" );
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "[Ljava/lang/Object;" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "logrb", false, "", argTypes);
}
public: static jmethodID _logrb_Level_String_String_String_String_Object_(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _logrb_Level_String_String_String_String_Object_().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _logrb_Level_String_String_String_String_Object_(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _logrb_Level_String_String_String_String_Object_(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _logrb_Level_String_String_String_String_Object__Caller : public muscle::CallerAttributes<void>
{
	public: _logrb_Level_String_String_String_String_Object__Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_logrb_Level_String_String_String_String_Object_(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0, jstring A1, jstring A2, jstring A3, jstring A4, jobjectArray A5)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0, A1, A2, A3, A4, A5);
	}
	private: bool isStatic;
};
//
public: void logrb_Level_String_String_String_String(jobject A0, jstring A1, jstring A2, jstring A3, jstring A4)
{
	java_util_logging_Logger::_logrb_Level_String_String_String_String_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0, A1, A2, A3, A4);
}
public: static muscle::NativeAccessMethod _logrb_Level_String_String_String_String()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/util/logging/Level;" );
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "Ljava/lang/String;" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "logrb", false, "", argTypes);
}
public: static jmethodID _logrb_Level_String_String_String_String(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _logrb_Level_String_String_String_String().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _logrb_Level_String_String_String_String(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _logrb_Level_String_String_String_String(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _logrb_Level_String_String_String_String_Caller : public muscle::CallerAttributes<void>
{
	public: _logrb_Level_String_String_String_String_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_logrb_Level_String_String_String_String(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0, jstring A1, jstring A2, jstring A3, jstring A4)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0, A1, A2, A3, A4);
	}
	private: bool isStatic;
};
//
public: void logrb_Level_String_String_String_String_Object(jobject A0, jstring A1, jstring A2, jstring A3, jstring A4, jobject A5)
{
	java_util_logging_Logger::_logrb_Level_String_String_String_String_Object_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0, A1, A2, A3, A4, A5);
}
public: static muscle::NativeAccessMethod _logrb_Level_String_String_String_String_Object()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/util/logging/Level;" );
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "Ljava/lang/Object;" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "logrb", false, "", argTypes);
}
public: static jmethodID _logrb_Level_String_String_String_String_Object(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _logrb_Level_String_String_String_String_Object().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _logrb_Level_String_String_String_String_Object(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _logrb_Level_String_String_String_String_Object(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _logrb_Level_String_String_String_String_Object_Caller : public muscle::CallerAttributes<void>
{
	public: _logrb_Level_String_String_String_String_Object_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_logrb_Level_String_String_String_String_Object(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0, jstring A1, jstring A2, jstring A3, jstring A4, jobject A5)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0, A1, A2, A3, A4, A5);
	}
	private: bool isStatic;
};
//
public: void logrb_Level_String_String_String_String_Throwable(jobject A0, jstring A1, jstring A2, jstring A3, jstring A4, jthrowable A5)
{
	java_util_logging_Logger::_logrb_Level_String_String_String_String_Throwable_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0, A1, A2, A3, A4, A5);
}
public: static muscle::NativeAccessMethod _logrb_Level_String_String_String_String_Throwable()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/util/logging/Level;" );
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "Ljava/lang/Throwable;" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "logrb", false, "", argTypes);
}
public: static jmethodID _logrb_Level_String_String_String_String_Throwable(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _logrb_Level_String_String_String_String_Throwable().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _logrb_Level_String_String_String_String_Throwable(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _logrb_Level_String_String_String_String_Throwable(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _logrb_Level_String_String_String_String_Throwable_Caller : public muscle::CallerAttributes<void>
{
	public: _logrb_Level_String_String_String_String_Throwable_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_logrb_Level_String_String_String_String_Throwable(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0, jstring A1, jstring A2, jstring A3, jstring A4, jthrowable A5)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0, A1, A2, A3, A4, A5);
	}
	private: bool isStatic;
};
//
public: void entering_String_String(jstring A0, jstring A1)
{
	java_util_logging_Logger::_entering_String_String_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0, A1);
}
public: static muscle::NativeAccessMethod _entering_String_String()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "Ljava/lang/String;" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "entering", false, "", argTypes);
}
public: static jmethodID _entering_String_String(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _entering_String_String().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _entering_String_String(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _entering_String_String(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _entering_String_String_Caller : public muscle::CallerAttributes<void>
{
	public: _entering_String_String_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_entering_String_String(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jstring A0, jstring A1)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0, A1);
	}
	private: bool isStatic;
};
//
public: void entering_String_String_Object_(jstring A0, jstring A1, jobjectArray A2)
{
	java_util_logging_Logger::_entering_String_String_Object__Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0, A1, A2);
}
public: static muscle::NativeAccessMethod _entering_String_String_Object_()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "[Ljava/lang/Object;" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "entering", false, "", argTypes);
}
public: static jmethodID _entering_String_String_Object_(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _entering_String_String_Object_().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _entering_String_String_Object_(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _entering_String_String_Object_(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _entering_String_String_Object__Caller : public muscle::CallerAttributes<void>
{
	public: _entering_String_String_Object__Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_entering_String_String_Object_(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jstring A0, jstring A1, jobjectArray A2)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0, A1, A2);
	}
	private: bool isStatic;
};
//
public: void entering_String_String_Object(jstring A0, jstring A1, jobject A2)
{
	java_util_logging_Logger::_entering_String_String_Object_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0, A1, A2);
}
public: static muscle::NativeAccessMethod _entering_String_String_Object()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "Ljava/lang/Object;" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "entering", false, "", argTypes);
}
public: static jmethodID _entering_String_String_Object(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _entering_String_String_Object().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _entering_String_String_Object(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _entering_String_String_Object(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _entering_String_String_Object_Caller : public muscle::CallerAttributes<void>
{
	public: _entering_String_String_Object_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_entering_String_String_Object(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jstring A0, jstring A1, jobject A2)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0, A1, A2);
	}
	private: bool isStatic;
};
//
public: void exiting_String_String(jstring A0, jstring A1)
{
	java_util_logging_Logger::_exiting_String_String_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0, A1);
}
public: static muscle::NativeAccessMethod _exiting_String_String()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "Ljava/lang/String;" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "exiting", false, "", argTypes);
}
public: static jmethodID _exiting_String_String(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _exiting_String_String().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _exiting_String_String(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _exiting_String_String(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _exiting_String_String_Caller : public muscle::CallerAttributes<void>
{
	public: _exiting_String_String_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_exiting_String_String(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jstring A0, jstring A1)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0, A1);
	}
	private: bool isStatic;
};
//
public: void exiting_String_String_Object(jstring A0, jstring A1, jobject A2)
{
	java_util_logging_Logger::_exiting_String_String_Object_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0, A1, A2);
}
public: static muscle::NativeAccessMethod _exiting_String_String_Object()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "Ljava/lang/Object;" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "exiting", false, "", argTypes);
}
public: static jmethodID _exiting_String_String_Object(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _exiting_String_String_Object().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _exiting_String_String_Object(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _exiting_String_String_Object(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _exiting_String_String_Object_Caller : public muscle::CallerAttributes<void>
{
	public: _exiting_String_String_Object_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_exiting_String_String_Object(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jstring A0, jstring A1, jobject A2)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0, A1, A2);
	}
	private: bool isStatic;
};
//
public: void throwing_String_String_Throwable(jstring A0, jstring A1, jthrowable A2)
{
	java_util_logging_Logger::_throwing_String_String_Throwable_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0, A1, A2);
}
public: static muscle::NativeAccessMethod _throwing_String_String_Throwable()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "Ljava/lang/String;" );
argTypes.push_back( "Ljava/lang/Throwable;" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "throwing", false, "", argTypes);
}
public: static jmethodID _throwing_String_String_Throwable(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _throwing_String_String_Throwable().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _throwing_String_String_Throwable(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _throwing_String_String_Throwable(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _throwing_String_String_Throwable_Caller : public muscle::CallerAttributes<void>
{
	public: _throwing_String_String_Throwable_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_throwing_String_String_Throwable(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jstring A0, jstring A1, jthrowable A2)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0, A1, A2);
	}
	private: bool isStatic;
};
//
public: void config_String(jstring A0)
{
	java_util_logging_Logger::_config_String_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _config_String()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/lang/String;" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "config", false, "", argTypes);
}
public: static jmethodID _config_String(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _config_String().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _config_String(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _config_String(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _config_String_Caller : public muscle::CallerAttributes<void>
{
	public: _config_String_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_config_String(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jstring A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: void finer_String(jstring A0)
{
	java_util_logging_Logger::_finer_String_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _finer_String()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/lang/String;" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "finer", false, "", argTypes);
}
public: static jmethodID _finer_String(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _finer_String().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _finer_String(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _finer_String(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _finer_String_Caller : public muscle::CallerAttributes<void>
{
	public: _finer_String_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_finer_String(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jstring A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: void finest_String(jstring A0)
{
	java_util_logging_Logger::_finest_String_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _finest_String()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/lang/String;" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "finest", false, "", argTypes);
}
public: static jmethodID _finest_String(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _finest_String().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _finest_String(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _finest_String(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _finest_String_Caller : public muscle::CallerAttributes<void>
{
	public: _finest_String_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_finest_String(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jstring A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: void setLevel_Level(jobject A0)
{
	java_util_logging_Logger::_setLevel_Level_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _setLevel_Level()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/util/logging/Level;" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "setLevel", false, "", argTypes);
}
public: static jmethodID _setLevel_Level(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _setLevel_Level().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _setLevel_Level(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _setLevel_Level(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _setLevel_Level_Caller : public muscle::CallerAttributes<void>
{
	public: _setLevel_Level_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_setLevel_Level(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: jobject getLevel()
{
	java_util_logging_Logger::_getLevel_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getLevel()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("java/util/logging/Logger", "getLevel", false, "Ljava/util/logging/Level;", argTypes);
}
public: static jmethodID _getLevel(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getLevel().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _getLevel(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getLevel(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _getLevel_Caller : public muscle::CallerAttributes<jobject>
{
	public: _getLevel_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_getLevel(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call()
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void addHandler_Handler(jobject A0)
{
	java_util_logging_Logger::_addHandler_Handler_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _addHandler_Handler()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/util/logging/Handler;" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "addHandler", false, "", argTypes);
}
public: static jmethodID _addHandler_Handler(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _addHandler_Handler().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _addHandler_Handler(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _addHandler_Handler(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _addHandler_Handler_Caller : public muscle::CallerAttributes<void>
{
	public: _addHandler_Handler_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_addHandler_Handler(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: void removeHandler_Handler(jobject A0)
{
	java_util_logging_Logger::_removeHandler_Handler_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _removeHandler_Handler()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/util/logging/Handler;" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "removeHandler", false, "", argTypes);
}
public: static jmethodID _removeHandler_Handler(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _removeHandler_Handler().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _removeHandler_Handler(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _removeHandler_Handler(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _removeHandler_Handler_Caller : public muscle::CallerAttributes<void>
{
	public: _removeHandler_Handler_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_removeHandler_Handler(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: jobjectArray getHandlers()
{
	java_util_logging_Logger::_getHandlers_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getHandlers()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("java/util/logging/Logger", "getHandlers", false, "[Ljava/util/logging/Handler;", argTypes);
}
public: static jmethodID _getHandlers(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getHandlers().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobjectArray> > _getHandlers(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getHandlers(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobjectArray> >( new muscle::MethodCaller<jobjectArray>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _getHandlers_Caller : public muscle::CallerAttributes<jobjectArray>
{
	public: _getHandlers_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobjectArray>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_getHandlers(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobjectArray call()
	{
		return muscle::CallerAttributes<jobjectArray>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void setUseParentHandlers_boolean(jboolean A0)
{
	java_util_logging_Logger::_setUseParentHandlers_boolean_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _setUseParentHandlers_boolean()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Z" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "setUseParentHandlers", false, "", argTypes);
}
public: static jmethodID _setUseParentHandlers_boolean(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _setUseParentHandlers_boolean().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _setUseParentHandlers_boolean(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _setUseParentHandlers_boolean(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _setUseParentHandlers_boolean_Caller : public muscle::CallerAttributes<void>
{
	public: _setUseParentHandlers_boolean_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_setUseParentHandlers_boolean(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jboolean A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: jboolean getUseParentHandlers()
{
	java_util_logging_Logger::_getUseParentHandlers_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getUseParentHandlers()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("java/util/logging/Logger", "getUseParentHandlers", false, "Z", argTypes);
}
public: static jmethodID _getUseParentHandlers(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getUseParentHandlers().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jboolean> > _getUseParentHandlers(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getUseParentHandlers(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jboolean> >( new muscle::MethodCaller<jboolean>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _getUseParentHandlers_Caller : public muscle::CallerAttributes<jboolean>
{
	public: _getUseParentHandlers_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jboolean>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_getUseParentHandlers(env, isStatic))
	{
		assert(isStatic == false);
	}
	jboolean call()
	{
		return muscle::CallerAttributes<jboolean>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void wait_long_int(jlong A0, jint A1)
{
	java_util_logging_Logger::_wait_long_int_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0, A1);
}
public: static muscle::NativeAccessMethod _wait_long_int()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "J" );
argTypes.push_back( "I" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "wait", false, "", argTypes);
}
public: static jmethodID _wait_long_int(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _wait_long_int().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _wait_long_int(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _wait_long_int(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _wait_long_int_Caller : public muscle::CallerAttributes<void>
{
	public: _wait_long_int_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_wait_long_int(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jlong A0, jint A1)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0, A1);
	}
	private: bool isStatic;
};
//
public: void wait()
{
	java_util_logging_Logger::_wait_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _wait()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("java/util/logging/Logger", "wait", false, "", argTypes);
}
public: static jmethodID _wait(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _wait().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _wait(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _wait(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _wait_Caller : public muscle::CallerAttributes<void>
{
	public: _wait_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_wait(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call()
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void wait_long(jlong A0)
{
	java_util_logging_Logger::_wait_long_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _wait_long()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "J" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "wait", false, "", argTypes);
}
public: static jmethodID _wait_long(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _wait_long().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _wait_long(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _wait_long(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _wait_long_Caller : public muscle::CallerAttributes<void>
{
	public: _wait_long_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_wait_long(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jlong A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: jint hashCode()
{
	java_util_logging_Logger::_hashCode_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _hashCode()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("java/util/logging/Logger", "hashCode", false, "I", argTypes);
}
public: static jmethodID _hashCode(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _hashCode().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jint> > _hashCode(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _hashCode(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jint> >( new muscle::MethodCaller<jint>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _hashCode_Caller : public muscle::CallerAttributes<jint>
{
	public: _hashCode_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jint>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_hashCode(env, isStatic))
	{
		assert(isStatic == false);
	}
	jint call()
	{
		return muscle::CallerAttributes<jint>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jclass getClass()
{
	java_util_logging_Logger::_getClass_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getClass()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("java/util/logging/Logger", "getClass", false, "Ljava/lang/Class;", argTypes);
}
public: static jmethodID _getClass(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getClass().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jclass> > _getClass(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getClass(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jclass> >( new muscle::MethodCaller<jclass>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _getClass_Caller : public muscle::CallerAttributes<jclass>
{
	public: _getClass_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jclass>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_getClass(env, isStatic))
	{
		assert(isStatic == false);
	}
	jclass call()
	{
		return muscle::CallerAttributes<jclass>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jboolean equals_Object(jobject A0)
{
	java_util_logging_Logger::_equals_Object_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _equals_Object()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/lang/Object;" );
return muscle::NativeAccessMethod("java/util/logging/Logger", "equals", false, "Z", argTypes);
}
public: static jmethodID _equals_Object(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _equals_Object().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jboolean> > _equals_Object(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _equals_Object(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jboolean> >( new muscle::MethodCaller<jboolean>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _equals_Object_Caller : public muscle::CallerAttributes<jboolean>
{
	public: _equals_Object_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jboolean>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_equals_Object(env, isStatic))
	{
		assert(isStatic == false);
	}
	jboolean call(jobject A0)
	{
		return muscle::CallerAttributes<jboolean>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: jstring toString()
{
	java_util_logging_Logger::_toString_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _toString()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("java/util/logging/Logger", "toString", false, "Ljava/lang/String;", argTypes);
}
public: static jmethodID _toString(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _toString().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jstring> > _toString(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _toString(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jstring> >( new muscle::MethodCaller<jstring>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _toString_Caller : public muscle::CallerAttributes<jstring>
{
	public: _toString_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jstring>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_toString(env, isStatic))
	{
		assert(isStatic == false);
	}
	jstring call()
	{
		return muscle::CallerAttributes<jstring>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void notify()
{
	java_util_logging_Logger::_notify_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _notify()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("java/util/logging/Logger", "notify", false, "", argTypes);
}
public: static jmethodID _notify(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _notify().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _notify(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _notify(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _notify_Caller : public muscle::CallerAttributes<void>
{
	public: _notify_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_notify(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call()
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void notifyAll()
{
	java_util_logging_Logger::_notifyAll_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _notifyAll()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("java/util/logging/Logger", "notifyAll", false, "", argTypes);
}
public: static jmethodID _notifyAll(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _notifyAll().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _notifyAll(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _notifyAll(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type java.util.logging.Logger
do not instantiate this class manually, better use a java_util_logging_Logger objectand call this function directly
\author Jan Hegewald
*/
class _notifyAll_Caller : public muscle::CallerAttributes<void>
{
	public: _notifyAll_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, java_util_logging_Logger::_CLASSNAME(), java_util_logging_Logger::_notifyAll(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call()
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
};
/**
cpp counterpart class to access the java class muscle.core.JNIConduitEntrance
function names prefixed with an underscore _ are cpp only utility functions which do not exist in the java class
\author Jan Hegewald
*/
class muscle_core_JNIConduitEntrance
{
/**
function which returns the classname as a cpp jni signature (e.g. java_lang_String)
*/
	public: static std::string _CLASSNAME()
	{
		return "muscle/core/JNIConduitEntrance";
	}
	public: muscle_core_JNIConduitEntrance(JNIEnv* newEnv, jobject newDelegate)
	: jenv(newEnv), delegate(newDelegate)
{
	// init jvm pointer
	jint result = newEnv->GetJavaVM(&jvm);
	if(result < 0)
		throw std::runtime_error("error obtaining JVM");
	// see if we got the right jobject
	muscle::JNITool::assertInstanceOf(newEnv, newDelegate, _CLASSNAME(), __FILE__, __LINE__);
}
private: JNIEnv* jenv;
private: JavaVM* jvm;
private: jobject delegate;
/**
returns a reference to the JNIEnv pointer belonging to the current thread
*/
public: JNIEnv*& _GETJNIENV()
{
	jint result = jvm->AttachCurrentThread((void **)&jenv, NULL);
	if(result != JNI_OK)
		throw std::runtime_error("error obtaining Java env");
	return jenv;
}
//
public: jobject toJavaJNIMethod()
{
	muscle_core_JNIConduitEntrance::_toJavaJNIMethod_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _toJavaJNIMethod()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/JNIConduitEntrance", "toJavaJNIMethod", false, "Lutilities/jni/JNIMethod;", argTypes);
}
public: static jmethodID _toJavaJNIMethod(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _toJavaJNIMethod().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _toJavaJNIMethod(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _toJavaJNIMethod(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitEntrance
do not instantiate this class manually, better use a muscle_core_JNIConduitEntrance objectand call this function directly
\author Jan Hegewald
*/
class _toJavaJNIMethod_Caller : public muscle::CallerAttributes<jobject>
{
	public: _toJavaJNIMethod_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, muscle_core_JNIConduitEntrance::_CLASSNAME(), muscle_core_JNIConduitEntrance::_toJavaJNIMethod(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call()
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void toJava_Object(jobject A0)
{
	muscle_core_JNIConduitEntrance::_toJava_Object_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _toJava_Object()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/lang/Object;" );
return muscle::NativeAccessMethod("muscle/core/JNIConduitEntrance", "toJava", false, "", argTypes);
}
public: static jmethodID _toJava_Object(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _toJava_Object().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _toJava_Object(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _toJava_Object(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitEntrance
do not instantiate this class manually, better use a muscle_core_JNIConduitEntrance objectand call this function directly
\author Jan Hegewald
*/
class _toJava_Object_Caller : public muscle::CallerAttributes<void>
{
	public: _toJava_Object_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_JNIConduitEntrance::_CLASSNAME(), muscle_core_JNIConduitEntrance::_toJava_Object(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: void send_Object(jobject A0)
{
	muscle_core_JNIConduitEntrance::_send_Object_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _send_Object()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/lang/Object;" );
return muscle::NativeAccessMethod("muscle/core/JNIConduitEntrance", "send", false, "", argTypes);
}
public: static jmethodID _send_Object(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _send_Object().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _send_Object(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _send_Object(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitEntrance
do not instantiate this class manually, better use a muscle_core_JNIConduitEntrance objectand call this function directly
\author Jan Hegewald
*/
class _send_Object_Caller : public muscle::CallerAttributes<void>
{
	public: _send_Object_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_JNIConduitEntrance::_CLASSNAME(), muscle_core_JNIConduitEntrance::_send_Object(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: void detachConduit_Agent(jobject A0)
{
	muscle_core_JNIConduitEntrance::_detachConduit_Agent_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _detachConduit_Agent()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljade/core/Agent;" );
return muscle::NativeAccessMethod("muscle/core/JNIConduitEntrance", "detachConduit", false, "", argTypes);
}
public: static jmethodID _detachConduit_Agent(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _detachConduit_Agent().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _detachConduit_Agent(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _detachConduit_Agent(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitEntrance
do not instantiate this class manually, better use a muscle_core_JNIConduitEntrance objectand call this function directly
\author Jan Hegewald
*/
class _detachConduit_Agent_Caller : public muscle::CallerAttributes<void>
{
	public: _detachConduit_Agent_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_JNIConduitEntrance::_CLASSNAME(), muscle_core_JNIConduitEntrance::_detachConduit_Agent(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: jobjectArray getDependencies()
{
	muscle_core_JNIConduitEntrance::_getDependencies_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getDependencies()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/JNIConduitEntrance", "getDependencies", false, "[Lmuscle/core/EntranceDependency;", argTypes);
}
public: static jmethodID _getDependencies(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getDependencies().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobjectArray> > _getDependencies(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getDependencies(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobjectArray> >( new muscle::MethodCaller<jobjectArray>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitEntrance
do not instantiate this class manually, better use a muscle_core_JNIConduitEntrance objectand call this function directly
\author Jan Hegewald
*/
class _getDependencies_Caller : public muscle::CallerAttributes<jobjectArray>
{
	public: _getDependencies_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobjectArray>(env, obj, muscle_core_JNIConduitEntrance::_CLASSNAME(), muscle_core_JNIConduitEntrance::_getDependencies(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobjectArray call()
	{
		return muscle::CallerAttributes<jobjectArray>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void setConduitID_AID(jobject A0)
{
	muscle_core_JNIConduitEntrance::_setConduitID_AID_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _setConduitID_AID()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljade/core/AID;" );
return muscle::NativeAccessMethod("muscle/core/JNIConduitEntrance", "setConduitID", false, "", argTypes);
}
public: static jmethodID _setConduitID_AID(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _setConduitID_AID().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _setConduitID_AID(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _setConduitID_AID(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitEntrance
do not instantiate this class manually, better use a muscle_core_JNIConduitEntrance objectand call this function directly
\author Jan Hegewald
*/
class _setConduitID_AID_Caller : public muscle::CallerAttributes<void>
{
	public: _setConduitID_AID_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_JNIConduitEntrance::_CLASSNAME(), muscle_core_JNIConduitEntrance::_setConduitID_AID(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: jboolean equals_Object(jobject A0)
{
	muscle_core_JNIConduitEntrance::_equals_Object_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _equals_Object()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/lang/Object;" );
return muscle::NativeAccessMethod("muscle/core/JNIConduitEntrance", "equals", false, "Z", argTypes);
}
public: static jmethodID _equals_Object(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _equals_Object().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jboolean> > _equals_Object(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _equals_Object(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jboolean> >( new muscle::MethodCaller<jboolean>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitEntrance
do not instantiate this class manually, better use a muscle_core_JNIConduitEntrance objectand call this function directly
\author Jan Hegewald
*/
class _equals_Object_Caller : public muscle::CallerAttributes<jboolean>
{
	public: _equals_Object_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jboolean>(env, obj, muscle_core_JNIConduitEntrance::_CLASSNAME(), muscle_core_JNIConduitEntrance::_equals_Object(env, isStatic))
	{
		assert(isStatic == false);
	}
	jboolean call(jobject A0)
	{
		return muscle::CallerAttributes<jboolean>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: jstring toString()
{
	muscle_core_JNIConduitEntrance::_toString_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _toString()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/JNIConduitEntrance", "toString", false, "Ljava/lang/String;", argTypes);
}
public: static jmethodID _toString(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _toString().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jstring> > _toString(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _toString(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jstring> >( new muscle::MethodCaller<jstring>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitEntrance
do not instantiate this class manually, better use a muscle_core_JNIConduitEntrance objectand call this function directly
\author Jan Hegewald
*/
class _toString_Caller : public muscle::CallerAttributes<jstring>
{
	public: _toString_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jstring>(env, obj, muscle_core_JNIConduitEntrance::_CLASSNAME(), muscle_core_JNIConduitEntrance::_toString(env, isStatic))
	{
		assert(isStatic == false);
	}
	jstring call()
	{
		return muscle::CallerAttributes<jstring>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void detachOwnerAgent()
{
	muscle_core_JNIConduitEntrance::_detachOwnerAgent_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _detachOwnerAgent()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/JNIConduitEntrance", "detachOwnerAgent", false, "", argTypes);
}
public: static jmethodID _detachOwnerAgent(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _detachOwnerAgent().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _detachOwnerAgent(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _detachOwnerAgent(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitEntrance
do not instantiate this class manually, better use a muscle_core_JNIConduitEntrance objectand call this function directly
\author Jan Hegewald
*/
class _detachOwnerAgent_Caller : public muscle::CallerAttributes<void>
{
	public: _detachOwnerAgent_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_JNIConduitEntrance::_CLASSNAME(), muscle_core_JNIConduitEntrance::_detachOwnerAgent(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call()
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jobject getSITime()
{
	muscle_core_JNIConduitEntrance::_getSITime_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getSITime()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/JNIConduitEntrance", "getSITime", false, "Ljavax/measure/DecimalMeasure;", argTypes);
}
public: static jmethodID _getSITime(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getSITime().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _getSITime(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getSITime(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitEntrance
do not instantiate this class manually, better use a muscle_core_JNIConduitEntrance objectand call this function directly
\author Jan Hegewald
*/
class _getSITime_Caller : public muscle::CallerAttributes<jobject>
{
	public: _getSITime_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, muscle_core_JNIConduitEntrance::_CLASSNAME(), muscle_core_JNIConduitEntrance::_getSITime(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call()
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jstring getLocalName()
{
	muscle_core_JNIConduitEntrance::_getLocalName_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getLocalName()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/JNIConduitEntrance", "getLocalName", false, "Ljava/lang/String;", argTypes);
}
public: static jmethodID _getLocalName(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getLocalName().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jstring> > _getLocalName(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getLocalName(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jstring> >( new muscle::MethodCaller<jstring>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitEntrance
do not instantiate this class manually, better use a muscle_core_JNIConduitEntrance objectand call this function directly
\author Jan Hegewald
*/
class _getLocalName_Caller : public muscle::CallerAttributes<jstring>
{
	public: _getLocalName_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jstring>(env, obj, muscle_core_JNIConduitEntrance::_CLASSNAME(), muscle_core_JNIConduitEntrance::_getLocalName(env, isStatic))
	{
		assert(isStatic == false);
	}
	jstring call()
	{
		return muscle::CallerAttributes<jstring>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jobject getDataTemplate()
{
	muscle_core_JNIConduitEntrance::_getDataTemplate_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getDataTemplate()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/JNIConduitEntrance", "getDataTemplate", false, "Lmuscle/core/DataTemplate;", argTypes);
}
public: static jmethodID _getDataTemplate(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getDataTemplate().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _getDataTemplate(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getDataTemplate(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitEntrance
do not instantiate this class manually, better use a muscle_core_JNIConduitEntrance objectand call this function directly
\author Jan Hegewald
*/
class _getDataTemplate_Caller : public muscle::CallerAttributes<jobject>
{
	public: _getDataTemplate_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, muscle_core_JNIConduitEntrance::_CLASSNAME(), muscle_core_JNIConduitEntrance::_getDataTemplate(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call()
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void oneShot()
{
	muscle_core_JNIConduitEntrance::_oneShot_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _oneShot()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/JNIConduitEntrance", "oneShot", false, "", argTypes);
}
public: static jmethodID _oneShot(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _oneShot().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _oneShot(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _oneShot(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitEntrance
do not instantiate this class manually, better use a muscle_core_JNIConduitEntrance objectand call this function directly
\author Jan Hegewald
*/
class _oneShot_Caller : public muscle::CallerAttributes<void>
{
	public: _oneShot_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_JNIConduitEntrance::_CLASSNAME(), muscle_core_JNIConduitEntrance::_oneShot(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call()
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void setOwner_CAController(jobject A0)
{
	muscle_core_JNIConduitEntrance::_setOwner_CAController_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _setOwner_CAController()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Lmuscle/core/kernel/CAController;" );
return muscle::NativeAccessMethod("muscle/core/JNIConduitEntrance", "setOwner", false, "", argTypes);
}
public: static jmethodID _setOwner_CAController(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _setOwner_CAController().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _setOwner_CAController(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _setOwner_CAController(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitEntrance
do not instantiate this class manually, better use a muscle_core_JNIConduitEntrance objectand call this function directly
\author Jan Hegewald
*/
class _setOwner_CAController_Caller : public muscle::CallerAttributes<void>
{
	public: _setOwner_CAController_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_JNIConduitEntrance::_CLASSNAME(), muscle_core_JNIConduitEntrance::_setOwner_CAController(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: void setTraceOutputStream_OutputStream(jobject A0)
{
	muscle_core_JNIConduitEntrance::_setTraceOutputStream_OutputStream_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _setTraceOutputStream_OutputStream()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/io/OutputStream;" );
return muscle::NativeAccessMethod("muscle/core/JNIConduitEntrance", "setTraceOutputStream", false, "", argTypes);
}
public: static jmethodID _setTraceOutputStream_OutputStream(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _setTraceOutputStream_OutputStream().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _setTraceOutputStream_OutputStream(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _setTraceOutputStream_OutputStream(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitEntrance
do not instantiate this class manually, better use a muscle_core_JNIConduitEntrance objectand call this function directly
\author Jan Hegewald
*/
class _setTraceOutputStream_OutputStream_Caller : public muscle::CallerAttributes<void>
{
	public: _setTraceOutputStream_OutputStream_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_JNIConduitEntrance::_CLASSNAME(), muscle_core_JNIConduitEntrance::_setTraceOutputStream_OutputStream(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: void trace_String(jstring A0)
{
	muscle_core_JNIConduitEntrance::_trace_String_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _trace_String()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/lang/String;" );
return muscle::NativeAccessMethod("muscle/core/JNIConduitEntrance", "trace", false, "", argTypes);
}
public: static jmethodID _trace_String(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _trace_String().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _trace_String(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _trace_String(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitEntrance
do not instantiate this class manually, better use a muscle_core_JNIConduitEntrance objectand call this function directly
\author Jan Hegewald
*/
class _trace_String_Caller : public muscle::CallerAttributes<void>
{
	public: _trace_String_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_JNIConduitEntrance::_CLASSNAME(), muscle_core_JNIConduitEntrance::_trace_String(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jstring A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: jobject getPortalID()
{
	muscle_core_JNIConduitEntrance::_getPortalID_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getPortalID()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/JNIConduitEntrance", "getPortalID", false, "Lmuscle/core/PortalID;", argTypes);
}
public: static jmethodID _getPortalID(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getPortalID().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _getPortalID(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getPortalID(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitEntrance
do not instantiate this class manually, better use a muscle_core_JNIConduitEntrance objectand call this function directly
\author Jan Hegewald
*/
class _getPortalID_Caller : public muscle::CallerAttributes<jobject>
{
	public: _getPortalID_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, muscle_core_JNIConduitEntrance::_CLASSNAME(), muscle_core_JNIConduitEntrance::_getPortalID(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call()
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jboolean isLoose()
{
	muscle_core_JNIConduitEntrance::_isLoose_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _isLoose()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/JNIConduitEntrance", "isLoose", false, "Z", argTypes);
}
public: static jmethodID _isLoose(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _isLoose().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jboolean> > _isLoose(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _isLoose(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jboolean> >( new muscle::MethodCaller<jboolean>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitEntrance
do not instantiate this class manually, better use a muscle_core_JNIConduitEntrance objectand call this function directly
\author Jan Hegewald
*/
class _isLoose_Caller : public muscle::CallerAttributes<jboolean>
{
	public: _isLoose_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jboolean>(env, obj, muscle_core_JNIConduitEntrance::_CLASSNAME(), muscle_core_JNIConduitEntrance::_isLoose(env, isStatic))
	{
		assert(isStatic == false);
	}
	jboolean call()
	{
		return muscle::CallerAttributes<jboolean>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void wait_long_int(jlong A0, jint A1)
{
	muscle_core_JNIConduitEntrance::_wait_long_int_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0, A1);
}
public: static muscle::NativeAccessMethod _wait_long_int()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "J" );
argTypes.push_back( "I" );
return muscle::NativeAccessMethod("muscle/core/JNIConduitEntrance", "wait", false, "", argTypes);
}
public: static jmethodID _wait_long_int(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _wait_long_int().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _wait_long_int(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _wait_long_int(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitEntrance
do not instantiate this class manually, better use a muscle_core_JNIConduitEntrance objectand call this function directly
\author Jan Hegewald
*/
class _wait_long_int_Caller : public muscle::CallerAttributes<void>
{
	public: _wait_long_int_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_JNIConduitEntrance::_CLASSNAME(), muscle_core_JNIConduitEntrance::_wait_long_int(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jlong A0, jint A1)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0, A1);
	}
	private: bool isStatic;
};
//
public: void wait()
{
	muscle_core_JNIConduitEntrance::_wait_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _wait()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/JNIConduitEntrance", "wait", false, "", argTypes);
}
public: static jmethodID _wait(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _wait().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _wait(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _wait(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitEntrance
do not instantiate this class manually, better use a muscle_core_JNIConduitEntrance objectand call this function directly
\author Jan Hegewald
*/
class _wait_Caller : public muscle::CallerAttributes<void>
{
	public: _wait_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_JNIConduitEntrance::_CLASSNAME(), muscle_core_JNIConduitEntrance::_wait(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call()
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void wait_long(jlong A0)
{
	muscle_core_JNIConduitEntrance::_wait_long_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _wait_long()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "J" );
return muscle::NativeAccessMethod("muscle/core/JNIConduitEntrance", "wait", false, "", argTypes);
}
public: static jmethodID _wait_long(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _wait_long().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _wait_long(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _wait_long(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitEntrance
do not instantiate this class manually, better use a muscle_core_JNIConduitEntrance objectand call this function directly
\author Jan Hegewald
*/
class _wait_long_Caller : public muscle::CallerAttributes<void>
{
	public: _wait_long_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_JNIConduitEntrance::_CLASSNAME(), muscle_core_JNIConduitEntrance::_wait_long(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jlong A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: jint hashCode()
{
	muscle_core_JNIConduitEntrance::_hashCode_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _hashCode()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/JNIConduitEntrance", "hashCode", false, "I", argTypes);
}
public: static jmethodID _hashCode(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _hashCode().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jint> > _hashCode(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _hashCode(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jint> >( new muscle::MethodCaller<jint>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitEntrance
do not instantiate this class manually, better use a muscle_core_JNIConduitEntrance objectand call this function directly
\author Jan Hegewald
*/
class _hashCode_Caller : public muscle::CallerAttributes<jint>
{
	public: _hashCode_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jint>(env, obj, muscle_core_JNIConduitEntrance::_CLASSNAME(), muscle_core_JNIConduitEntrance::_hashCode(env, isStatic))
	{
		assert(isStatic == false);
	}
	jint call()
	{
		return muscle::CallerAttributes<jint>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jclass getClass()
{
	muscle_core_JNIConduitEntrance::_getClass_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getClass()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/JNIConduitEntrance", "getClass", false, "Ljava/lang/Class;", argTypes);
}
public: static jmethodID _getClass(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getClass().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jclass> > _getClass(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getClass(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jclass> >( new muscle::MethodCaller<jclass>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitEntrance
do not instantiate this class manually, better use a muscle_core_JNIConduitEntrance objectand call this function directly
\author Jan Hegewald
*/
class _getClass_Caller : public muscle::CallerAttributes<jclass>
{
	public: _getClass_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jclass>(env, obj, muscle_core_JNIConduitEntrance::_CLASSNAME(), muscle_core_JNIConduitEntrance::_getClass(env, isStatic))
	{
		assert(isStatic == false);
	}
	jclass call()
	{
		return muscle::CallerAttributes<jclass>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void notify()
{
	muscle_core_JNIConduitEntrance::_notify_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _notify()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/JNIConduitEntrance", "notify", false, "", argTypes);
}
public: static jmethodID _notify(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _notify().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _notify(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _notify(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitEntrance
do not instantiate this class manually, better use a muscle_core_JNIConduitEntrance objectand call this function directly
\author Jan Hegewald
*/
class _notify_Caller : public muscle::CallerAttributes<void>
{
	public: _notify_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_JNIConduitEntrance::_CLASSNAME(), muscle_core_JNIConduitEntrance::_notify(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call()
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void notifyAll()
{
	muscle_core_JNIConduitEntrance::_notifyAll_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _notifyAll()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/JNIConduitEntrance", "notifyAll", false, "", argTypes);
}
public: static jmethodID _notifyAll(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _notifyAll().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _notifyAll(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _notifyAll(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitEntrance
do not instantiate this class manually, better use a muscle_core_JNIConduitEntrance objectand call this function directly
\author Jan Hegewald
*/
class _notifyAll_Caller : public muscle::CallerAttributes<void>
{
	public: _notifyAll_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_JNIConduitEntrance::_CLASSNAME(), muscle_core_JNIConduitEntrance::_notifyAll(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call()
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
};
/**
cpp counterpart class to access the java class muscle.core.JNIConduitExit
function names prefixed with an underscore _ are cpp only utility functions which do not exist in the java class
\author Jan Hegewald
*/
class muscle_core_JNIConduitExit
{
/**
function which returns the classname as a cpp jni signature (e.g. java_lang_String)
*/
	public: static std::string _CLASSNAME()
	{
		return "muscle/core/JNIConduitExit";
	}
	public: muscle_core_JNIConduitExit(JNIEnv* newEnv, jobject newDelegate)
	: jenv(newEnv), delegate(newDelegate)
{
	// init jvm pointer
	jint result = newEnv->GetJavaVM(&jvm);
	if(result < 0)
		throw std::runtime_error("error obtaining JVM");
	// see if we got the right jobject
	muscle::JNITool::assertInstanceOf(newEnv, newDelegate, _CLASSNAME(), __FILE__, __LINE__);
}
private: JNIEnv* jenv;
private: JavaVM* jvm;
private: jobject delegate;
/**
returns a reference to the JNIEnv pointer belonging to the current thread
*/
public: JNIEnv*& _GETJNIENV()
{
	jint result = jvm->AttachCurrentThread((void **)&jenv, NULL);
	if(result != JNI_OK)
		throw std::runtime_error("error obtaining Java env");
	return jenv;
}
//
public: jobject fromJavaJNIMethod()
{
	muscle_core_JNIConduitExit::_fromJavaJNIMethod_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _fromJavaJNIMethod()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/JNIConduitExit", "fromJavaJNIMethod", false, "Lutilities/jni/JNIMethod;", argTypes);
}
public: static jmethodID _fromJavaJNIMethod(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _fromJavaJNIMethod().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _fromJavaJNIMethod(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _fromJavaJNIMethod(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitExit
do not instantiate this class manually, better use a muscle_core_JNIConduitExit objectand call this function directly
\author Jan Hegewald
*/
class _fromJavaJNIMethod_Caller : public muscle::CallerAttributes<jobject>
{
	public: _fromJavaJNIMethod_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, muscle_core_JNIConduitExit::_CLASSNAME(), muscle_core_JNIConduitExit::_fromJavaJNIMethod(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call()
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jobject fromJava()
{
	muscle_core_JNIConduitExit::_fromJava_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _fromJava()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/JNIConduitExit", "fromJava", false, "Ljava/lang/Object;", argTypes);
}
public: static jmethodID _fromJava(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _fromJava().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _fromJava(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _fromJava(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitExit
do not instantiate this class manually, better use a muscle_core_JNIConduitExit objectand call this function directly
\author Jan Hegewald
*/
class _fromJava_Caller : public muscle::CallerAttributes<jobject>
{
	public: _fromJava_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, muscle_core_JNIConduitExit::_CLASSNAME(), muscle_core_JNIConduitExit::_fromJava(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call()
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jobject getMessage()
{
	muscle_core_JNIConduitExit::_getMessage_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getMessage()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/JNIConduitExit", "getMessage", false, "Ljade/lang/acl/ACLMessage;", argTypes);
}
public: static jmethodID _getMessage(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getMessage().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _getMessage(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getMessage(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitExit
do not instantiate this class manually, better use a muscle_core_JNIConduitExit objectand call this function directly
\author Jan Hegewald
*/
class _getMessage_Caller : public muscle::CallerAttributes<jobject>
{
	public: _getMessage_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, muscle_core_JNIConduitExit::_CLASSNAME(), muscle_core_JNIConduitExit::_getMessage(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call()
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jobject getData_CAController(jobject A0)
{
	muscle_core_JNIConduitExit::_getData_CAController_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _getData_CAController()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Lmuscle/core/kernel/CAController;" );
return muscle::NativeAccessMethod("muscle/core/JNIConduitExit", "getData", false, "Lmuscle/core/wrapper/DataWrapper;", argTypes);
}
public: static jmethodID _getData_CAController(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getData_CAController().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _getData_CAController(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getData_CAController(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitExit
do not instantiate this class manually, better use a muscle_core_JNIConduitExit objectand call this function directly
\author Jan Hegewald
*/
class _getData_CAController_Caller : public muscle::CallerAttributes<jobject>
{
	public: _getData_CAController_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, muscle_core_JNIConduitExit::_CLASSNAME(), muscle_core_JNIConduitExit::_getData_CAController(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call(jobject A0)
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: void addMessage_ACLMessage(jobject A0)
{
	muscle_core_JNIConduitExit::_addMessage_ACLMessage_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _addMessage_ACLMessage()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljade/lang/acl/ACLMessage;" );
return muscle::NativeAccessMethod("muscle/core/JNIConduitExit", "addMessage", false, "", argTypes);
}
public: static jmethodID _addMessage_ACLMessage(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _addMessage_ACLMessage().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _addMessage_ACLMessage(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _addMessage_ACLMessage(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitExit
do not instantiate this class manually, better use a muscle_core_JNIConduitExit objectand call this function directly
\author Jan Hegewald
*/
class _addMessage_ACLMessage_Caller : public muscle::CallerAttributes<void>
{
	public: _addMessage_ACLMessage_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_JNIConduitExit::_CLASSNAME(), muscle_core_JNIConduitExit::_addMessage_ACLMessage(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: jobject receive()
{
	muscle_core_JNIConduitExit::_receive_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _receive()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/JNIConduitExit", "receive", false, "Ljava/lang/Object;", argTypes);
}
public: static jmethodID _receive(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _receive().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _receive(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _receive(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitExit
do not instantiate this class manually, better use a muscle_core_JNIConduitExit objectand call this function directly
\author Jan Hegewald
*/
class _receive_Caller : public muscle::CallerAttributes<jobject>
{
	public: _receive_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, muscle_core_JNIConduitExit::_CLASSNAME(), muscle_core_JNIConduitExit::_receive(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call()
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jobject getTemplate()
{
	muscle_core_JNIConduitExit::_getTemplate_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getTemplate()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/JNIConduitExit", "getTemplate", false, "Ljade/lang/acl/MessageTemplate;", argTypes);
}
public: static jmethodID _getTemplate(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getTemplate().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _getTemplate(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getTemplate(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitExit
do not instantiate this class manually, better use a muscle_core_JNIConduitExit objectand call this function directly
\author Jan Hegewald
*/
class _getTemplate_Caller : public muscle::CallerAttributes<jobject>
{
	public: _getTemplate_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, muscle_core_JNIConduitExit::_CLASSNAME(), muscle_core_JNIConduitExit::_getTemplate(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call()
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jboolean equals_Object(jobject A0)
{
	muscle_core_JNIConduitExit::_equals_Object_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _equals_Object()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/lang/Object;" );
return muscle::NativeAccessMethod("muscle/core/JNIConduitExit", "equals", false, "Z", argTypes);
}
public: static jmethodID _equals_Object(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _equals_Object().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jboolean> > _equals_Object(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _equals_Object(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jboolean> >( new muscle::MethodCaller<jboolean>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitExit
do not instantiate this class manually, better use a muscle_core_JNIConduitExit objectand call this function directly
\author Jan Hegewald
*/
class _equals_Object_Caller : public muscle::CallerAttributes<jboolean>
{
	public: _equals_Object_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jboolean>(env, obj, muscle_core_JNIConduitExit::_CLASSNAME(), muscle_core_JNIConduitExit::_equals_Object(env, isStatic))
	{
		assert(isStatic == false);
	}
	jboolean call(jobject A0)
	{
		return muscle::CallerAttributes<jboolean>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: jstring toString()
{
	muscle_core_JNIConduitExit::_toString_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _toString()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/JNIConduitExit", "toString", false, "Ljava/lang/String;", argTypes);
}
public: static jmethodID _toString(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _toString().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jstring> > _toString(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _toString(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jstring> >( new muscle::MethodCaller<jstring>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitExit
do not instantiate this class manually, better use a muscle_core_JNIConduitExit objectand call this function directly
\author Jan Hegewald
*/
class _toString_Caller : public muscle::CallerAttributes<jstring>
{
	public: _toString_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jstring>(env, obj, muscle_core_JNIConduitExit::_CLASSNAME(), muscle_core_JNIConduitExit::_toString(env, isStatic))
	{
		assert(isStatic == false);
	}
	jstring call()
	{
		return muscle::CallerAttributes<jstring>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void detachOwnerAgent()
{
	muscle_core_JNIConduitExit::_detachOwnerAgent_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _detachOwnerAgent()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/JNIConduitExit", "detachOwnerAgent", false, "", argTypes);
}
public: static jmethodID _detachOwnerAgent(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _detachOwnerAgent().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _detachOwnerAgent(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _detachOwnerAgent(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitExit
do not instantiate this class manually, better use a muscle_core_JNIConduitExit objectand call this function directly
\author Jan Hegewald
*/
class _detachOwnerAgent_Caller : public muscle::CallerAttributes<void>
{
	public: _detachOwnerAgent_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_JNIConduitExit::_CLASSNAME(), muscle_core_JNIConduitExit::_detachOwnerAgent(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call()
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jobject getSITime()
{
	muscle_core_JNIConduitExit::_getSITime_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getSITime()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/JNIConduitExit", "getSITime", false, "Ljavax/measure/DecimalMeasure;", argTypes);
}
public: static jmethodID _getSITime(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getSITime().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _getSITime(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getSITime(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitExit
do not instantiate this class manually, better use a muscle_core_JNIConduitExit objectand call this function directly
\author Jan Hegewald
*/
class _getSITime_Caller : public muscle::CallerAttributes<jobject>
{
	public: _getSITime_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, muscle_core_JNIConduitExit::_CLASSNAME(), muscle_core_JNIConduitExit::_getSITime(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call()
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jstring getLocalName()
{
	muscle_core_JNIConduitExit::_getLocalName_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getLocalName()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/JNIConduitExit", "getLocalName", false, "Ljava/lang/String;", argTypes);
}
public: static jmethodID _getLocalName(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getLocalName().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jstring> > _getLocalName(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getLocalName(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jstring> >( new muscle::MethodCaller<jstring>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitExit
do not instantiate this class manually, better use a muscle_core_JNIConduitExit objectand call this function directly
\author Jan Hegewald
*/
class _getLocalName_Caller : public muscle::CallerAttributes<jstring>
{
	public: _getLocalName_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jstring>(env, obj, muscle_core_JNIConduitExit::_CLASSNAME(), muscle_core_JNIConduitExit::_getLocalName(env, isStatic))
	{
		assert(isStatic == false);
	}
	jstring call()
	{
		return muscle::CallerAttributes<jstring>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jobject getDataTemplate()
{
	muscle_core_JNIConduitExit::_getDataTemplate_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getDataTemplate()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/JNIConduitExit", "getDataTemplate", false, "Lmuscle/core/DataTemplate;", argTypes);
}
public: static jmethodID _getDataTemplate(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getDataTemplate().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _getDataTemplate(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getDataTemplate(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitExit
do not instantiate this class manually, better use a muscle_core_JNIConduitExit objectand call this function directly
\author Jan Hegewald
*/
class _getDataTemplate_Caller : public muscle::CallerAttributes<jobject>
{
	public: _getDataTemplate_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, muscle_core_JNIConduitExit::_CLASSNAME(), muscle_core_JNIConduitExit::_getDataTemplate(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call()
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void oneShot()
{
	muscle_core_JNIConduitExit::_oneShot_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _oneShot()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/JNIConduitExit", "oneShot", false, "", argTypes);
}
public: static jmethodID _oneShot(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _oneShot().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _oneShot(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _oneShot(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitExit
do not instantiate this class manually, better use a muscle_core_JNIConduitExit objectand call this function directly
\author Jan Hegewald
*/
class _oneShot_Caller : public muscle::CallerAttributes<void>
{
	public: _oneShot_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_JNIConduitExit::_CLASSNAME(), muscle_core_JNIConduitExit::_oneShot(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call()
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void setOwner_CAController(jobject A0)
{
	muscle_core_JNIConduitExit::_setOwner_CAController_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _setOwner_CAController()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Lmuscle/core/kernel/CAController;" );
return muscle::NativeAccessMethod("muscle/core/JNIConduitExit", "setOwner", false, "", argTypes);
}
public: static jmethodID _setOwner_CAController(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _setOwner_CAController().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _setOwner_CAController(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _setOwner_CAController(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitExit
do not instantiate this class manually, better use a muscle_core_JNIConduitExit objectand call this function directly
\author Jan Hegewald
*/
class _setOwner_CAController_Caller : public muscle::CallerAttributes<void>
{
	public: _setOwner_CAController_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_JNIConduitExit::_CLASSNAME(), muscle_core_JNIConduitExit::_setOwner_CAController(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: void setTraceOutputStream_OutputStream(jobject A0)
{
	muscle_core_JNIConduitExit::_setTraceOutputStream_OutputStream_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _setTraceOutputStream_OutputStream()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/io/OutputStream;" );
return muscle::NativeAccessMethod("muscle/core/JNIConduitExit", "setTraceOutputStream", false, "", argTypes);
}
public: static jmethodID _setTraceOutputStream_OutputStream(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _setTraceOutputStream_OutputStream().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _setTraceOutputStream_OutputStream(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _setTraceOutputStream_OutputStream(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitExit
do not instantiate this class manually, better use a muscle_core_JNIConduitExit objectand call this function directly
\author Jan Hegewald
*/
class _setTraceOutputStream_OutputStream_Caller : public muscle::CallerAttributes<void>
{
	public: _setTraceOutputStream_OutputStream_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_JNIConduitExit::_CLASSNAME(), muscle_core_JNIConduitExit::_setTraceOutputStream_OutputStream(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jobject A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: void trace_String(jstring A0)
{
	muscle_core_JNIConduitExit::_trace_String_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _trace_String()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/lang/String;" );
return muscle::NativeAccessMethod("muscle/core/JNIConduitExit", "trace", false, "", argTypes);
}
public: static jmethodID _trace_String(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _trace_String().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _trace_String(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _trace_String(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitExit
do not instantiate this class manually, better use a muscle_core_JNIConduitExit objectand call this function directly
\author Jan Hegewald
*/
class _trace_String_Caller : public muscle::CallerAttributes<void>
{
	public: _trace_String_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_JNIConduitExit::_CLASSNAME(), muscle_core_JNIConduitExit::_trace_String(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jstring A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: jobject getPortalID()
{
	muscle_core_JNIConduitExit::_getPortalID_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getPortalID()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/JNIConduitExit", "getPortalID", false, "Lmuscle/core/PortalID;", argTypes);
}
public: static jmethodID _getPortalID(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getPortalID().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _getPortalID(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getPortalID(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitExit
do not instantiate this class manually, better use a muscle_core_JNIConduitExit objectand call this function directly
\author Jan Hegewald
*/
class _getPortalID_Caller : public muscle::CallerAttributes<jobject>
{
	public: _getPortalID_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, muscle_core_JNIConduitExit::_CLASSNAME(), muscle_core_JNIConduitExit::_getPortalID(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call()
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jboolean isLoose()
{
	muscle_core_JNIConduitExit::_isLoose_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _isLoose()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/JNIConduitExit", "isLoose", false, "Z", argTypes);
}
public: static jmethodID _isLoose(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _isLoose().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jboolean> > _isLoose(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _isLoose(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jboolean> >( new muscle::MethodCaller<jboolean>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitExit
do not instantiate this class manually, better use a muscle_core_JNIConduitExit objectand call this function directly
\author Jan Hegewald
*/
class _isLoose_Caller : public muscle::CallerAttributes<jboolean>
{
	public: _isLoose_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jboolean>(env, obj, muscle_core_JNIConduitExit::_CLASSNAME(), muscle_core_JNIConduitExit::_isLoose(env, isStatic))
	{
		assert(isStatic == false);
	}
	jboolean call()
	{
		return muscle::CallerAttributes<jboolean>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void wait_long_int(jlong A0, jint A1)
{
	muscle_core_JNIConduitExit::_wait_long_int_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0, A1);
}
public: static muscle::NativeAccessMethod _wait_long_int()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "J" );
argTypes.push_back( "I" );
return muscle::NativeAccessMethod("muscle/core/JNIConduitExit", "wait", false, "", argTypes);
}
public: static jmethodID _wait_long_int(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _wait_long_int().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _wait_long_int(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _wait_long_int(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitExit
do not instantiate this class manually, better use a muscle_core_JNIConduitExit objectand call this function directly
\author Jan Hegewald
*/
class _wait_long_int_Caller : public muscle::CallerAttributes<void>
{
	public: _wait_long_int_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_JNIConduitExit::_CLASSNAME(), muscle_core_JNIConduitExit::_wait_long_int(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jlong A0, jint A1)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0, A1);
	}
	private: bool isStatic;
};
//
public: void wait()
{
	muscle_core_JNIConduitExit::_wait_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _wait()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/JNIConduitExit", "wait", false, "", argTypes);
}
public: static jmethodID _wait(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _wait().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _wait(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _wait(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitExit
do not instantiate this class manually, better use a muscle_core_JNIConduitExit objectand call this function directly
\author Jan Hegewald
*/
class _wait_Caller : public muscle::CallerAttributes<void>
{
	public: _wait_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_JNIConduitExit::_CLASSNAME(), muscle_core_JNIConduitExit::_wait(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call()
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void wait_long(jlong A0)
{
	muscle_core_JNIConduitExit::_wait_long_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _wait_long()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "J" );
return muscle::NativeAccessMethod("muscle/core/JNIConduitExit", "wait", false, "", argTypes);
}
public: static jmethodID _wait_long(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _wait_long().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _wait_long(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _wait_long(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitExit
do not instantiate this class manually, better use a muscle_core_JNIConduitExit objectand call this function directly
\author Jan Hegewald
*/
class _wait_long_Caller : public muscle::CallerAttributes<void>
{
	public: _wait_long_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_JNIConduitExit::_CLASSNAME(), muscle_core_JNIConduitExit::_wait_long(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jlong A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: jint hashCode()
{
	muscle_core_JNIConduitExit::_hashCode_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _hashCode()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/JNIConduitExit", "hashCode", false, "I", argTypes);
}
public: static jmethodID _hashCode(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _hashCode().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jint> > _hashCode(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _hashCode(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jint> >( new muscle::MethodCaller<jint>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitExit
do not instantiate this class manually, better use a muscle_core_JNIConduitExit objectand call this function directly
\author Jan Hegewald
*/
class _hashCode_Caller : public muscle::CallerAttributes<jint>
{
	public: _hashCode_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jint>(env, obj, muscle_core_JNIConduitExit::_CLASSNAME(), muscle_core_JNIConduitExit::_hashCode(env, isStatic))
	{
		assert(isStatic == false);
	}
	jint call()
	{
		return muscle::CallerAttributes<jint>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jclass getClass()
{
	muscle_core_JNIConduitExit::_getClass_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getClass()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/JNIConduitExit", "getClass", false, "Ljava/lang/Class;", argTypes);
}
public: static jmethodID _getClass(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getClass().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jclass> > _getClass(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getClass(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jclass> >( new muscle::MethodCaller<jclass>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitExit
do not instantiate this class manually, better use a muscle_core_JNIConduitExit objectand call this function directly
\author Jan Hegewald
*/
class _getClass_Caller : public muscle::CallerAttributes<jclass>
{
	public: _getClass_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jclass>(env, obj, muscle_core_JNIConduitExit::_CLASSNAME(), muscle_core_JNIConduitExit::_getClass(env, isStatic))
	{
		assert(isStatic == false);
	}
	jclass call()
	{
		return muscle::CallerAttributes<jclass>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void notify()
{
	muscle_core_JNIConduitExit::_notify_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _notify()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/JNIConduitExit", "notify", false, "", argTypes);
}
public: static jmethodID _notify(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _notify().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _notify(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _notify(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitExit
do not instantiate this class manually, better use a muscle_core_JNIConduitExit objectand call this function directly
\author Jan Hegewald
*/
class _notify_Caller : public muscle::CallerAttributes<void>
{
	public: _notify_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_JNIConduitExit::_CLASSNAME(), muscle_core_JNIConduitExit::_notify(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call()
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void notifyAll()
{
	muscle_core_JNIConduitExit::_notifyAll_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _notifyAll()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("muscle/core/JNIConduitExit", "notifyAll", false, "", argTypes);
}
public: static jmethodID _notifyAll(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _notifyAll().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _notifyAll(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _notifyAll(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type muscle.core.JNIConduitExit
do not instantiate this class manually, better use a muscle_core_JNIConduitExit objectand call this function directly
\author Jan Hegewald
*/
class _notifyAll_Caller : public muscle::CallerAttributes<void>
{
	public: _notifyAll_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, muscle_core_JNIConduitExit::_CLASSNAME(), muscle_core_JNIConduitExit::_notifyAll(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call()
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
};
/**
cpp counterpart class to access the java class utilities.jni.JNIMethod
function names prefixed with an underscore _ are cpp only utility functions which do not exist in the java class
\author Jan Hegewald
*/
class utilities_jni_JNIMethod
{
/**
function which returns the classname as a cpp jni signature (e.g. java_lang_String)
*/
	public: static std::string _CLASSNAME()
	{
		return "utilities/jni/JNIMethod";
	}
	public: utilities_jni_JNIMethod(JNIEnv* newEnv, jobject newDelegate)
	: jenv(newEnv), delegate(newDelegate)
{
	// init jvm pointer
	jint result = newEnv->GetJavaVM(&jvm);
	if(result < 0)
		throw std::runtime_error("error obtaining JVM");
	// see if we got the right jobject
	muscle::JNITool::assertInstanceOf(newEnv, newDelegate, _CLASSNAME(), __FILE__, __LINE__);
}
private: JNIEnv* jenv;
private: JavaVM* jvm;
private: jobject delegate;
/**
returns a reference to the JNIEnv pointer belonging to the current thread
*/
public: JNIEnv*& _GETJNIENV()
{
	jint result = jvm->AttachCurrentThread((void **)&jenv, NULL);
	if(result != JNI_OK)
		throw std::runtime_error("error obtaining Java env");
	return jenv;
}
//
public: jstring toString()
{
	utilities_jni_JNIMethod::_toString_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _toString()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("utilities/jni/JNIMethod", "toString", false, "Ljava/lang/String;", argTypes);
}
public: static jmethodID _toString(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _toString().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jstring> > _toString(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _toString(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jstring> >( new muscle::MethodCaller<jstring>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type utilities.jni.JNIMethod
do not instantiate this class manually, better use a utilities_jni_JNIMethod objectand call this function directly
\author Jan Hegewald
*/
class _toString_Caller : public muscle::CallerAttributes<jstring>
{
	public: _toString_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jstring>(env, obj, utilities_jni_JNIMethod::_CLASSNAME(), utilities_jni_JNIMethod::_toString(env, isStatic))
	{
		assert(isStatic == false);
	}
	jstring call()
	{
		return muscle::CallerAttributes<jstring>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jstring getName()
{
	utilities_jni_JNIMethod::_getName_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getName()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("utilities/jni/JNIMethod", "getName", false, "Ljava/lang/String;", argTypes);
}
public: static jmethodID _getName(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getName().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jstring> > _getName(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getName(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jstring> >( new muscle::MethodCaller<jstring>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type utilities.jni.JNIMethod
do not instantiate this class manually, better use a utilities_jni_JNIMethod objectand call this function directly
\author Jan Hegewald
*/
class _getName_Caller : public muscle::CallerAttributes<jstring>
{
	public: _getName_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jstring>(env, obj, utilities_jni_JNIMethod::_CLASSNAME(), utilities_jni_JNIMethod::_getName(env, isStatic))
	{
		assert(isStatic == false);
	}
	jstring call()
	{
		return muscle::CallerAttributes<jstring>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jobject getMethod()
{
	utilities_jni_JNIMethod::_getMethod_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getMethod()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("utilities/jni/JNIMethod", "getMethod", false, "Ljava/lang/reflect/Method;", argTypes);
}
public: static jmethodID _getMethod(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getMethod().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _getMethod(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getMethod(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type utilities.jni.JNIMethod
do not instantiate this class manually, better use a utilities_jni_JNIMethod objectand call this function directly
\author Jan Hegewald
*/
class _getMethod_Caller : public muscle::CallerAttributes<jobject>
{
	public: _getMethod_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, utilities_jni_JNIMethod::_CLASSNAME(), utilities_jni_JNIMethod::_getMethod(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call()
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jstring getDescriptor()
{
	utilities_jni_JNIMethod::_getDescriptor_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getDescriptor()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("utilities/jni/JNIMethod", "getDescriptor", false, "Ljava/lang/String;", argTypes);
}
public: static jmethodID _getDescriptor(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getDescriptor().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jstring> > _getDescriptor(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getDescriptor(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jstring> >( new muscle::MethodCaller<jstring>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type utilities.jni.JNIMethod
do not instantiate this class manually, better use a utilities_jni_JNIMethod objectand call this function directly
\author Jan Hegewald
*/
class _getDescriptor_Caller : public muscle::CallerAttributes<jstring>
{
	public: _getDescriptor_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jstring>(env, obj, utilities_jni_JNIMethod::_CLASSNAME(), utilities_jni_JNIMethod::_getDescriptor(env, isStatic))
	{
		assert(isStatic == false);
	}
	jstring call()
	{
		return muscle::CallerAttributes<jstring>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jobject getDelegate()
{
	utilities_jni_JNIMethod::_getDelegate_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getDelegate()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("utilities/jni/JNIMethod", "getDelegate", false, "Ljava/lang/Object;", argTypes);
}
public: static jmethodID _getDelegate(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getDelegate().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jobject> > _getDelegate(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getDelegate(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jobject> >( new muscle::MethodCaller<jobject>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type utilities.jni.JNIMethod
do not instantiate this class manually, better use a utilities_jni_JNIMethod objectand call this function directly
\author Jan Hegewald
*/
class _getDelegate_Caller : public muscle::CallerAttributes<jobject>
{
	public: _getDelegate_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jobject>(env, obj, utilities_jni_JNIMethod::_CLASSNAME(), utilities_jni_JNIMethod::_getDelegate(env, isStatic))
	{
		assert(isStatic == false);
	}
	jobject call()
	{
		return muscle::CallerAttributes<jobject>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void wait_long_int(jlong A0, jint A1)
{
	utilities_jni_JNIMethod::_wait_long_int_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0, A1);
}
public: static muscle::NativeAccessMethod _wait_long_int()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "J" );
argTypes.push_back( "I" );
return muscle::NativeAccessMethod("utilities/jni/JNIMethod", "wait", false, "", argTypes);
}
public: static jmethodID _wait_long_int(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _wait_long_int().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _wait_long_int(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _wait_long_int(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type utilities.jni.JNIMethod
do not instantiate this class manually, better use a utilities_jni_JNIMethod objectand call this function directly
\author Jan Hegewald
*/
class _wait_long_int_Caller : public muscle::CallerAttributes<void>
{
	public: _wait_long_int_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, utilities_jni_JNIMethod::_CLASSNAME(), utilities_jni_JNIMethod::_wait_long_int(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jlong A0, jint A1)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0, A1);
	}
	private: bool isStatic;
};
//
public: void wait()
{
	utilities_jni_JNIMethod::_wait_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _wait()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("utilities/jni/JNIMethod", "wait", false, "", argTypes);
}
public: static jmethodID _wait(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _wait().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _wait(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _wait(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type utilities.jni.JNIMethod
do not instantiate this class manually, better use a utilities_jni_JNIMethod objectand call this function directly
\author Jan Hegewald
*/
class _wait_Caller : public muscle::CallerAttributes<void>
{
	public: _wait_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, utilities_jni_JNIMethod::_CLASSNAME(), utilities_jni_JNIMethod::_wait(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call()
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void wait_long(jlong A0)
{
	utilities_jni_JNIMethod::_wait_long_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _wait_long()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "J" );
return muscle::NativeAccessMethod("utilities/jni/JNIMethod", "wait", false, "", argTypes);
}
public: static jmethodID _wait_long(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _wait_long().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _wait_long(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _wait_long(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type utilities.jni.JNIMethod
do not instantiate this class manually, better use a utilities_jni_JNIMethod objectand call this function directly
\author Jan Hegewald
*/
class _wait_long_Caller : public muscle::CallerAttributes<void>
{
	public: _wait_long_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, utilities_jni_JNIMethod::_CLASSNAME(), utilities_jni_JNIMethod::_wait_long(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call(jlong A0)
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: jint hashCode()
{
	utilities_jni_JNIMethod::_hashCode_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _hashCode()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("utilities/jni/JNIMethod", "hashCode", false, "I", argTypes);
}
public: static jmethodID _hashCode(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _hashCode().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jint> > _hashCode(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _hashCode(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jint> >( new muscle::MethodCaller<jint>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type utilities.jni.JNIMethod
do not instantiate this class manually, better use a utilities_jni_JNIMethod objectand call this function directly
\author Jan Hegewald
*/
class _hashCode_Caller : public muscle::CallerAttributes<jint>
{
	public: _hashCode_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jint>(env, obj, utilities_jni_JNIMethod::_CLASSNAME(), utilities_jni_JNIMethod::_hashCode(env, isStatic))
	{
		assert(isStatic == false);
	}
	jint call()
	{
		return muscle::CallerAttributes<jint>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jclass getClass()
{
	utilities_jni_JNIMethod::_getClass_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _getClass()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("utilities/jni/JNIMethod", "getClass", false, "Ljava/lang/Class;", argTypes);
}
public: static jmethodID _getClass(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _getClass().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jclass> > _getClass(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _getClass(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jclass> >( new muscle::MethodCaller<jclass>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type utilities.jni.JNIMethod
do not instantiate this class manually, better use a utilities_jni_JNIMethod objectand call this function directly
\author Jan Hegewald
*/
class _getClass_Caller : public muscle::CallerAttributes<jclass>
{
	public: _getClass_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jclass>(env, obj, utilities_jni_JNIMethod::_CLASSNAME(), utilities_jni_JNIMethod::_getClass(env, isStatic))
	{
		assert(isStatic == false);
	}
	jclass call()
	{
		return muscle::CallerAttributes<jclass>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: jboolean equals_Object(jobject A0)
{
	utilities_jni_JNIMethod::_equals_Object_Caller caller(_GETJNIENV(), delegate);
	return caller.call(A0);
}
public: static muscle::NativeAccessMethod _equals_Object()
{
	std::vector<std::string> argTypes;
argTypes.push_back( "Ljava/lang/Object;" );
return muscle::NativeAccessMethod("utilities/jni/JNIMethod", "equals", false, "Z", argTypes);
}
public: static jmethodID _equals_Object(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _equals_Object().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<jboolean> > _equals_Object(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _equals_Object(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<jboolean> >( new muscle::MethodCaller<jboolean>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type utilities.jni.JNIMethod
do not instantiate this class manually, better use a utilities_jni_JNIMethod objectand call this function directly
\author Jan Hegewald
*/
class _equals_Object_Caller : public muscle::CallerAttributes<jboolean>
{
	public: _equals_Object_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<jboolean>(env, obj, utilities_jni_JNIMethod::_CLASSNAME(), utilities_jni_JNIMethod::_equals_Object(env, isStatic))
	{
		assert(isStatic == false);
	}
	jboolean call(jobject A0)
	{
		return muscle::CallerAttributes<jboolean>::call(muscle::VABEGIN::FIRST, A0);
	}
	private: bool isStatic;
};
//
public: void notify()
{
	utilities_jni_JNIMethod::_notify_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _notify()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("utilities/jni/JNIMethod", "notify", false, "", argTypes);
}
public: static jmethodID _notify(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _notify().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _notify(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _notify(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type utilities.jni.JNIMethod
do not instantiate this class manually, better use a utilities_jni_JNIMethod objectand call this function directly
\author Jan Hegewald
*/
class _notify_Caller : public muscle::CallerAttributes<void>
{
	public: _notify_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, utilities_jni_JNIMethod::_CLASSNAME(), utilities_jni_JNIMethod::_notify(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call()
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
//
public: void notifyAll()
{
	utilities_jni_JNIMethod::_notifyAll_Caller caller(_GETJNIENV(), delegate);
	return caller.call();
}
public: static muscle::NativeAccessMethod _notifyAll()
{
	std::vector<std::string> argTypes;
return muscle::NativeAccessMethod("utilities/jni/JNIMethod", "notifyAll", false, "", argTypes);
}
public: static jmethodID _notifyAll(JNIEnv*& env, bool& isStatic)
{
	jclass cls = env->FindClass(_CLASSNAME().c_str());
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	isStatic = false;
	jmethodID mid = _notifyAll().getMethodID(env, cls);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	return mid;
}
public: static std::tr1::shared_ptr<muscle::AbstractMethodCaller<void> > _notifyAll(JNIEnv*& env, jobject& obj)
{
	muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);
	bool isStatic;
	jmethodID mid = _notifyAll(env, isStatic);
	muscle::JNITool::catchJREException(env, __FILE__, __LINE__);
	
	return std::tr1::shared_ptr<muscle::MethodCaller<void> >( new muscle::MethodCaller<void>(env, obj, _CLASSNAME(), mid) );
}
/**
internal helper class to cache a jni method invocation procedure
this caller can be used to invoke a method on the target java object of type utilities.jni.JNIMethod
do not instantiate this class manually, better use a utilities_jni_JNIMethod objectand call this function directly
\author Jan Hegewald
*/
class _notifyAll_Caller : public muscle::CallerAttributes<void>
{
	public: _notifyAll_Caller(JNIEnv* env, jobject& obj)
	: muscle::CallerAttributes<void>(env, obj, utilities_jni_JNIMethod::_CLASSNAME(), utilities_jni_JNIMethod::_notifyAll(env, isStatic))
	{
		assert(isStatic == false);
	}
	void call()
	{
		muscle::CallerAttributes<void>::call(muscle::VABEGIN::FIRST);
	}
	private: bool isStatic;
};
};
} // EO namespace muscle
#endif


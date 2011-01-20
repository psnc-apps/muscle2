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

#ifndef NativeAccessMethod_7743F57C_D015_4B57_B661_352D727E3D46
#define NativeAccessMethod_7743F57C_D015_4B57_B661_352D727E3D46

#include <vector>

//
namespace muscle {

// descriptors can be e.g. "Ljava/lang/String;" or "D", see JNITool::fieldDescriptor

/**
stores information about a jmethodID (signatures, associated class etc.)
\author Jan Hegewald
*/
class NativeAccessMethod
{
public:
	NativeAccessMethod(const std::string newClassName, const std::string newMethodName, bool newIsStatic, const std::string newReturnDescriptor, const std::string argDescriptor = "")
		: _className(newClassName), _methodName(newMethodName), _isStatic(newIsStatic), _returnDescriptor(newReturnDescriptor)
	{
		argDescriptors = std::vector<std::string>(1,argDescriptor);
	}
	
	NativeAccessMethod(const std::string newClassName, const std::string newMethodName, bool newIsStatic, const std::string newReturnDescriptor, const std::vector<std::string> newArgDescriptors)
		: _className(newClassName), _methodName(newMethodName), _isStatic(newIsStatic), _returnDescriptor(newReturnDescriptor), argDescriptors(newArgDescriptors)
	{
	}
	
	virtual ~NativeAccessMethod()
	{
	}
	

	//
	jmethodID getMethodID(JNIEnv*& env, jobject& obj)
	{
		jclass cls = env->GetObjectClass(obj);
		JNITool::catchJREException(env, __FILE__, __LINE__);
		return getMethodID(env, cls);
	}

	
	//
	jmethodID getMethodID(JNIEnv*& env, jclass& cls)
	{
		if( isStatic() ) {
			jmethodID mid = env->GetStaticMethodID(cls, _methodName.c_str(), signature().c_str());
			JNITool::catchJREException(env, __FILE__, __LINE__);
			return mid;
		}
		
		jmethodID mid = env->GetMethodID(cls, _methodName.c_str(), signature().c_str());
		JNITool::catchJREException(env, __FILE__, __LINE__);
		return mid;
	}
	
	
	//
	bool isStatic()
	{
		return _isStatic;
	}
	
	
private:
//	std::string className()
//	{
//		return _className;
//	}
//
//	std::string methodName()
//	{
//		return _methodName;
//	}

	std::string signature()
	{
		return JNIMethod::signature(_returnDescriptor, argDescriptors);
	}


private:
	const std::string _className;
	const std::string _methodName;
	bool _isStatic;
	const std::string _returnDescriptor;
	std::vector<std::string> argDescriptors;
};

} // EO namespace muscle
#endif

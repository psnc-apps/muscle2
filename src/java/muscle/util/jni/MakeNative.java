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

package muscle.util.jni;

import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import muscle.util.CodeWriter;

/**
produces cpp code to get access to a java class
@author Jan Hegewald
*/
public class MakeNative {

	//
//	private static final String nl = "/*do not edit*/"+System.getProperty("line.separator");
	private static final String nl = System.getProperty("line.separator");
	private static final String end = ";"+nl; // ends a cpp statement

	private Class<?> cls;
	private String cppClsName;
	private String javaClsID;


	//
	public MakeNative(Class<?> newCls) {
	
		cls = newCls;
		cppClsName = muscle.util.ClassTool.getName(cls).replace('.','_');
		javaClsID = muscle.util.ClassTool.getName(cls).replace('.','/');
	}

	
	/**
	generates cpp source code for body of cpp class declaration
	*/
	public String toClass() {

		StringWriter output = new StringWriter();
		CodeWriter code = new CodeWriter(output);
		code.doc("cpp counterpart class to access the java class "+muscle.util.ClassTool.getName(cls)
			, "function names prefixed with an underscore _ are cpp only utility functions which do not exist in the java class"
			, "\\author Jan Hegewald");
		code.addln("class "+cppClsName);
		code.begin();
		
		code.doc("function which returns the classname as a cpp jni signature (e.g. java_lang_String)");
		code.addln("public: static std::string _CLASSNAME()");
		code.begin();
		code.addln("return \""+javaClsID+"\";");
		code.end();
		
		code.add(toDeclaration());
		code.end("};"+nl);
		
		code.close();
		return output.toString();
	}


	//
	public static String includeCode() {
	
		return "#include <JNITool.h>"+nl
			+"#include <access/NativeAccessMethod.h>"+nl
			+"#if __GNUC__ >= 4"+nl
			+"#include <tr1/memory>"+nl
			+"#else"+nl
			+"#include <memory>"+nl
			+"#endif"+nl
			+"#include <access/AbstractMethodCaller.h>"+nl
			+"#include <access/MethodCaller.h>"+nl
			+"#include <access/MethodCallerStatic.h>"+nl
			+"#include <access/CallerStaticAttributes.h>"+nl
			+"#include <access/CallerAttributes.h>"+nl
			+"#include <jni.h>"+nl;
	}

	/**
	complete contents for a cpp header file
	*/
	public String toString() {
		
		return "#ifndef "+cppClsName+"_DAABC73E_18CF_4601_BBA5_C7CF536C34FF"+nl
		+"#define "+cppClsName+"_DAABC73E_18CF_4601_BBA5_C7CF536C34FF"+nl
		+includeCode()
		+"namespace muscle {"+nl
		+toClass()
		+"} // EO namespace muscle"+nl
		+"#endif";
	}
	
	
	/**
	generates source code for a cpp class containing the methods (muscle::NativeAccessMethod) of a java class<br>
	the class needs to include eveything returned by #includeCode()
	*/
	private String methodToCpp(Method m) throws java.lang.ClassNotFoundException {
		
		String argCode = "std::vector<std::string> argTypes;"+nl;
		for(Class<?> t : m.getParameterTypes()) {
			argCode += "argTypes.push_back( \""+JNITool.toFieldDescriptor(t.getName())+"\" );"+nl;
		}
				
		String nameSuffix = "";
		for(Class<?> t : m.getParameterTypes()) {
			if(t.isArray())
				nameSuffix += "_"+t.getComponentType().getSimpleName()+"_"; // suffix arrays with a _
			else
				nameSuffix += "_"+t.getSimpleName();
		}
		
		boolean isStatic = Modifier.isStatic(m.getModifiers());
		
		StringWriter output = new StringWriter();
		CodeWriter code = new CodeWriter(output);
		code.addln("//");
		
		String functionName = m.getName()+nameSuffix; // use method name for name of function
		String cppReturnType = JNITool.toCppTypename(m.getReturnType());
		String callArgsSignature = ""; // e.g. "jint A0, jint A1"
		String callArgs = ""; // e.g. "muscle::VABEGIN::FIRST, A0, A1"
		int argID = 0;
		for(Class<?> t : m.getParameterTypes()) {
			if("".equals(callArgsSignature))
				callArgsSignature = JNITool.toCppTypename(t)+" A"+argID;
			else
				callArgsSignature += ", "+JNITool.toCppTypename(t)+" A"+argID;
			if("".equals(callArgs))
				callArgs = "A"+argID;
			else
				callArgs += ", A"+argID;
			argID ++;
		}
		
		// the preferred function to call a method on our java object
		code.addln("public: "+cppReturnType+" "+functionName+"("+callArgsSignature+")");
		code.begin();
		if(isStatic)
			code.addln(cppClsName+"::_"+functionName+"_Caller caller(_GETJNIENV());");
		else
			code.addln(cppClsName+"::_"+functionName+"_Caller caller(_GETJNIENV(), delegate);");
		code.addln("return caller.call("+callArgs+");");
		code.end();
		
		// same as above, but can be called without an instance of this class
		if(isStatic) {
			if(callArgsSignature.length() == 0)
				code.addln("public: static "+cppReturnType+" "+functionName+"(JNIEnv*& env)");
			else
				code.addln("public: static "+cppReturnType+" "+functionName+"(JNIEnv*& env, "+callArgsSignature+")");
			code.begin();
			code.addln(cppClsName+"::_"+functionName+"_Caller caller(env);");
			code.addln("return caller.call("+callArgs+");");
			code.end();
		}

		functionName = "_"+functionName; // prefix function name for the utility functions
		// return muscle::NativeAccessMethod object for this method
		code.addln("public: static muscle::NativeAccessMethod "+functionName+"()");
		code.begin();
		code.add(argCode
		+"return muscle::NativeAccessMethod(\""+javaClsID
		+"\", \""+m.getName()
		+"\", "+isStatic
		+", \""+JNITool.toFieldDescriptor(m.getReturnType().getName())
		+"\", argTypes"
		+")"+end);
		code.end();

		// return a jmethodID for this method (requires a JNIEnv)
		code.addln("public: static jmethodID "+functionName+"(JNIEnv*& env, bool& isStatic)");
		code.begin();
		code.addln("jclass cls = env->FindClass(_CLASSNAME().c_str());");
		code.addln("muscle::JNITool::catchJREException(env, __FILE__, __LINE__);");
		code.addln("isStatic = "+isStatic+";");
		code.addln("jmethodID mid = "+functionName+"().getMethodID(env, cls);");
		code.addln("muscle::JNITool::catchJREException(env, __FILE__, __LINE__);");
		code.addln("return mid;");
		code.end();

		// return a muscle::MethodCaller for this method (requires a JNIEnv and jobject)
		String returnText;
		String clsText;
		if(isStatic) {
			clsText = "jclass cls = env->FindClass(_CLASSNAME().c_str());"+nl
						+"muscle::JNITool::catchJREException(env, __FILE__, __LINE__);";
			returnText = "std::tr1::shared_ptr<muscle::MethodCallerStatic<"+cppReturnType+"> >( new muscle::MethodCallerStatic<"+cppReturnType+">(env, cls, _CLASSNAME(), mid) );";
		}
		else {
			clsText = "";
			returnText = "std::tr1::shared_ptr<muscle::MethodCaller<"+cppReturnType+"> >( new muscle::MethodCaller<"+cppReturnType+">(env, obj, _CLASSNAME(), mid) );";
		}
		code.add("public: static ");
		code.addln("std::tr1::shared_ptr<muscle::AbstractMethodCaller<"+cppReturnType+"> > "+functionName+"(JNIEnv*& env, jobject& obj)");
		code.begin();
		code.addln("muscle::JNITool::assertInstanceOf(env, obj, _CLASSNAME(), __FILE__, __LINE__);");
		code.addln("bool isStatic;");
		code.addln("jmethodID mid = "+functionName+"(env, isStatic);");
		code.addln("muscle::JNITool::catchJREException(env, __FILE__, __LINE__);");
		code.addln(clsText);
		code.addln("return "+returnText);
		code.end();

		// only available if method is static:
		// return a muscle::MethodCaller for this method (requires only a JNIEnv)
		// this is basically the same as above, but does nor require to pass a jobject
		if(isStatic) {
			code.add("public: ");
			code.addln("std::tr1::shared_ptr<muscle::AbstractMethodCaller<"+cppReturnType+"> > "+functionName+"(JNIEnv*& env)");
			code.begin();
			code.addln("bool isStatic;");
			code.addln("jmethodID mid = "+functionName+"(env, isStatic);");
			code.addln("muscle::JNITool::catchJREException(env, __FILE__, __LINE__);");
			code.addln("jclass cls = env->FindClass(_CLASSNAME().c_str());");
			code.addln("muscle::JNITool::catchJREException(env, __FILE__, __LINE__);");
			code.addln("return std::tr1::shared_ptr<muscle::MethodCallerStatic<"+cppReturnType+"> >( new muscle::MethodCallerStatic<"+cppReturnType+">(env, cls, _CLASSNAME(), mid) );");
			code.end();
		}
		
		// create dedicated Caller class to call this method
		String callerCallArgs = "muscle::VABEGIN::FIRST";
		if(callArgs.length() > 0)
			callerCallArgs = callerCallArgs+", "+callArgs;
		if(isStatic) {
			code.doc("helper class to cache a static jni method invocation procedure"+nl
			+"this caller can be used to invoke a static method on the target java class "+muscle.util.ClassTool.getName(cls)+nl
			+"it should be used if there is no object of the target class "+cppClsName+" (which is not required for static calls)"
			+" and the method needs to be invoked frequently", "\\author Jan Hegewald");
			code.addln("class "+functionName+"_Caller : public muscle::CallerStaticAttributes<"+cppReturnType+">");
			code.begin();
			// constructor
			code.addln("public: "+functionName+"_Caller(JNIEnv* env)");
			code.addln(": muscle::CallerStaticAttributes<"+cppReturnType+">(env, env->FindClass("+cppClsName+"::_CLASSNAME().c_str()), "+cppClsName+"::_CLASSNAME(), "+cppClsName+"::"+functionName+"(env, isStatic))");
			code.begin();
			code.addln("assert(isStatic == "+isStatic+");");
			code.end();
			// the call function
			code.addln(cppReturnType+" call("+callArgsSignature+")");
			code.begin();
			if(cppReturnType.equals("void"))
				code.addln("muscle::CallerStaticAttributes<"+cppReturnType+">::call("+callerCallArgs+");");
			else
				code.addln("return muscle::CallerStaticAttributes<"+cppReturnType+">::call("+callerCallArgs+");");
			code.end();
			code.addln("private: bool isStatic;");
			code.end("};"+nl);
		}
		else {
			code.doc("internal helper class to cache a jni method invocation procedure"+nl
			+"this caller can be used to invoke a method on the target java object of type "+muscle.util.ClassTool.getName(cls)+nl
			+"do not instantiate this class manually, better use a "+cppClsName+" object"
			+"and call this function directly", "\\author Jan Hegewald");
			code.addln("class "+functionName+"_Caller : public muscle::CallerAttributes<"+cppReturnType+">");
			code.begin();
			// constructor
			code.addln("public: "+functionName+"_Caller(JNIEnv* env, jobject& obj)");
			code.addln(": muscle::CallerAttributes<"+cppReturnType+">(env, obj, "+cppClsName+"::_CLASSNAME(), "+cppClsName+"::"+functionName+"(env, isStatic))");
			code.begin();
			code.addln("assert(isStatic == "+isStatic+");");
			code.end();
			// the call function
			code.addln(cppReturnType+" call("+callArgsSignature+")");
			code.begin();
			if(cppReturnType.equals("void"))
				code.addln("muscle::CallerAttributes<"+cppReturnType+">::call("+callerCallArgs+");");
			else
				code.addln("return muscle::CallerAttributes<"+cppReturnType+">::call("+callerCallArgs+");");
			code.end();
			code.addln("private: bool isStatic;");
			code.end("};"+nl);
		}
				
		code.close();
		return output.toString();
	}
	
	
	// body of cpp declaration
	private String toDeclaration() {
		
		StringWriter output = new StringWriter();
		CodeWriter code = new CodeWriter(output);

		// public constructor
		code.addln("public: "+cppClsName+"(JNIEnv* newEnv, jobject newDelegate)");
		code.addln("\t: jenv(newEnv), delegate(newDelegate)");
		code.begin();
		code.addln("// init jvm pointer");
		code.addln("jint result = newEnv->GetJavaVM(&jvm);");
		code.addln("if(result < 0)");
		code.addln("\tthrow std::runtime_error(\"error obtaining JVM\");");
		code.addln("// see if we got the right jobject");
		code.addln("muscle::JNITool::assertInstanceOf(newEnv, newDelegate, _CLASSNAME(), __FILE__, __LINE__);");
		code.end();

		// attributes
		code.addln("private: JNIEnv* jenv;");
		code.addln("private: JavaVM* jvm;");
		code.addln("private: jobject delegate;");
		
		// access thread save env pointer
		code.addln("/**");
		code.addln("returns a reference to the JNIEnv pointer belonging to the current thread");
		code.addln("*/");
		code.addln("public: JNIEnv*& _GETJNIENV()");
		code.begin();
		code.addln("jint result = jvm->AttachCurrentThread((void **)&jenv, NULL);");
		code.addln("if(result != JNI_OK)");
		code.addln("\tthrow std::runtime_error(\"error obtaining Java env\");");
		code.addln("return jenv;");
		code.end();


		// functions
		try {
			for(Method m : cls.getMethods()) {
				code.add(methodToCpp(m));
			}
		}
		catch(java.lang.ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		
		code.close();
		return output.toString();
	}

//	private String mkSingleton() {
//	
//		// private! access to sigleton
//		StringBuilder code = new StringBuilder();
//		code.append("private: static "+cppClsName+"* only()"+nl);
//		code.append("{"+nl);
//		code.append("static "+cppClsName+" me"+end);
//		code.append("return  &me"+end);
//		code.append("}"+nl);
//
//		// make constructor private
//		code.append("private: "+cppClsName+"()"+nl);
//		code.append("{"+nl);
//		code.append("}"+nl);
//		
//		return code.toString();
//	}

}


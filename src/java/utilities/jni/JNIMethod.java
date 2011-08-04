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

package utilities.jni;


import java.lang.reflect.Method;


/**
helper class to construct a jmethodID in native code<br>
delegate is the object on which to call the method
@author Jan Hegewald
*/
public final class JNIMethod {

	private String name;
	private Object delegate;
	private Method method;
	private String descriptor; // the descriptor can be a specialized version of the descriptor which fits to our method (e.g. use int[] as arg instead of Object)

	
	//
	public JNIMethod(Object newDelegate, String methodName, Class<?>... parameterTypes) {
		this(newDelegate, methodName, parameterTypes, null, null, null);
	}


	/**
	use this constructor to provide different signatures for the real method and for the descriptor,
	e.g. a method argument type might be Object.class, but you want to provide an int[].class instead (which of course is a Object)<br>
	if a specialized return type is provided, the developer has to ensure that a downcast from returnType to the specializedReturnType is possible during runtime,
	this can be the case for a generic return type which will be of type Object during runtime due to type erasure
	*/
	public JNIMethod(Object newDelegate, String methodName, Class<?>[] parameterTypes, Class<?>[] specializedParameterTypes, Class<?> returnType, Class<?> specializedReturnType) {

		if(specializedParameterTypes == null)
			specializedParameterTypes = parameterTypes;
		if(specializedReturnType == null)
			specializedReturnType = returnType;
		if(parameterTypes.length != specializedParameterTypes.length)
			throw new IllegalArgumentException("must have the same number of parameter types <"+parameterTypes+"> vs <"+specializedParameterTypes+">");
		// assure that our specializedParameterTypes are subclasses of the parameterTypes
		for(int i = 0; i < parameterTypes.length; i++) {
			if( !parameterTypes[i].isAssignableFrom(specializedParameterTypes[i]) )
				throw new IllegalArgumentException("<"+parameterTypes[i]+"> is not assignable from <"+specializedParameterTypes[i]+">");
		}
		if( returnType != null && !returnType.isAssignableFrom(specializedReturnType) )
			throw new IllegalArgumentException("<"+returnType+"> is not assignable from <"+specializedReturnType+">");
		
		delegate = newDelegate;
		try {
			method = delegate.getClass().getMethod(methodName, parameterTypes);
		}
		catch(java.lang.NoSuchMethodException e) {
			throw new IllegalArgumentException(e);
		}
		if( returnType != null && !returnType.equals(method.getReturnType()) )
			throw new IllegalArgumentException("return types do not match <"+returnType+"> vs <"+method.getReturnType()+">");
		
		// generate jni method name and descriptor from the Java method
		name = method.getName();
		
		if(returnType == null)
			returnType = method.getReturnType();
		if(specializedReturnType == null)
			specializedReturnType = method.getReturnType();
		
		// descriptor encodes agrument(s) and return type of the method
		// get method args (if any)
		String jniArgs = "";
		for(Class<?> t : specializedParameterTypes)
			jniArgs += JNITool.toFieldDescriptor(t);
		
		String jniReturn = JNITool.toFieldDescriptor(specializedReturnType);
		if( jniReturn.length() == 0 )
			jniReturn = "V"; // only the method return type is marked as void, never the argument
		descriptor = "("+jniArgs+")"+jniReturn;
	}
	
	
	//
//	public JNIMethod(Object newDelegate, String newName, String newDescriptor) {
//
//		name = newName;
//		descriptor = newDescriptor;
//		delegate = newDelegate;
//	}

	
	//
	public String getName() {
		return name;
	}
	
	
	//
	public String getDescriptor() {
		return descriptor;
	}
	
	
	//
	public Object getDelegate() {
	
		return delegate;
	}
	
	
	//
	public Method getMethod() {
	
		return method;
	}

	
	//
	public String toString() {
		return delegate+"."+name+" "+descriptor;
	}

	// for testing purposes
//	public static void main (String args[]) {
//				
//		String s = "Text";
//		
//		JNIMethod m1 = null;
//		try {
//			m1 = new JNIMethod(s, Test.class.getMethod("arr"));
//		} catch(Exception e) {
//			throw new RuntimeException(e);
//		}
//
//		System.out.println("name:"+m1.getName()+" descriptor:"+m1.getDescriptor());
//	}
//	public static class Test {
//		public boolean[][] arr() {
//			return new boolean[0][0];
//		}
//	};

}

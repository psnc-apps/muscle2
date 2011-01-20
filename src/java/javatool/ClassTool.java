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

package javatool;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;


/**
additional functionality for java.lang.Class
@author Jan Hegewald
*/
public class ClassTool {


	/**
	true if a declared method of baseClass or any superclass upto (and including) lastSuperclass has the native tag<br>
	*/
	static public boolean isNative(Class baseClass, Class lastSuperclass) {

		if(!lastSuperclass.isAssignableFrom(baseClass)) {
			throw new java.lang.IllegalArgumentException("<"+lastSuperclass+"> is not a superclass of <"+baseClass+">");
		}

		Class cls = baseClass;
		while(lastSuperclass.isAssignableFrom(cls)) {
			for(Method m : cls.getDeclaredMethods()) {
				if( Modifier.isNative(m.getModifiers()) ) {
					return true;
				}
			}

			cls = cls.getSuperclass();
		}

		return false;
	}


	/**
	returns all declared methods of this class and all superclasses upto (and including) lastSuperclass<br>
	so if lastSuperclass is java.lang.Object.class, this method should return the same methods as Class#getMethods
	*/
	public static Method[] getMethodsUptoSuperclass(Class baseClass, Class lastSuperclass) {

		if(!lastSuperclass.isAssignableFrom(baseClass)) {
			throw new java.lang.IllegalArgumentException("<"+lastSuperclass+"> is not a superclass of <"+baseClass+">");
		}

		ArrayList<Method> methods = new ArrayList<Method>();
		Class cls = baseClass;

		while(lastSuperclass.isAssignableFrom(cls)) {
			methods.addAll(Arrays.asList(cls.getDeclaredMethods()));
			cls = cls.getSuperclass();
		}

		return (Method[])methods.toArray();
	}


	/**
	handle primitives and array as well as the standard classes (which is the only thing the plain java.lang.Class can handle)
	*/
	public static Class<?> forName(String name) throws ClassNotFoundException {


		Class<?> cls = null;

		// try to get a primitive class for this class-name
		try {
			cls = ClassTool.primitiveClassForName(name);
		}
		catch (ClassNotFoundException e) {
		}

		if(cls == null) {
			// try to get a array class for this class-name
			try {
				cls = ClassTool.arrayClassForName(name);
			}
			catch (ClassNotFoundException e) {
			}
		}

		if(cls == null) {
			// try to get a full class for this class-name with the standard procedure
			try {
				cls = Class.forName(name);
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException(e);
			}
		}

		return cls;
	}


	/**
	full name of a class, e.g. "java.lang.String"<br>
	the method Class#getName often returns null, e.g. if a class has been instantiated via reflection
	*/
	public static String getName(Class<?> cls) {

		return cls.toString().replaceFirst("class ", "");
	}


	//
	public static Class<?> arrayClassForName(String name) throws ClassNotFoundException {

		int index = name.indexOf("[]");
		if(index > 0) {
			String componentName = name.substring(0,index);
			Class<?> componentClass = ClassTool.forName(componentName);
			// get dimensions of array
			assert (name.length()-index) % 2 == 0;
			int dimensions = (name.length()-index) / 2;
			return ArrayClassBuilder.build(componentClass, dimensions);
		}

		throw new ClassNotFoundException("can not get array class for <"+name+">");
	}


	//
	public static Class<?> wrapperClassForPrimitiveClass(Class<?> primitiveClass) throws ClassNotFoundException {

		if(!primitiveClass.isPrimitive()) {
			throw new IllegalArgumentException("no a primitive class <"+primitiveClass+">");
		}

		if(primitiveClass.equals(boolean.class)) {
			return Boolean.class;
		} else if(primitiveClass.equals(byte.class)) {
			return Byte.class;
		} else if(primitiveClass.equals(char.class)) {
			return Character.class;
		} else if(primitiveClass.equals(short.class)) {
			return Short.class;
		} else if(primitiveClass.equals(int.class)) {
			return Integer.class;
		} else if(primitiveClass.equals(long.class)) {
			return Long.class;
		} else if(primitiveClass.equals(float.class)) {
			return Float.class;
		} else if(primitiveClass.equals(double.class)) {
			return Double.class;
		} else if(primitiveClass.equals(void.class)) {
			return Void.class;
		}

		throw new ClassNotFoundException("can not get wrapper class for <"+primitiveClass.getName()+">");
	}


	//
	public static Class<?> primitiveClassForWrapperClass(Class<?> wrapperClass) throws ClassNotFoundException {

		if(wrapperClass.equals(Boolean.class)) {
			return boolean.class;
		} else if(wrapperClass.equals(Byte.class)) {
			return byte.class;
		} else if(wrapperClass.equals(Character.class)) {
			return char.class;
		} else if(wrapperClass.equals(Short.class)) {
			return short.class;
		} else if(wrapperClass.equals(Integer.class)) {
			return int.class;
		} else if(wrapperClass.equals(Long.class)) {
			return long.class;
		} else if(wrapperClass.equals(Float.class)) {
			return float.class;
		} else if(wrapperClass.equals(Double.class)) {
			return double.class;
		} else if(wrapperClass.equals(Void.class)) {
			return void.class;
		}

		throw new ClassNotFoundException("can not get primitive class for <"+wrapperClass.getName()+">");
	}


	//
	public static Class<?> primitiveClassForName(String name) throws ClassNotFoundException {

		if(name.equals("boolean")) {
			return boolean.class;
		} else if(name.equals("byte")) {
			return byte.class;
		} else if(name.equals("char")) {
			return char.class;
		} else if(name.equals("short")) {
			return short.class;
		} else if(name.equals("int")) {
			return int.class;
		} else if(name.equals("long")) {
			return long.class;
		} else if(name.equals("float")) {
			return float.class;
		} else if(name.equals("double")) {
			return double.class;
		} else if(name.equals("void")) {
			return void.class;
		}

		throw new ClassNotFoundException("can not get primitive class for <"+name+">");
	}


	//
	public static Class<?> arrayClassForPrimitive(Class<?> primitiveClass) {

		if( !primitiveClass.isPrimitive() ) {
			throw new IllegalArgumentException("unable to create primitive array class for non primitive type <"+primitiveClass.getName()+">");
		}

		if( primitiveClass.equals(void.class) ) {
			throw new IllegalArgumentException("unable to create array class for primitive type <"+primitiveClass.getName()+">");
		}

		if(primitiveClass.equals(boolean.class)) {
			return boolean[].class;
		} else if(primitiveClass.equals(byte.class)) {
			return byte[].class;
		} else if(primitiveClass.equals(char.class)) {
			return char[].class;
		} else if(primitiveClass.equals(short.class)) {
			return short[].class;
		} else if(primitiveClass.equals(int.class)) {
			return int[].class;
		} else if(primitiveClass.equals(long.class)) {
			return long[].class;
		} else if(primitiveClass.equals(float.class)) {
			return float[].class;
		} else if(primitiveClass.equals(double.class)) {
			return double[].class;
		}

		throw new IllegalArgumentException("unable to create array class for primitive type <"+primitiveClass.getName()+">");
	}


	// helper class to recursively construct multi dimensional array classes
	private static class ArrayClassBuilder<T> {

		//
		public static Class<?> build(Class<?> componentClass, int dims) {

			if(dims == 0) {
				return componentClass;
			}
			if(dims < 0) {
				throw new IllegalArgumentException("array dimensions can not be negative <"+dims+">");
			}

			if( componentClass.isPrimitive() ) {
				componentClass = ClassTool.arrayClassForPrimitive(componentClass);
				dims --;
				if(dims == 0) {
					return componentClass;
				}
			}

			ArrayClassBuilder builder = null;
			Class<?> tmpClass = componentClass;
			for(int i = 0; i < dims; i++) {
				builder = new ArrayClassBuilder(tmpClass);
				tmpClass = builder.getArrayClass();
			}

			return builder.getArrayClass();
		}

		//
		private T[] arr;

		//
		private ArrayClassBuilder(Class<T> cls) {
			if( cls.isPrimitive() ) {
				throw new IllegalArgumentException("class can not be a primitive type <"+cls.getName()+">");
			}

			this.arr = (T[])Array.newInstance(cls,0);
		}

		//
		private Class getArrayClass() {
			return this.arr.getClass();
		}

	}

}




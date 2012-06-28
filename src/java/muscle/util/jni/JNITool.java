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

import muscle.util.ClassTool;

/**
provides solutions for common JNI stuff
@author Jan Hegewald
*/
public class JNITool {


	/**
	returns the JNI type signature for a given Java type,
	e.g. "Ljava/lang/String;" for String.class or "D" for double.class
	*/
	public static String toFieldDescriptor(Class<?> javaClass) {
			
		if(javaClass.equals(void.class))
			return "";
		else if(javaClass.equals(Void.class))
			return "";
		
		if(javaClass.isPrimitive()) {
			return getPrimitiveFieldDescriptor(javaClass);
		}
		else if(javaClass.isArray()) {
			return getArrayFieldDescriptor(javaClass);
		}
		else {
			return getReferenceFieldDescriptor(javaClass);		
		}		
	}
	
	
	/**
	@see #toFieldDescriptor(Class)
	*/
	public static String toFieldDescriptor(String javaClassName) throws ClassNotFoundException  {
		Class<?> cls = ClassTool.forName(javaClassName);
		return toFieldDescriptor(cls);
	}
	
	
	/**
	the corresponding cpp typename
	*/
	public static String toCppTypename(Class<?> javaClass) {
	
		if(javaClass.equals(boolean.class))
			return "jboolean";
		else if(javaClass.equals(byte.class))
			return "jbyte";
		else if(javaClass.equals(char.class))
			return "jchar";
		else if(javaClass.equals(short.class))
			return "jshort";
		else if(javaClass.equals(int.class))
			return "jint";
		else if(javaClass.equals(long.class))
			return "jlong"; // uppercase j
		else if(javaClass.equals(float.class))
			return "jfloat";
		else if(javaClass.equals(double.class))
			return "jdouble";

		else if(javaClass.equals(void.class))
			return "void";

		else if(javaClass.equals(Class.class))
			return "jclass";
		else if(javaClass.equals(String.class))
			return "jstring";
		else if(javaClass.equals(Throwable.class))
			return "jthrowable";
			
		else if(javaClass.isArray()) {
			if( javaClass.getComponentType().isPrimitive() ) {
				return toCppTypename(javaClass.getComponentType())+"Array";
			}
			else {
				return "jobjectArray";
			}
		}

		assert javaClass.isPrimitive() == false;
		return "jobject";			
	}
	
	
	//
	private static String getPrimitiveFieldDescriptor(Class<?> javaClass) {
	
		assert javaClass.isPrimitive() == true;
		
		if(javaClass.equals(boolean.class))
			return "Z";
		else if(javaClass.equals(byte.class))
			return "B";
		else if(javaClass.equals(char.class))
			return "C";
		else if(javaClass.equals(short.class))
			return "S";
		else if(javaClass.equals(int.class))
			return "I";
		else if(javaClass.equals(long.class))
			return "J"; // uppercase j
		else if(javaClass.equals(float.class))
			return "F";
		else if(javaClass.equals(double.class))
			return "D";
				
		throw new IllegalArgumentException("unable to determine JNI field descriptor for primitive type <"+muscle.util.ClassTool.getName(javaClass)+">");		
	}


	// java class can be e.g. java.lang.String[] or [D or java.lang.Integer[][][]
	private static String getArrayFieldDescriptor(Class<?> javaClass) {
	
		assert javaClass.isArray() == true;
		String fullName = muscle.util.ClassTool.getName(javaClass);

		Class<?> elementClass = javaClass.getComponentType();
				
		return "["+toFieldDescriptor(elementClass);
	}


	//
	private static String getReferenceFieldDescriptor(Class<?> javaClass) {
	
		assert javaClass.isPrimitive() == false;
		
		String classDescriptor = muscle.util.ClassTool.getName(javaClass).replaceAll("\\.", "/");
		String fieldDescriptor = "L"+classDescriptor+";";
		
		return fieldDescriptor;		
	}

}


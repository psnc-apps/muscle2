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

package muscle.util;

/**
additional functionality for java.lang.Class
@author Jan Hegewald
*/
public class ClassTool {
	/**
	full name of a class, e.g. "java.lang.String"<br>
	the method Class#getName often returns null, e.g. if a class has been instantiated via reflection
	*/
	public static String getName(Class cls) {
		return cls.toString().replaceFirst("class ", "");
	}
	
	public static Class<?> primitiveClassForWrapperClass(Class<?> wrapperClass) throws ClassNotFoundException {

		if(wrapperClass.equals(Boolean.class))
			return boolean.class;
		else if(wrapperClass.equals(Byte.class))
			return byte.class;
		else if(wrapperClass.equals(Character.class))
			return char.class;
		else if(wrapperClass.equals(Short.class))
			return short.class;
		else if(wrapperClass.equals(Integer.class))
			return int.class;
		else if(wrapperClass.equals(Long.class))
			return long.class;
		else if(wrapperClass.equals(Float.class))
			return float.class;
		else if(wrapperClass.equals(Double.class))
			return double.class;
		else if(wrapperClass.equals(Void.class))
			return void.class;
			
		throw new ClassNotFoundException("can not get primitive class for <"+wrapperClass.getName()+">");
	}

	public static Class<?> primitiveClassForName(String name) throws ClassNotFoundException {
		if(name.equals("boolean"))
			return boolean.class;
		else if(name.equals("byte"))
			return byte.class;
		else if(name.equals("char"))
			return char.class;
		else if(name.equals("short"))
			return short.class;
		else if(name.equals("int"))
			return int.class;
		else if(name.equals("long"))
			return long.class;
		else if(name.equals("float"))
			return float.class;
		else if(name.equals("double"))
			return double.class;
		else if(name.equals("void"))
			return void.class;
			
		throw new ClassNotFoundException("can not get primitive class for <"+name+">");
	}

	public static Class<?> arrayClassForPrimitive(Class<?> primitiveClass) {
	
		if( !primitiveClass.isPrimitive() )
			throw new IllegalArgumentException("unable to create primitive array class for non primitive type <"+primitiveClass.getName()+">");		

		if( primitiveClass.equals(void.class) )
			throw new IllegalArgumentException("unable to create array class for primitive type <"+primitiveClass.getName()+">");		

		if(primitiveClass.equals(boolean.class))
			return boolean[].class;
		else if(primitiveClass.equals(byte.class))
			return byte[].class;
		else if(primitiveClass.equals(char.class))
			return char[].class;
		else if(primitiveClass.equals(short.class))
			return short[].class;
		else if(primitiveClass.equals(int.class))
			return int[].class;
		else if(primitiveClass.equals(long.class))
			return long[].class;
		else if(primitiveClass.equals(float.class))
			return float[].class;
		else if(primitiveClass.equals(double.class))
			return double[].class;
				
		throw new IllegalArgumentException("unable to create array class for primitive type <"+primitiveClass.getName()+">");		
	}
}

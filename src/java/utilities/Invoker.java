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

package utilities;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;


/**
this class provides utility methods for invoking a generic method<br>
may also be used as stand alone programme<br>
issues: curretly it does not work to invokeMethod a static main, there seems to be some trouble with javas varargs handling
@author Jan Hegewald
*/
public class Invoker {

	// invokeMethod a static method
	public static Object invokeMethod(Class cls, String methodName, Object ... args) throws NoSuchMethodException, IllegalAccessException, java.lang.reflect.InvocationTargetException {

		if(cls == null) {
			throw new IllegalArgumentException("cls can not both be null");
		}

		return Invoker.invokeMethod(null, cls, methodName, args);
	}

	// invokeMethod an instance method
	public static Object invokeMethod(Object obj, String methodName, Object ... args) throws NoSuchMethodException, IllegalAccessException, java.lang.reflect.InvocationTargetException {

		if(obj == null) {
			throw new IllegalArgumentException("obj can not both be null");
		}

		return Invoker.invokeMethod(obj, obj.getClass(), methodName, args);
	}

	// obj may be null if a static method should be invoked
	public static Object invokeMethod(Object obj, Class cls, String methodName, Object ... args) throws NoSuchMethodException, IllegalAccessException, java.lang.reflect.InvocationTargetException {

		if(obj == null && cls == null) {
			throw new IllegalArgumentException("obj and cls can not both be null");
		}

		Class[] parameterTypes = null;
		if( args.getClass().equals(Object[].class) ) { // we assume we got an varargs array from multiple individual args
			parameterTypes = new Class[args.length];
			for(int i = 0; i < parameterTypes.length; i++) {
				// prefer raw primitive types above their wrapper classes
				try {
					parameterTypes[i] = javatool.ClassTool.primitiveClassForWrapperClass(args[i].getClass());
				}
				catch(java.lang.ClassNotFoundException e) {
					parameterTypes[i] = args[i].getClass();
				}
			}
		}
		else { // we assume we got a real array, not multiple args
			parameterTypes = new Class[1];
			parameterTypes[0] = args.getClass();
		}
		// here we first try to get the method for out exact signature
		// if this fails, we try to use general Objects as argument types
		// this way we can e.g. call String.toString(String) (which does not exist),
		// but String.toString(Object) does exist which is ofcoures also fine for a String as arg
		Method method = null;
		try {
			method = cls.getMethod(methodName, parameterTypes);
		}
		catch (NoSuchMethodException e) {
			Class[] objectTypes = new Class[parameterTypes.length];
			Arrays.fill(objectTypes, Object.class);
			method = cls.getMethod(methodName, objectTypes);
		}
		Object result = method.invoke(obj, args);
		return result;
	}


   //
	public static Object invokeField(Class cls, String fieldName) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

		if(cls == null) {
			throw new IllegalArgumentException("cls can not be null");
		}

      Field field = cls.getField(fieldName);
      Object result;
      try {
         result = field.get(null); // get static field
      }
      catch(NullPointerException e) {
         throw new RuntimeException("can only access static fields");
      }
      return result;
	}


	//
	public static void main (String cliargs[]) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {

		// we assume a static method should be called
		if (cliargs.length < 2) {
			throw new IllegalArgumentException("at least two args needed: class method [arg0 arg2 ...]"
			+"\nexample: "
			+javatool.ClassTool.getName(Invoker.class)+" java.lang.System getProperty os.name"
			+"\nexample: "
			+javatool.ClassTool.getName(Invoker.class)+" java.lang.Thread sleep java.lang.Long:42"
			+"\nexample: "
			+javatool.ClassTool.getName(Invoker.class)+" java.lang.Long MAX_VALUE"
			);
		}

		Class cls = null;
		cls = Class.forName(cliargs[0]);

		String name = cliargs[1];
		Object[] args = new Object[cliargs.length - 2]; // remaining cliargs are all treated as Strings
		System.arraycopy(cliargs, 2, args, 0, args.length);

      Exception fieldException = null;
      if(args.length == 0) {

         String fieldName = name;
         try {
            System.out.println(""+Invoker.invokeField(cls, fieldName));
            return;
         }
         catch(NoSuchFieldException e) {
            // we try again and assume we want to call a method
            fieldException = e;
         }
      }

		// assume we want to call a method
		String methodName = name;

		// see if we should convert any String arg to another type, e.g. java.lang.Integer
		// assume a format like java.lang.Integer:42
		for (int i = 0; i < args.length; i++) {
			String s = (String) args[i];
			int index = s.indexOf(':');
			if (index > 0) {
				 String argClassName = s.substring(0, index);
				 String argVal = s.substring(index + 1);
				 // try to call the valueOf method of class argClass to convert argVal
				 Class argClass = null;
				 try {
						argClass = Class.forName(argClassName);
				 } catch (java.lang.ClassNotFoundException e) {
						throw new RuntimeException(e);
				 }

				try {
					args[i] = Invoker.invokeMethod(argClass, "valueOf", argVal);
				}
				catch (NoSuchMethodException e) {
					throw new RuntimeException("can not determine how to convert <"+argVal+"> to a <"+argClass+">");
				}
				catch (IllegalAccessException e) {
					throw new RuntimeException("can not determine how to convert <"+argVal+"> to a <"+argClass+">");
				}
				catch (java.lang.reflect.InvocationTargetException e) {
					throw new RuntimeException("<"+argVal+"> is not a <"+argClass+">");
				}
			}
		}

		try {
			// invokeMethod the specified method and print results
			System.out.println(Invoker.invokeMethod(cls, methodName, args));
		}
		catch(NoSuchMethodException e) {

			if(fieldException != null) {
				throw new RuntimeException("can not find field or method <"+name+">\n"+fieldException+"\n"+e);
			}
			else {
				throw e;
			}

		}
	}
}

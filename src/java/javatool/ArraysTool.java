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

import java.io.File;
import java.lang.reflect.Array;
import java.util.Arrays;

import utilities.MiscTool;
import utilities.MiscTool.NotEqualException;


/**
provides additional functionality like the java.util.Arrays class
@author Jan Hegewald
*/
public class ArraysTool {

	/**
	returns the first index where an instance of the specified class can be found
	*/
	public static int indexForInstanceOf(Object[] arr, Class<?> cls) {

		for(int i = 0; i < arr.length; i++) {
			if( cls.isInstance(arr[i]) ) {
				return i;
			}
		}

		return -1;
	}


	/**
	creates a array from an ascii file
	*/
	public static double[] getFromFile_double(File file) {

		String[] content = null;
		try {
			content = MiscTool.fileToString(file).split("(\r\n)|(\n)|(\r)");
		} catch (java.io.IOException e) {
			new RuntimeException(e);
		}
		double[] fileData = new double[content.length];
		// set value for every line
		for(int i = 0; i < content.length; i++) {
			fileData[i] = Double.parseDouble(content[i]);
		}

		return fileData;
	}


	/**
	creates a array from an ascii file
	*/
	public static boolean[] getFromFile_boolean(File file) {

		String[] content = null;
		try {
			content = MiscTool.fileToString(file).split("(\r\n)|(\n)|(\r)");
		} catch (java.io.IOException e) {
			new RuntimeException(e);
		}
		boolean[] fileData = new boolean[content.length];
		// set value for every line
		for(int i = 0; i < content.length; i++) {
			fileData[i] = Boolean.parseBoolean(content[i]);
		}

		return fileData;
	}


	//
	public static <T> T[] asArray(T item) {

		return Arrays.asList(item).toArray((T[])Array.newInstance(item.getClass(), 1));
	}


	/**
	throws an exception if the data of the two arrays does not match
	*/
	public static void assertEqualArrays(Object arrA, Object arrB, final double threshold) throws NotEqualException {

		if( !arrA.getClass().isArray() || !arrB.getClass().isArray() ) {
			throw new IllegalArgumentException("only arrays can be compared");
		}

		// exception if array types are not equal
		if( !arrA.getClass().equals(arrB.getClass()) ) {
			throw new NotEqualException(arrA.getClass().getName()+" vs "+arrB.getClass().getName());
		}

		// exception if array size is not equal
		if( Array.getLength(arrA) != Array.getLength(arrB) ) {
			throw new NotEqualException(Array.getLength(arrA)+" vs "+Array.getLength(arrB));
		}

		// exception if array values are not equal
		for(int i = 0; i < Array.getLength(arrA); i++) {
			if( !MiscTool.equalObjectValues(Array.get(arrA, i), Array.get(arrB, i), threshold) ) {
				throw new NotEqualException("index <"+i+">:"+Array.get(arrA, i)+" vs "+Array.get(arrB, i));
			}
		}
	}
	public static void assertEqualArrays(Object arrA, Object arrB) throws NotEqualException {
		ArraysTool.assertEqualArrays(arrA, arrB, MiscTool.COMPARE_THESHOLD);
	}


	// returns a copy of arr where the element at index is removed
//	public static <T> T removeAt(T arr, int index) {
//
//		T begin = Arrays.copyOfRange(arr, 0, index-1);
//		T end = Arrays.copyOfRange(arr, index+1, arr.length);
//
//		return joinArrays(begin, end);
//	}


	/**
	copies several arrays of same component type into one array<br>
	this will not compile with java 5 in case of primitive component types
	I am not sure if this is an autoboxing bug or a generics bug
	*/
	public static <T> T joinArrays(T ... arrs) {

		// all args must be arrays of same component type
		Class arrayClass = null;
		Class componentClass = null;
		for(T a : arrs) {
			if(!a.getClass().isArray()) {
				throw new IllegalArgumentException("not an array");
			}
			if(arrayClass == null) {
				arrayClass = a.getClass();
				componentClass = arrayClass.getComponentType();
			}
			else if(!a.getClass().equals(arrayClass)) {
				throw new IllegalArgumentException("arrays must be of same baseclass <"+arrayClass.getName()+"> vs. <"+a.getClass().getName()+">");
			}
		}

		int len = 0;
		for(T a : arrs) {
			len += Array.getLength(a);
		}

		T joined = (T)Array.newInstance(componentClass, len);
		int pos = 0;
		for(T src : arrs) {
			System.arraycopy(src, 0, joined, pos, Array.getLength(src));
			pos += Array.getLength(src);
		}

		return joined;
	}
	/**
	@deprecated workaround for java 5, {see joinArrays(T ... arrs)}
	*/
	@Deprecated
	public static double[] joinArraysDouble(double[] ... arrs) {
		return ArraysTool.joinArrays(arrs);
	}


}




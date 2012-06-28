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

import java.lang.reflect.Array;
import muscle.util.MiscTool;
import muscle.util.MiscTool.NotEqualException;

/**
provides additional functionality like the java.util.Arrays class
@author Jan Hegewald
*/
public class ArraysTool {
	/**
	throws an exception if the data of the two arrays does not match
	*/
	public static void assertEqualArrays(Object arrA, Object arrB, final double threshold) throws NotEqualException {

		if( !arrA.getClass().isArray() || !arrB.getClass().isArray() )
			throw new IllegalArgumentException("only arrays can be compared");

		// exception if array types are not equal
		if( !arrA.getClass().equals(arrB.getClass()) )
			throw new NotEqualException(arrA.getClass().getName()+" vs "+arrB.getClass().getName());

		// exception if array size is not equal
		if( Array.getLength(arrA) != Array.getLength(arrB) )
			throw new NotEqualException(Array.getLength(arrA)+" vs "+Array.getLength(arrB));

		// exception if array values are not equal
		for(int i = 0; i < Array.getLength(arrA); i++) {
			if( !MiscTool.equalObjectValues(Array.get(arrA, i), Array.get(arrB, i), threshold) )
				throw new NotEqualException("index <"+i+">:"+Array.get(arrA, i)+" vs "+Array.get(arrB, i));		
		}
	}
}




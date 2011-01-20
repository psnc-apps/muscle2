/* !!! this file was generated automatically from <Array2D_double.java> DO NOT EDIT */
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

package utilities.array2d;	//edit Array2D_double.java:1 instead

import java.lang.reflect.Constructor;


// TODO: inherit functionality from cern.colt.matrix.IntegerMatrix2D?
/**
2D array backed by a 1D C-style array of primitive type
@author Jan Hegewald
*/
public class Array2D_int {

	private int[] data;	//edit Array2D_double.java:4 instead
	private IndexStrategy indexStrategy;	//edit Array2D_double.java:7 instead

	//
	public Array2D_int(int newXSize, int newYSize) {

		this(newXSize, newYSize, new int[newXSize*newYSize], IndexStrategy.FortranIndexStrategy.class);	//edit Array2D_double.java:8 instead
	}

	//
	public Array2D_int(int newXSize, int newYSize, int[] newData) {

		this(newXSize, newYSize, newData, IndexStrategy.FortranIndexStrategy.class);	//edit Array2D_double.java:9 instead
	}

	//
	public Array2D_int(int newXSize, int newYSize, Class<? extends IndexStrategy> strategyClass) {

		this(newXSize, newYSize, new int[newXSize*newYSize], strategyClass);	//edit Array2D_double.java:10 instead
	}


	//
	public Array2D_int(int newXSize, int newYSize, int[] newData, Class<? extends IndexStrategy> strategyClass) {

		this.data = newData;	//edit Array2D_double.java:13 instead

		if( !IndexStrategy.class.isAssignableFrom(strategyClass) ) {
			throw new IllegalArgumentException("index strategy must be a "+javatool.ClassTool.getName(IndexStrategy.class));	//edit Array2D_double.java:14 instead
		}


		Constructor<? extends IndexStrategy> strategyConstructor = null;	//edit Array2D_double.java:15 instead
		try {
			strategyConstructor = strategyClass.getConstructor(int.class, int.class);	//edit Array2D_double.java:16 instead
		}
		catch(java.lang.NoSuchMethodException e) {
			throw new RuntimeException(e);	//edit Array2D_double.java:17 instead
		}
		try {
			this.indexStrategy = strategyConstructor.newInstance(newXSize, newYSize);	//edit Array2D_double.java:18 instead
		}
		catch(java.lang.InstantiationException e) {
			throw new RuntimeException(e);	//edit Array2D_double.java:19 instead
		}
		catch(java.lang.IllegalAccessException e) {
			throw new RuntimeException(e);	//edit Array2D_double.java:20 instead
		}
		catch(java.lang.reflect.InvocationTargetException e) {
			throw new RuntimeException(e);	//edit Array2D_double.java:21 instead
		}
	}


	//
	public int get(int x1, int x2) {

		return this.data[this.indexStrategy.index(x1, x2)];	//edit Array2D_double.java:22 instead
	}

	//
	public int[] getData() {

		return this.data;	//edit Array2D_double.java:23 instead
	}


	//
	public void set(int x1, int x2, int value) {

		this.data[this.indexStrategy.index(x1, x2)] = value;	//edit Array2D_double.java:24 instead
	}


	//
	public int size() {

		return this.data.length;	//edit Array2D_double.java:25 instead
	}
}

/* !!! this file was generated automatically from <Array2D_double.java> DO NOT EDIT */

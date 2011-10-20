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

package utilities.array2d;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;


// TODO: inherit functionality from cern.colt.matrix.DoubleMatrix2D?
/**
2D array backed by a 1D C-style array of primitive type
@author Jan Hegewald
*/
public class Array2D_double {

	private double[] data;
	private int xSize;
	private int ySize;
	private IndexStrategy indexStrategy;
	
	//
	public Array2D_double(int newXSize, int newYSize) {
	
		this(newXSize, newYSize, new double[newXSize*newYSize], IndexStrategy.FortranIndexStrategy.class);
	}

	//
	public Array2D_double(int newXSize, int newYSize, double[] newData) {
	
		this(newXSize, newYSize, newData, IndexStrategy.FortranIndexStrategy.class);
	}

	//
	public Array2D_double(int newXSize, int newYSize, Class<? extends IndexStrategy> strategyClass) {
	
		this(newXSize, newYSize, new double[newXSize*newYSize], strategyClass);
	}


	//
	public Array2D_double(int newXSize, int newYSize, double[] newData, Class<? extends IndexStrategy> strategyClass) {
	
		xSize = newXSize;
		ySize = newYSize;

		data = newData;
		
		if( !IndexStrategy.class.isAssignableFrom(strategyClass) )
			throw new IllegalArgumentException("index strategy must be a "+javatool.ClassTool.getName(IndexStrategy.class));
			
		
		Constructor<? extends IndexStrategy> strategyConstructor = null;
		try {
			strategyConstructor = strategyClass.getConstructor(int.class, int.class);
		}
		catch(java.lang.NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		try {
			indexStrategy = strategyConstructor.newInstance(newXSize, newYSize);
		}
		catch(java.lang.InstantiationException e) {
			throw new RuntimeException(e);
		}
		catch(java.lang.IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		catch(java.lang.reflect.InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}


	//
	public double get(int x1, int x2) {

		return data[indexStrategy.index(x1, x2)];
	}
	
	//
	public double[] getData() {

		return data;
	}


	//
	public void set(int x1, int x2, double value) {

		data[indexStrategy.index(x1, x2)] = value;
	}

	
	//
	public int size() {

		return data.length;
	}
}


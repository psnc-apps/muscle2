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

package utilities.array3d;

import java.lang.reflect.Constructor;
import java.util.Arrays;

// TODO: inherit functionality from cern.colt.matrix.DoubleMatrix3D?
/**
3D array backed by a 1D C-style array of primitive type
@author Jan Hegewald
*/
public class Array3D_double {

	private double[] data;
	private int xSize;
	private int ySize;
	private int zSize;
	private IndexStrategy indexStrategy;


	//
	public Array3D_double(int newXSize, int newYSize, int newZSize) {

		this(newXSize, newYSize, newZSize, new double[newXSize*newYSize*newZSize], IndexStrategy.FortranIndexStrategy.class);
	}

	//
	public Array3D_double(int newXSize, int newYSize, int newZSize, double[] newData) {

		this(newXSize, newYSize, newZSize, newData, IndexStrategy.FortranIndexStrategy.class);
	}

	//
	public Array3D_double(int newXSize, int newYSize, int newZSize, Class<? extends IndexStrategy> strategyClass) {

		this(newXSize, newYSize, newZSize, new double[newXSize*newYSize*newZSize], strategyClass);
	}


	//
	public Array3D_double(int newXSize, int newYSize, int newZSize, double[] newData, Class<? extends IndexStrategy> strategyClass) {

		this.xSize = newXSize;
		this.ySize = newYSize;
		this.zSize = newZSize;

		this.data = newData;

		if( !IndexStrategy.class.isAssignableFrom(strategyClass) ) {
			throw new IllegalArgumentException("index strategy must be a "+javatool.ClassTool.getName(IndexStrategy.class));
		}


		Constructor<? extends IndexStrategy> strategyConstructor = null;
		try {
			strategyConstructor = strategyClass.getConstructor(int.class, int.class, int.class); // simply assume this constructor is indeed available
		}
		catch(java.lang.NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		try {
			this.indexStrategy = strategyConstructor.newInstance(newXSize, newYSize, newZSize);
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
	public void fill(double value) {

		Arrays.fill(this.data, value);
	}


	//
	public double get(int x1, int x2, int x3) {

		return this.data[this.indexStrategy.index(x1, x2, x3)];
	}

	//
	public double[] getData() {

		return this.data;
	}


	//
	public void set(int x1, int x2, int x3, double value) {

		this.data[this.indexStrategy.index(x1, x2, x3)] = value;
	}


	//
	public int getSize() {

		return this.data.length;
	}

	//
	public int getX1Size() {

		return this.xSize;
	}

	//
	public int getX2Size() {

		return this.ySize;
	}

	//
	public int getX3Size() {

		return this.zSize;
	}
}


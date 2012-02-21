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

import java.io.Serializable;
import java.util.Arrays;
import utilities.array3d.IndexStrategy.FortranIndexStrategy;

// TODO: inherit functionality from cern.colt.matrix.DoubleMatrix3D?
/**
3D array backed by a 1D C-style array of primitive type
@author Jan Hegewald
*/
public class Array3D_double implements Serializable {
	private double[] data;
	private final int xSize;
	private final int ySize;
	private final int zSize;
	private IndexStrategy indexStrategy;

	public Array3D_double(int newXSize, int newYSize, int newZSize) {
		this(newXSize, newYSize, newZSize, new double[newXSize*newYSize*newZSize], new FortranIndexStrategy(newXSize, newYSize, newZSize));
	}

	public Array3D_double(int newXSize, int newYSize, int newZSize, double[] newData) {
		this(newXSize, newYSize, newZSize, newData, new FortranIndexStrategy(newXSize, newYSize, newZSize));
	}

	public Array3D_double(int newXSize, int newYSize, int newZSize, IndexStrategy strategy) {
		this(newXSize, newYSize, newZSize, new double[newXSize*newYSize*newZSize], strategy);
	}

	public Array3D_double(int newXSize, int newYSize, int newZSize, double[] newData, IndexStrategy strategy) {
		xSize = newXSize;
		ySize = newYSize;
		zSize = newZSize;

		data = newData;
		
		indexStrategy = strategy;
	}
	
	public void fill(double value) {
		Arrays.fill(data, value);
	}

	public double get(int x1, int x2, int x3) {
		return data[indexStrategy.index(x1, x2, x3)];
	}
	
	public double[] getData() {
		return data;
	}


	public void set(int x1, int x2, int x3, double value) {
		data[indexStrategy.index(x1, x2, x3)] = value;
	}

	public int getSize() {
		return data.length;
	}

	public int getX1Size() {
		return xSize;
	}

	public int getX2Size() {
		return ySize;
	}

	public int getX3Size() {
		return zSize;
	}
}

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

package muscle.util.data.array2d;

import java.io.Serializable;
import muscle.util.data.array2d.IndexStrategy.FortranIndexStrategy;


// TODO: inherit functionality from cern.colt.matrix.DoubleMatrix2D?
/**
2D array backed by a 1D C-style array of primitive type
@author Jan Hegewald
*/
public class Array2D_double implements Serializable {

	private final double[] data;
	private final IndexStrategy indexStrategy;
	
	public Array2D_double(int newXSize, int newYSize) {
		this(new double[newXSize*newYSize], new FortranIndexStrategy(newXSize, newYSize));
	}

	public Array2D_double(int newXSize, int newYSize, double[] newData) {
		this(newData, new FortranIndexStrategy(newXSize, newYSize));
	}

	public Array2D_double(int newXSize, int newYSize, IndexStrategy strategy) {
		this(new double[newXSize*newYSize], strategy);
	}

	public Array2D_double(double[] newData, IndexStrategy strategy) {
		this.data = newData;
		this.indexStrategy = strategy;
	}

	public double get(int x1, int x2) {
		return data[indexStrategy.index(x1, x2)];
	}
	
	public double[] getData() {
		return data;
	}

	public void set(int x1, int x2, double value) {
		data[indexStrategy.index(x1, x2)] = value;
	}

	public int size() {
		return data.length;
	}
}

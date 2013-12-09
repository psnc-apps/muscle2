/*
* Copyright 2008, 2009 Complex Automata Simulation Technique (COAST) consortium
* Copyright 2010-2013 Multiscale Applications on European e-Infrastructures (MAPPER) project
*
* GNU Lesser General Public License
* 
* This file is part of MUSCLE (Multiscale Coupling Library and Environment).
* 
* MUSCLE is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* MUSCLE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public License
* along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/
/*
 * 
 */

package examples.laplace;

import examples.laplace.BoundaryCondition.Direction;

/**
 * custom boundary condition which is using a ghostnode column to
 * synchronize data with the other kernel
 */
public final class CommunicationBoundary extends BoundaryCondition {
	private double[] remoteSlice;
	private final double[] localSlice;
	private double[][] data;
	
	public CommunicationBoundary(Direction dir, int newNx, int newNy, double[][] data) {
		super(dir, newNx, newNy);
		this.data = data;
		// init local slice
		localSlice = new double[ny];
		for (int x = 0; x < nx; x++) {
			for (int y = 0; y < ny; y++) {
				if (applies(x, y)) localSlice[y] = data[x][y];
			}
		}
		// set remote slice to be the same as our local one, just for initialization
		remoteSlice = localSlice;
	}

	public void updateData(double[][] data, double[] slice) {
		this.data = data;
		this.remoteSlice = slice;
	}
	
	public double[] getLocalBoundary() {
		return localSlice;
	}
	
	// synchronize ghostnode column with their corresponding original data
	// calc new boundary value based on the neighbours
	@Override
	public double get(int x, int y, int step) {
		double val;
		if (y == 0 || y == ny - 1) {
			val = data[x][y];
		} else {
			double n = data[x][y + 1];
			double s = data[x][y - 1];
			double w = direction == Direction.West ? remoteSlice[y] : data[x - 1][y];
			double e = direction == Direction.East ? remoteSlice[y] : data[x + 1][y];
			val = (n + e + s + w) / 4.0;
		}
		localSlice[y] = val;
		return val;
	}
}

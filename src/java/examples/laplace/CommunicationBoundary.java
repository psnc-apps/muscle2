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
	private double[] localSlice;
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

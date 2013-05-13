/*
 * 
 */

package examples.laplace;

/**
 * boundary condition which applies to the main area of the grid<br>
 * calculates new value bases on the four neighbour values
 */
public class AreaFunction extends BoundaryCondition {
	private double[][] data;

	public AreaFunction(int newNx, int newNy) {
		super(Direction.Area, newNx, newNy);
	}

	public void updateData(double[][] data) {
		this.data = data;
	}
	
	// calc new value based on the neighbours
	public double get(int x, int y, int step) {
		double n = data[x][y + 1];
		double e = data[x + 1][y];
		double s = data[x][y - 1];
		double w = data[x - 1][y];
		return (n + e + s + w) / 4.0;
	}

}

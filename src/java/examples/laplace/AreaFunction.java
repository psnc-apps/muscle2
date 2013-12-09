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
	@Override
	public double get(int x, int y, int step) {
		double n = data[x][y + 1];
		double e = data[x + 1][y];
		double s = data[x][y - 1];
		double w = data[x - 1][y];
		return (n + e + s + w) / 4.0;
	}

}

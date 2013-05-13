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
package examples.laplace;

/**
 * interface for boundary conditions
 *
 * @author Jan Hegewald
 */
public class BoundaryCondition {
	protected final Direction direction;
	protected final int ny;
	protected final int nx;
	
	public enum Direction {
		East, West, North, South, Any, Area;
	}
	
	public BoundaryCondition(Direction direction, int nx, int ny) {
		this.direction = direction;
		this.nx = nx;
		this.ny = ny;
	}
	
	public boolean applies(int x, int y) {
		switch (direction) {
			case East:
				return x == nx - 1;
			case West:
				return x == 0;
			case North:
				return y == ny - 1;
			case South:
				return y == 0;
			case Area:
				return x > 0 && x < nx - 1 && y > 0 && y < ny - 1;
			default:
				return true;
		}
	}

	public double get(int x, int y, int step) {
		return 0d;
	}
}

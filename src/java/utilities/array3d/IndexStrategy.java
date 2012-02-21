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

/**
allow different index orderings to access items of an array3d
@author Jan Hegewald
*/
public interface IndexStrategy {
	
	int index(int x, int y, int z);
		
	/**
	fortran-style index calculation, use<br>
	for iz in lz<br>
		for iy in ly<br>
			for ix in lx<br>
				array[ix][iy][iz] = ...
	*/
	public static class FortranIndexStrategy implements IndexStrategy {
		private final int xSize;
		private final int ySize;
		
		public FortranIndexStrategy(int newXSize, int newYSize, int newZSize) {
			xSize = newXSize;
			ySize = newYSize;
		}

		public int index(int x, int y, int z) {
			return xSize * (ySize * z + y) + x;
		}
	}


	/**
	C-style index calculation, use<br>
	for ix in lx<br>
		for iy in ly<br>
			for iz in lz<br>
				array[ix][iy][iz] = ...
	*/
	public static class CIndexStrategy implements IndexStrategy {
		private final int ySize;
		private final int zSize;
		
		public CIndexStrategy(int newXSize, int newYSize, int newZSize) {
			ySize = newYSize;
			zSize = newZSize;		
		}

		public int index(int x, int y, int z) {
			return zSize * (ySize * x + y) + z;
		}
	}

}


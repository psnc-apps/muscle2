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


/**
allow different index orderings to access items of an array2d
@author Jan Hegewald
*/
public interface IndexStrategy {
	
	//
	int index(int x, int y);
		

	/**
	fortran-style index calculation, use<br>
	for iy in ly<br>
		for ix in lx<br>
			array[ix][iy] = ...
	*/
	public static class FortranIndexStrategy implements IndexStrategy {

		int xSize;
		int ySize;
		
		public FortranIndexStrategy(int newXSize, int newYSize) {

			xSize = newXSize;
			ySize = newYSize;
		}

		public int index(int x, int y) {

			return xSize * y + x;
		}
	}


	/**
	C-style index calculation, use<br>
	for ix in lx<br>
		for iy in ly<br>
			array[ix][iy] = ...
	*/
	public static class CIndexStrategy implements IndexStrategy {

		int xSize;
		int ySize;
		
		public CIndexStrategy(int newXSize, int newYSize) {

			xSize = newXSize;
			ySize = newYSize;
		}

		public int index(int x, int y) {

			return ySize * x + y;
		}
	}

}


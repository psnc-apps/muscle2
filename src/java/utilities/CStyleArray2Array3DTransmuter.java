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

package utilities;

import utilities.array3d.Array3D_double;

/**
creates an Array3D from a plain c-style array
@author Jan Hegewald
*/
public class CStyleArray2Array3DTransmuter implements Transmutable<double[],Array3D_double> {

	private int xSize;
	private int ySize;
	private int zSize;

	//
	public CStyleArray2Array3DTransmuter(int newXSize, int newYSize, int newZSize) {

		xSize = newXSize;
		ySize = newYSize;
		zSize = newZSize;
	}
	
	
	//
	public Array3D_double transmute(double[] in) {
		
		// get a 3d view of the incomming data
		Array3D_double out = new Array3D_double(xSize, ySize, zSize, in);
		
		return out;
	}

}

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

package muscle.core;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import com.thoughtworks.xstream.XStream;
import muscle.exception.MUSCLERuntimeException;
import muscle.core.CxADescription;


/**
describes local (dicrete) coordinate system of dataset and a viewport<br>
for more functinality the javax.media.j3d.BoundingBox class may be more suitable
@author Jan Hegewald
*/
public class BBox implements java.io.Serializable {

	private int[] origin;
	private int[] bounds;
	
	
	//
	public BBox(int ... values) {  // i.e. x,y,z, w,h,d

		int dims = 0;
		if(values.length == 2) { // 1D
			dims = 1;
		}
		else if(values.length == 4) { // 2D
			dims = 2;
		}
		else if(values.length == 6) { // 3D
			dims = 3;		
		}
		else {
			throw new IllegalArgumentException("unknown number of dimensions: "+values.length+"/2");
		}
		
		origin = new int[dims];
		System.arraycopy(values, 0, origin, 0, dims);
		bounds = new int[dims];
		System.arraycopy(values, dims, bounds, 0, dims);

	}
	
	
}


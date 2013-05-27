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

package examples.triangle;

import muscle.core.ConduitEntrance;
import muscle.core.ConduitExit;

/**
@author Bartosz Bosak
*/
public class Two extends muscle.core.kernel.CAController {
	private ConduitExit<double[]> reader2;
	private ConduitEntrance<double[]> writer2;

	protected void addPortals() {
		reader2 = addExit("data", double[].class);
		writer2 = addEntrance("data", double[].class);
	}

	protected void execute() {		
		while (!willStop()) {
		
			// read from our portals
			double[] dataA = reader2.receive();
						
			// process data
						
			// dump to our portals
			for(int i = 0; i < dataA.length; i++) {
				System.out.println("Got from One: "+dataA[i]);
				dataA[i]++;
			}
			System.out.println("Sendint data to Three");
			writer2.send(dataA);
		}
	}
}

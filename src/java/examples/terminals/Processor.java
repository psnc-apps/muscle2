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

package examples.terminals;

import muscle.core.kernel.Submodel;
import muscle.core.model.Timestamp;

/**
 *
 * @author Joris Borgdorff
 */
public class Processor extends Submodel {
	double[] data;
	
	@Override
	public Timestamp init(Timestamp prevOrigin) {
		data = (double[]) in("initialData").receive();
		return super.init(prevOrigin);
	}
	
	@Override
	public void intermediateObservation() {
		boolean[] isLarge = new boolean[data.length];
		for (int i = 0; i < data.length; i++) {
			isLarge[i] = (data[i] > 2);
		}
		log("Sending data...");
		out("largeMask").send(isLarge);
	}

	@Override
	public void solvingStep() {
		log("Processing data...");
		for (int i = 0; i < data.length; i++) {
			data[i]++;
		}
	}
}

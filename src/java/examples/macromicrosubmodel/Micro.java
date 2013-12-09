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

package examples.macromicrosubmodel;

import muscle.core.kernel.Submodel;
import muscle.core.model.Observation;
import muscle.core.model.Timestamp;

/**
 *
 * @author Joris Borgdorff
 */
public class Micro extends Submodel {
	private double firstValue;
	
	@Override
	protected Timestamp init(Timestamp previousTime) {
		Observation<double[][]> obs = this.<double[][]>in("macroObs").receiveObservation();
		double[][] data = obs.getData();
		System.out.println("Got matrix:");
		System.out.println("(" + data[0][0] + "\t" + data[1][0] + "\t)");
		System.out.println("(" + data[0][1] + "\t" + data[1][1] + "\t)");
		System.out.println();
		firstValue = data[0][0];
		
		// Initialize (t_0) at time of initial message
		return obs.getTimestamp();
	}
	
	
	@Override
	protected void solvingStep() {
		firstValue++;
	}
	
	@Override
	protected void finalObservation() {
		out("microObs").send(firstValue);
	}
	
	@Override
	protected boolean restartSubmodel() {
		return in("macroObs").hasNext();
	}
}

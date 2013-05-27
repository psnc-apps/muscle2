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
package examples.pingpongsubmodel;

import java.io.Serializable;
import muscle.core.kernel.Submodel;
import muscle.core.model.Observation;
import muscle.core.model.Timestamp;

/**
 * Receives at 'in' exit byte array and writes them to 'out' exit
 */
public class Pong extends Submodel {
	private Serializable data;

	protected Timestamp init(Timestamp previousTime) {
		Observation obs = in("in").receiveObservation();
		data = obs.getData();
		return obs.getTimestamp();
	}
	
	protected void finalObservation() {
		out("out").send(data);
	}
	
	protected boolean restartSubmodel() {
		return in("in").hasNext();
	}
}

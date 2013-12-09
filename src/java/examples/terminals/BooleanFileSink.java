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

import java.io.IOException;
import java.io.Writer;
import muscle.core.conduit.terminal.FileSink;
import muscle.core.model.Observation;

/**
 *
 * @author Joris Borgdorff
 */
public class BooleanFileSink extends FileSink<boolean[]> {
	int iteration = 0;
	@Override
	protected String getInfix() {
		return String.valueOf(iteration);
	}
	
	@Override
	protected void write(Writer out, Observation<boolean[]> obs) throws IOException {
		boolean[] data = obs.getData();
		System.out.println("Writing data to <"+ getLocalFile(getInfix()) + ">");
		for (boolean x : data) {
			out.write(x ? "1 " : "0 ");
		}
		iteration++;
	}
}

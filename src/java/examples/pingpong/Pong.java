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
package examples.pingpong;

import muscle.core.ConduitEntrance;
import muscle.core.ConduitExit;
import muscle.core.kernel.CAController;

/**
 * Receives at 'in' exit byte array and writes them to 'out' exit
 */
public class Pong extends CAController {

	private static final long serialVersionUID = 1L;
	private ConduitEntrance<byte[]> entrance;
	private ConduitExit<byte[]> exit;

	@Override
	protected void addPortals() {
		entrance = addEntrance("out", byte[].class);
		exit = addExit("in", byte[].class);

	}

	@Override
	protected void execute() {
		int count = getIntProperty("preparation_steps") + getIntProperty("tests_count")*getIntProperty("same_size_runs")*getIntProperty("steps");
		
		for (int i = 0; i < count; i++) {
			byte[] ba = exit.receive();
			entrance.send(ba);
		}
	}
}

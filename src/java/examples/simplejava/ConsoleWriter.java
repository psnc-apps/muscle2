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

package examples.simplejava;

import muscle.core.ConduitExit;

/**
a simple java example kernel which receives data and prints its content to stdout
@author Jan Hegewald
*/
public class ConsoleWriter extends muscle.core.kernel.CAController {
	private ConduitExit<double[]> readerA;

	protected void addPortals() {	
		readerA = addExit("data", double[].class);
	}

	protected void execute() {
		while (!this.willStop()) {
			// read from our portals
			double[] dataA = readerA.receive();
			
			int size = dataA.length;
			// process data
			for(int i = 0; i < size; i++) {
				doSomething(dataA);
			}
						
			for (int i = 0; i < 5 && i < size; i++) {
				log("got: "+dataA[i]);				
			}

			if (size > 5) {
				log("...");
				log("got: "+dataA[dataA.length - 1]);
			}
			
			log("");
		}
	}

	private void doSomething(double[] dataA) {
		// Do something
	}
}

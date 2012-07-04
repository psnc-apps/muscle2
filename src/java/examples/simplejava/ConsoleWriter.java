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
import muscle.core.Scale;
import muscle.core.model.Distance;

/**
a simple java example kernel which receives data and prints its content to stdout
@author Jan Hegewald
*/
public class ConsoleWriter extends muscle.core.kernel.CAController {
	private ConduitExit<double[]> readerA;

	public Scale getScale() {
		Distance delta = new Distance(1);
		return new Scale(delta,delta);
	}

	protected void addPortals() {	
		readerA = addExit("data", double[].class);
	}

	protected void execute() {
		while (!this.willStop()) {
			// read from our portals
			double[] dataA = readerA.receive();
						
			// process data
			for(int i = 0; i < dataA.length; i++) {
				doSomething(dataA);
			}
						
			// dump to our portals at designated frequency
			// we reduce our maximum available output frequency since it is not needed anywhere in the CxA (could also be done by the drop filter)
			for(int i = 0; i < dataA.length; i++) {
				System.out.println("got: "+dataA[i]);
			}
			System.out.println();
		}
	}

	private void doSomething(double[] dataA) {
		// Do something
	}
}

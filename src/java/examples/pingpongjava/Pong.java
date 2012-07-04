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

package examples.pingpongjava;

import muscle.core.ConduitExit;
import muscle.core.Scale;
import muscle.core.kernel.CAController;
import muscle.core.model.Distance;

/**
a simple java example kernel which receives data and prints its content to stdout
@author Jan Hegewald
*/
public class Pong extends CAController {
	private ConduitExit<double[]> readerA;

	@Override
	public Scale getScale() {
		Distance delta = new Distance(1);
		return new Scale(delta,delta);
	}

	@Override
	protected void addPortals() {
		this.readerA = this.addExit("data", double[].class);
	}

	@Override
	protected void execute() {
		while (!this.willStop()) {

			// read from our portals
			double[] dataA = this.readerA.receive();

			// dump to our portals
			for (double element : dataA) {
				System.out.println("got: "+element);
			}
			System.out.println();
		}
	}
}

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

import muscle.core.ConduitEntrance;

/**
a simple java example kernel which sends data
@author Jan Hegewald
*/
public class Ping extends muscle.core.kernel.CAController {
	private ConduitEntrance<double[]> entrance;

	@Override
	protected void addPortals() {
		this.entrance = this.addEntrance("data", double[].class);
	}

	@Override
	protected void execute() {

		double[] dataA = new double[5];

		while(!this.willStop()) {

			// process data
			for(int i = 0; i < dataA.length; i++) {
				dataA[i] = i;
			}

			// dump to our portals
			this.entrance.send(dataA);
		}
	}

}

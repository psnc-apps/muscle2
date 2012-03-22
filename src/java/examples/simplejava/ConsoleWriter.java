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

import java.math.BigDecimal;
import javax.measure.DecimalMeasure;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import muscle.core.ConduitExit;
import muscle.core.Scale;


/**
a simple java example kernel which receives data and prints its content to stdout
@author Jan Hegewald
*/
public class ConsoleWriter extends muscle.core.kernel.CAController {

	private static final int INTERNAL_DT = 1;

	private ConduitExit<double[]> readerA;

	public muscle.core.Scale getScale() {
		DecimalMeasure<Duration> dt = DecimalMeasure.valueOf(new BigDecimal(1), SI.SECOND);
		DecimalMeasure<Length> dx = DecimalMeasure.valueOf(new BigDecimal(1), SI.METER);
		return new Scale(dt,dx);
	}

	protected void addPortals() {	
		readerA = addExit("data", 1, double[].class);
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

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

package examples.triangle;

import muscle.core.ConduitExit;
import muscle.core.ConduitEntrance;
import muscle.core.Scale;
import muscle.core.kernel.RawKernel;
import java.math.BigDecimal;
import javax.measure.DecimalMeasure;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;


/**
@author Bartosz Bosak
*/
public class Two extends muscle.core.kernel.CAController {

	private static final int INTERNAL_DT = 1;

	// external dt's for out portals
	private static final int DT_READ_A = 1;
	private static final int dtWriteA = 1;

	private ConduitExit<double[]> reader2;
	private ConduitEntrance<double[]> writer2;

	private int time; // cxa time


	//
	public muscle.core.Scale getScale() {
		DecimalMeasure<Duration> dt = DecimalMeasure.valueOf(new BigDecimal(1), SI.SECOND);
		DecimalMeasure<Length> dx = DecimalMeasure.valueOf(new BigDecimal(1), SI.METER);
		return new Scale(dt,dx);
	}


	//
	protected void addPortals() {
	
		reader2 = addExit("data", DT_READ_A, double[].class);
		writer2 = addEntrance("data", 1, double[].class);
	}
		

	//
	protected void execute() {

		startAutomaton();
	}
	
	
	//
	private void startAutomaton() {
	
		double[] dataA = null;
		
		// loop stepping with INTERNAL_DT
		for(time = 0; !willStop(); time += INTERNAL_DT) {
		
			// read from our portals at designated frequency
			if(time % DT_READ_A == 0)
				dataA = reader2.receive();
						
			// process data
			for(int i = 0; i < dataA.length; i++) {
			}
						
			// dump to our portals at designated frequency
			// we reduce our maximum available output frequency since it is not needed anywhere in the CxA (could also be done by the drop filter)
			if(time % dtWriteA == 0) {
				for(int i = 0; i < dataA.length; i++)
				{
					System.out.println("Got from One: "+dataA[i]);
					dataA[i]++;
				}
				System.out.println("Sendint data to Three");
				writer2.send(dataA);
			}
		}
	}

}

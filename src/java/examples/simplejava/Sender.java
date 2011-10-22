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

import muscle.core.ConduitEntrance;
import muscle.core.Scale;
import java.math.BigDecimal;
import javax.measure.DecimalMeasure;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;


/**
a simple java example kernel which sends data
@author Jan Hegewald
*/
public class Sender extends muscle.core.kernel.CAController {

	private ConduitEntrance<double[]> entrance;
	
	private int time;


	//
	public muscle.core.Scale getScale() {
		DecimalMeasure<Duration> dt = DecimalMeasure.valueOf(new BigDecimal(1), SI.SECOND);
		DecimalMeasure<Length> dx = DecimalMeasure.valueOf(new BigDecimal(1), SI.METER);
		return new Scale(dt,dx);
	}


	//
	protected void addPortals() {
	
		entrance = addEntrance("data", 1, double[].class);
	}


	//
	protected void execute() {

		double[] dataA = new double[5];
		
		for(time = 0; !willStop(); time ++) {
								
			// process data
			for(int i = 0; i < dataA.length; i++) {
				dataA[i] = i;
			}
						
			// dump to our portals
			entrance.send(dataA);
		}
	}

}

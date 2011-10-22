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

package muscle.core.conduit.filter;

import muscle.core.wrapper.Observation;

/**
interpolates two adjacent values of the incoming data array
for this filter to work, the incoming data must have one value more than the outgoing data
@author Jan Hegewald
*/
public class LinearInterpolationFilterDouble extends AbstractWrapperFilter<double[],double[]> {
	protected void apply(Observation<double[]> subject) {
		double[] inData = subject.getData();
		double[] outData = new double[inData.length-1];

		// warning: as outWrapper is mutable, a successive filter might change its length
		for (int i = 0; i < outData.length; i++) {			
			outData[i] = ( inData[i] + inData[i+1]) / 2.0;
		}
		
		put(new Observation<double[]>(outData, subject.getTimestamp()));
	}
}


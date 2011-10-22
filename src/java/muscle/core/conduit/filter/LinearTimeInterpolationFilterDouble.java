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
interpolates between current and last timestep<br>
maps coarse time scale to fine time scale, where dt_coarse = 2*dt_fine
dt_coarse-dt_fine must be equal to dt_coarse/2<br>
remember: the count of overall calculated coarse dts should be half as many as calculated fine dts
otherwise this filter can not provide data for the last timestep<br>
dtCoarseCount = (globalStepCount%dt_coarse)-1<br>
dtFineCount = (globalStepCount%dt_fine)-1<br>
dtCoarseCount/2 == dtFineCount<br>
<tt>0���1���2���3 t_coarse<br>
    |��/|��/|��/|<br>
    |�|�|�|�|�|�|<br>
    0�1�2�3�4�5�6 t_fine</tt>
@author Jan Hegewald
*/
public class LinearTimeInterpolationFilterDouble extends AbstractObservationFilter<double[],double[]> {
	private double[] lastCoarseData; // copy of the last used coarse timestep data (inData)
	private final int dtFactor;
		
	public LinearTimeInterpolationFilterDouble(int dtFactor) {
		super();
		// so far only tested with dt_coarse = 2*dt_fine
		assert dtFactor == 2;
		this.dtFactor = dtFactor;
	}
	
	public void apply(Observation<double[]> subject) {
		// init lastCoarseData buffer
		// create our buffer for out data only once
		if(lastCoarseData == null) {
			lastCoarseData = (subject.getData()).clone();

			// feed next filter with data for the current coarse timestep
			put(subject);

			return;
		}

		double[] lastCoarseArray = lastCoarseData;
		double[] currentCoarseArray = subject.getData();
		double[] interpolatedArray = new double[currentCoarseArray.length];
		for(int i = 0; i < lastCoarseArray.length; i++) {
			interpolatedArray[i] = (lastCoarseArray[i] + currentCoarseArray[i]) / 2.0;
		}
		
		// retain the current coarse data to be able to calculate the next timestep
		lastCoarseData = (subject.getData()).clone();
		
		Observation<double[]> interpolatedData = new Observation<double[]>(interpolatedArray, subject.getTimestamp().divide(dtFactor), subject.getNextTimestamp().divide(dtFactor));
		assert interpolatedData.getTimestamp().compareTo(subject.getTimestamp()) != 0;
		
		// feed next filter with interpolated data for last fine timestep
		put(interpolatedData);

		// feed next filter with data for the current coarse timestep
		put(subject);
	}

}


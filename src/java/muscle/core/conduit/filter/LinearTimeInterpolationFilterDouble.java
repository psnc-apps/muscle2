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

import muscle.core.model.Distance;
import muscle.core.model.Observation;
import muscle.core.model.Timestamp;

/**
interpolates between current and last timestep<br>
maps coarse time scale to fine time scale, where dt_coarse = 2*dt_fine
dt_coarse-dt_fine must be equal to dt_coarse/2<br>
remember: the count of overall calculated coarse dts should be half as many as calculated fine dts
otherwise this filter can not provide data for the last timestep<br>
dtCoarseCount = (globalStepCount%dt_coarse)-1<br>
dtFineCount = (globalStepCount%dt_fine)-1<br>
dtCoarseCount/2 == dtFineCount<br>
@author Jan Hegewald
*/
public class LinearTimeInterpolationFilterDouble extends AbstractFilter<double[],double[]> {
	private double[] lastCoarseArray; // copy of the last used coarse timestep data (inData)
	private final int dtFactor;
	private Timestamp lastTime;
		
	public LinearTimeInterpolationFilterDouble(int dtFactor) {
		super();
		// only works with strictly positive dt
		assert dtFactor > 0;
		this.dtFactor = dtFactor;
	}
	
	public void apply(Observation<double[]> subject) {
		double[] data = subject.getData();
		int size = data.length;
		// init lastCoarseData buffer
		// create our buffer for out data only once
		Timestamp currentTime = subject.getTimestamp();
		// We need to copy this so that we can store it for the next iteration
		double[] currentCoarseArray = new double[size];
		System.arraycopy(data, 0, currentCoarseArray, 0, size);
		
		// In the first step, don't interpolate anything
		if(lastCoarseArray != null) {
			Distance dt = lastTime.distance(currentTime).div((double)dtFactor);
			Timestamp interpolatedTime = lastTime.add(dt);
			for (int t = 1; t < dtFactor; t++) {
				double[] interpolatedArray = new double[size];
				for(int i = 0; i < lastCoarseArray.length; i++) {
					interpolatedArray[i] = ((dtFactor - t)*lastCoarseArray[i] + t*currentCoarseArray[i]) / (double)dtFactor;
				}

				Timestamp nextInterpolatedTime = (t + 1 == dtFactor) ? currentTime : interpolatedTime.add(dt);
				Observation<double[]> interpolatedData = new Observation<double[]>(interpolatedArray, interpolatedTime, nextInterpolatedTime, true);
				interpolatedTime = nextInterpolatedTime;
				
				// feed next filter with interpolated data for last fine timestep
				put(interpolatedData);
			}
		}

		// retain the current coarse data to be able to calculate the next timestep
		lastCoarseArray = currentCoarseArray;
		lastTime = currentTime;

		Distance nextDt = currentTime.distance(subject.getNextTimestamp()).div((double)dtFactor);
		// feed next filter with data for the current coarse timestep
		put(subject.copyWithNewTimestamps(currentTime, currentTime.add(nextDt)));
	}
}


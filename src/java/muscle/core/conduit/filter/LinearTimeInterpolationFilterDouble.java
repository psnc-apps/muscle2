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

import java.math.BigDecimal;

import javax.measure.DecimalMeasure;
import javax.measure.quantity.Duration;

import muscle.core.DataTemplate;
import muscle.core.Scale;
import muscle.core.wrapper.DataWrapper;


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
public class LinearTimeInterpolationFilterDouble implements muscle.core.conduit.filter.WrapperFilter<DataWrapper> {

	private double[] lastCoarseData; // copy of the last used coarse timestep data (inData)

	private WrapperFilter childFilter;
	private DecimalMeasure<Duration> fineDt;
	private DataTemplate inTemplate;


	//
	public LinearTimeInterpolationFilterDouble(WrapperFilter newChildFilter, int dtFactor) {

// so far only tested with dt_coarse = 2*dt_fine
assert dtFactor == 2;


		this.childFilter = newChildFilter;
		DataTemplate outTemplate = this.childFilter.getInTemplate();
		Scale outScale = outTemplate.getScale();
		assert outScale != null;
		DecimalMeasure<Duration> inDt = new DecimalMeasure(outScale.getDt().getValue().multiply(new BigDecimal(dtFactor)), outScale.getDt().getUnit());

		this.fineDt = outScale.getDt();

		assert inDt.compareTo(this.fineDt) > 0;

		this.inTemplate = new DataTemplate(outTemplate.getDataClass(), new Scale(inDt, outScale.getAllDx()));
	}


	//
	public DataTemplate getInTemplate() {

		return this.inTemplate;
	}


	//
	public void put(DataWrapper newInData) {

		DataWrapper inData = newInData;

		// init lastCoarseData buffer
		// create our buffer for out data only once
		if(this.lastCoarseData == null) {

			this.lastCoarseData = ((double[])inData.getData()).clone();

			// feed next filter with data for the current coarse timestep
			this.childFilter.put(inData);

			return;
		}

		DataWrapper currentCoarseData = inData;

		double[] lastCoarseArray = this.lastCoarseData;
		double[] currentCoarseArray = (double[])currentCoarseData.getData();
		double[] interpolatedArray = new double[currentCoarseArray.length];
		for(int i = 0; i < lastCoarseArray.length; i++) {
			interpolatedArray[i] = (lastCoarseArray[i] + currentCoarseArray[i]) / 2.0;
		}

		// retain the current coarse data to be able to calculate the next timestep
		this.lastCoarseData = ((double[])currentCoarseData.getData()).clone();

		DecimalMeasure<Duration> t = new DecimalMeasure<Duration>(currentCoarseData.getSITime().getValue().subtract(this.fineDt.to(currentCoarseData.getSITime().getUnit()).getValue()), currentCoarseData.getSITime().getUnit());
		DataWrapper interpolatedData = new DataWrapper(interpolatedArray, t);
		assert interpolatedData.getSITime().compareTo(currentCoarseData.getSITime()) != 0;

		// feed next filter with interpolated data for last fine timestep
		this.childFilter.put(interpolatedData);

		// feed next filter with data for the current coarse timestep
		this.childFilter.put(currentCoarseData);
	}

}


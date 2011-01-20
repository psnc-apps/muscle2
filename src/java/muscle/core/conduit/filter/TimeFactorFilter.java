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

import javatool.DecimalMeasureTool;

import javax.measure.DecimalMeasure;
import javax.measure.quantity.Duration;

import muscle.core.DataTemplate;
import muscle.core.Scale;
import muscle.core.wrapper.DataWrapper;


/**
modifies timestep with a given factor
@author Jan Hegewald
*/
public class TimeFactorFilter implements muscle.core.conduit.filter.WrapperFilter<DataWrapper> {

	private DataTemplate inTemplate;
	private WrapperFilter childFilter;
	int factor;

	//
	public TimeFactorFilter(WrapperFilter newChildFilter, int newFactor) {

		this.childFilter = newChildFilter;

		this.factor = newFactor;

		DataTemplate outTemplate = this.childFilter.getInTemplate();
		Scale outScale = outTemplate.getScale();
		assert outScale != null;
		DecimalMeasure<Duration> inDt = new DecimalMeasure(outScale.getDt().getValue().multiply(new BigDecimal(this.factor)), outScale.getDt().getUnit());

		this.inTemplate = new DataTemplate(outTemplate.getDataClass(), new Scale(inDt, outScale.getAllDx()));
	}


	//
	public DataTemplate getInTemplate() {

		return this.inTemplate;
	}


	//
	public void put(DataWrapper newInData) {

		DataWrapper inData = newInData;
		this.childFilter.put(new DataWrapper(inData.getData(), DecimalMeasureTool.multiply(inData.getSITime(), new BigDecimal(this.factor))));
	}

}


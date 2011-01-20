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
import javax.measure.unit.SI;

import muscle.core.DataTemplate;
import muscle.core.wrapper.DataWrapper;


/**
modifies timestep with a given offset
@author Jan Hegewald
*/
public class TimeOffsetFilter implements muscle.core.conduit.filter.WrapperFilter<DataWrapper> {

	private DataTemplate inTemplate;
	private WrapperFilter childFilter;
	DecimalMeasure<Duration> offset;

	/**
	offset in seconds
	*/
	public TimeOffsetFilter(WrapperFilter newChildFilter, int newOffset) {

		this.childFilter = newChildFilter;

		this.offset = new DecimalMeasure<Duration>(new BigDecimal(newOffset), SI.SECOND);

		DataTemplate outTemplate = this.childFilter.getInTemplate();
		this.inTemplate = outTemplate;
	}


	//
	public DataTemplate getInTemplate() {

		return this.inTemplate;
	}


	//
	public void put(DataWrapper inData) {

		this.childFilter.put(new DataWrapper(inData.getData(), DecimalMeasureTool.add(inData.getSITime(), this.offset)));
	}

}


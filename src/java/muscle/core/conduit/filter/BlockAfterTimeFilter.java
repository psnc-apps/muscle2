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
import javax.measure.unit.SI;

import muscle.core.DataTemplate;
import muscle.core.wrapper.DataWrapper;


/**
ignores data after a given timestep, only recommended for debugging purposes
@author Jan Hegewald
*/
public class BlockAfterTimeFilter implements muscle.core.conduit.filter.WrapperFilter<DataWrapper> {

	private DataTemplate inTemplate;
	private WrapperFilter childFilter;
	DecimalMeasure<Duration> maxTime;

	//
	public BlockAfterTimeFilter(WrapperFilter newChildFilter, int newMaxTime/*in seconds*/) {

		this.childFilter = newChildFilter;

		this.maxTime = new DecimalMeasure(new BigDecimal(newMaxTime), SI.SECOND);

		DataTemplate outTemplate = this.childFilter.getInTemplate();
		this.inTemplate = outTemplate;
	}


	//
	public DataTemplate getInTemplate() {

		return this.inTemplate;
	}


	//
	public void put(DataWrapper newInData) {

		if(newInData.getSITime().compareTo(this.maxTime) < 1) {
			this.childFilter.put(newInData);
		} else {
			System.out.println("warning: blocking data for time <"+newInData.getSITime()+">");
		}
	}

}


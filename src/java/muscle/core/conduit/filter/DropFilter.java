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

import muscle.core.wrapper.DataWrapper;
import muscle.core.DataTemplate;
import muscle.core.Scale;
import javax.measure.DecimalMeasure;
import javax.measure.unit.SI;
import javax.measure.quantity.Duration;
import java.math.BigDecimal;


/**
drops data if incoming time scale is not a multiple of outgoing dt, newInDt is only required to build the corresponding DataTemplate for incomming data
use for testing, usually better try to not send the dropped data at all from within the CA
@author Jan Hegewald
*/
public class DropFilter implements muscle.core.conduit.filter.WrapperFilter<DataWrapper> {

	private DataTemplate inTemplate;
	private WrapperFilter childFilter;
	DecimalMeasure<Duration> outDt;
	int counter;
	int outRate;

	//
	public DropFilter(WrapperFilter newChildFilter, int newInDtSec/*assume dt in seconds here*/) {
	
		childFilter = newChildFilter;
		
		outRate = newInDtSec;
		DecimalMeasure<Duration> inDt = DecimalMeasure.valueOf(new BigDecimal(newInDtSec), SI.SECOND);
		DataTemplate outTemplate = childFilter.getInTemplate();
		Scale outScale = outTemplate.getScale();
		assert outScale != null;
		outDt = outTemplate.getScale().getDt();

		inTemplate = new DataTemplate(outTemplate.getDataClass(), new Scale(inDt, outScale.getAllDx()));
	}


	//
	public DataTemplate getInTemplate() {
	
		return inTemplate;
	}


	//	
	public void put(DataWrapper newInData) {
		
		// only pass data to next filter on given interval
		if(counter % outRate == 0)
			childFilter.put(newInData);
		
		counter ++;
	}

}


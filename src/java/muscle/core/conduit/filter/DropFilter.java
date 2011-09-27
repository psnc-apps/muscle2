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
public class DropFilter extends AbstractWrapperFilter {
	private final int outRate;
	private int counter;
	
	/** @param newInDtSec in seconds */
	public DropFilter(int newInDtSec) {
		super();
		outRate = newInDtSec;
	}
	
	protected void setInTemplate(DataTemplate consumerTemplate) {
		DecimalMeasure<Duration> inDt = DecimalMeasure.valueOf(new BigDecimal(outRate), SI.SECOND);
		Scale outScale = consumerTemplate.getScale();
		assert outScale != null;
		
		this.inTemplate = new DataTemplate(consumerTemplate.getDataClass(), new Scale(inDt, outScale.getAllDx()));
	}

	protected void apply(DataWrapper subject) {
		if(counter % outRate == 0)
			put(subject);
		
		counter++;
	}
}


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
import javax.measure.unit.SI;
import javax.measure.DecimalMeasure;
import javax.measure.quantity.Duration;
import java.math.BigDecimal;


/**
ignores data after a given timestep, only recommended for debugging purposes
@author Jan Hegewald
*/
public class BlockAfterTimeFilter extends AbstractWrapperFilter {
	private final DecimalMeasure<Duration> maxTime;

	/** @param newMaxTime seconds after which the filter blocks */
	public BlockAfterTimeFilter(int newMaxTime) {
		super();
		maxTime = new DecimalMeasure<Duration>(new BigDecimal(newMaxTime), SI.SECOND);
	}

	protected void apply(DataWrapper subject) {
		if(subject.getSITime().compareTo(maxTime) < 1)
			put(subject);
		else
			System.out.println("warning: blocking data for time <"+subject.getSITime()+">");
	}
}


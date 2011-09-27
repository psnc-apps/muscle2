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
import javatool.DecimalMeasureTool;
import javax.measure.quantity.Duration;
import javax.measure.unit.SI;
import javax.measure.DecimalMeasure;
import java.math.BigDecimal;

/**
modifies timestep with a given offset
@author Jan Hegewald
*/
public class TimeOffsetFilter extends AbstractWrapperFilter {
	DecimalMeasure<Duration> offset;

	/** @param newOffset offset in seconds */
	public TimeOffsetFilter(int newOffset) {
		super();
		offset = new DecimalMeasure<Duration>(new BigDecimal(newOffset), SI.SECOND);
	}

	protected void apply(DataWrapper subject) {
		put(new DataWrapper(subject.getData(), DecimalMeasureTool.add(subject.getSITime(), offset)));
	}
}

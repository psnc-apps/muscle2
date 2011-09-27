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
import javax.measure.quantity.Duration;
import java.math.BigDecimal;
import javatool.DecimalMeasureTool;


/**
modifies timestep with a given factor
@author Jan Hegewald
*/
public class TimeFactorFilter extends AbstractWrapperFilter {
	private final int factor;

	public TimeFactorFilter(int newFactor) {
		super();
		factor = newFactor;
	}
	
	protected void setInTemplate(DataTemplate consumerTemplate) {
		Scale outScale = consumerTemplate.getScale();
		assert outScale != null;
		DecimalMeasure<Duration> inDt = new DecimalMeasure(outScale.getDt().getValue().multiply(new BigDecimal(factor)), outScale.getDt().getUnit());

		this.inTemplate = new DataTemplate(consumerTemplate.getDataClass(), new Scale(inDt, outScale.getAllDx()));
	}

	protected void apply(DataWrapper subject) {		
		put(new DataWrapper(subject.getData(), DecimalMeasureTool.multiply(subject.getSITime(), new BigDecimal(factor))));
	}
}

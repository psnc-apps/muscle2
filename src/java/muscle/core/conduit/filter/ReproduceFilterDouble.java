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
duplicates input data of a coarse delta t and reproduces this data multiple times for a fine time scale
@author Jan Hegewald
*/
public class ReproduceFilterDouble extends AbstractWrapperFilter {
	private DecimalMeasure<Duration> outDt;
	private final int outCount;

	public ReproduceFilterDouble(int newFactor) {
		super();
		this.outCount = newFactor;
	}
	
	protected void setInTemplate(DataTemplate consumerTemplate) {
		Scale outScale = consumerTemplate.getScale();
		assert outScale != null;
		
		this.outDt = outScale.getDt();
		DecimalMeasure<Duration> inDt = new DecimalMeasure<Duration>(outDt.getValue().multiply(new BigDecimal(this.outCount)), this.outDt.getUnit());
		
		inTemplate = new DataTemplate(consumerTemplate.getDataClass(), new Scale(inDt, outScale.getAllDx()));
	}

	protected void apply(DataWrapper subject) {
		
		DataWrapper inData = subject;
		// send clones from the unmodified inData
		for(int i = 0; i < outCount-1; i++) {		
			double[] inArray = (double[])inData.getData();
			double[] outArray = new double[inArray.length];
			System.arraycopy(inArray, 0, outArray, 0, inArray.length);
			
			DecimalMeasure<Duration> dt = DecimalMeasureTool.multiply(outDt, new BigDecimal(i));
			DataWrapper outData = new DataWrapper(outArray, DecimalMeasureTool.add(inData.getSITime(), dt));
			put(outData);
		}

		// send the inData itself as our last output instead of another copy
		double[] inArray = (double[])inData.getData();
		double[] outArray = new double[inArray.length];
		System.arraycopy(inArray, 0, outArray, 0, inArray.length);
		
		//DataWrapper outData = new DataWrapper(outArray, inData.getTimestep()+(outCount-1)*outDt);
		DecimalMeasure<Duration> dt = DecimalMeasureTool.multiply(outDt, new BigDecimal(outCount-1));
		DataWrapper outData = new DataWrapper(outArray, DecimalMeasureTool.add(inData.getSITime(), dt));
		put(outData);
	}
}


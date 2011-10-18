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
import javatool.DecimalMeasureTool;


/**
duplicates input data of a coarse delta t and reproduces this data multiple times for a fine time scale
@author Jan Hegewald
*/
public class ReproduceFilterDouble implements muscle.core.conduit.filter.WrapperFilter<DataWrapper> {

	private DataTemplate inTemplate;
	private DataTemplate outTemplate;
	private WrapperFilter childFilter;
	int outCount;


	//
	public ReproduceFilterDouble(WrapperFilter newChildFilter, int newFactor/*factor for output frequency*/) {
	
		childFilter = newChildFilter;

		outTemplate = childFilter.getInTemplate();
		Scale outScale = outTemplate.getScale();
		assert outScale != null;
		
		DecimalMeasure<Duration> inDt = new DecimalMeasure(outScale.getDt().getValue().multiply(new BigDecimal(newFactor)), outScale.getDt().getUnit());
		
		outCount = newFactor;
		
		inTemplate = new DataTemplate(outTemplate.getDataClass(), new Scale(inDt, outScale.getAllDx()));
	}


	//
	public DataTemplate getInTemplate() {
	
		return inTemplate;
	}


	// pass to next filter at a higher frequency
	public void put(DataWrapper newInData) {
		
		DataWrapper inData = newInData;
		// send clones from the unmodified inData
		for(int i = 0; i < outCount-1; i++) {		
			double[] inArray = (double[])inData.getData();
			double[] outArray = new double[inArray.length];
			System.arraycopy(inArray, 0, outArray, 0, inArray.length);
			
			DecimalMeasure<Duration> dt = DecimalMeasureTool.multiply(outTemplate.getScale().getDt(), new BigDecimal(i));
			DataWrapper outData = new DataWrapper(outArray, DecimalMeasureTool.add(inData.getSITime(), dt));
			childFilter.put(outData);
		}

		// send the inData itself as our last output instead of another copy
		double[] inArray = (double[])inData.getData();
		double[] outArray = new double[inArray.length];
		System.arraycopy(inArray, 0, outArray, 0, inArray.length);
		
//DataWrapper outData = new DataWrapper(outArray, inData.getTimestep()+(outCount-1)*outDt);
		DecimalMeasure<Duration> dt = DecimalMeasureTool.multiply(outTemplate.getScale().getDt(), new BigDecimal(outCount-1));
		DataWrapper outData = new DataWrapper(outArray, DecimalMeasureTool.add(inData.getSITime(), dt));
		childFilter.put(outData);
	}

}


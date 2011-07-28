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

import muscle.core.DataTemplate;
import muscle.core.Scale;
import muscle.core.wrapper.DataWrapper;
import muscle.core.DataTemplate;
import com.thoughtworks.xstream.XStream;
import muscle.exception.MUSCLERuntimeException;


/**
interpolates two adjacent values of the incoming data array
for this filter to work, the incomming data must have one value more than the outgoing data
@author Jan Hegewald
*/
public class LinearInterpolationFilterDouble implements muscle.core.conduit.filter.WrapperFilter<DataWrapper> {

	private WrapperFilter childFilter;
	private DataTemplate inTemplate;

	
	//
	public LinearInterpolationFilterDouble(WrapperFilter newChildFilter) {
		
		childFilter = newChildFilter;
		DataTemplate outTemplate = childFilter.getInTemplate();

		inTemplate = new DataTemplate(outTemplate.getDataClass(), outTemplate.getScale());				
	}
	
	
	//
	public DataTemplate getInTemplate() {
	
		return inTemplate;
	}


	//	
	public void put(DataWrapper newInData) {
		
		double[] inData = (double[])newInData.getData();
		double[] outData = new double[inData.length-1];


// warning: as outWrapper is mutable, a successive filter might change its length

		for (int i = 0; i < outData.length; i++) {			
			outData[i] = ( inData[i] + inData[i+1]) / 2.0;
		}
		
		DataWrapper outWrapper = new DataWrapper(outData, newInData.getSITime());
		childFilter.put(outWrapper);
	}

}


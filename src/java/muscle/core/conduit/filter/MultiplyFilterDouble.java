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


/**
multiplies every value of incomming data with a constant factor
@author Jan Hegewald
*/
public class MultiplyFilterDouble implements muscle.core.conduit.filter.WrapperFilter<DataWrapper> {

	private double factor;
	private DataTemplate inTemplate;
	private WrapperFilter childFilter;

	
	//
	public MultiplyFilterDouble(WrapperFilter newChildFilter, double newFactor) {

		childFilter = newChildFilter;
		inTemplate = childFilter.getInTemplate();
		factor = newFactor;
	}
	

	//
	public DataTemplate getInTemplate() {
	
		return inTemplate;
	}
	
	
	//	
	public void put(DataWrapper newInData) {
		
		double[] inData = (double[])newInData.getData();

		for (int i = 0; i < inData.length; i++) {			
			inData[i] *= factor;
		}		

		childFilter.put(newInData);
	}
	
}


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
import muscle.core.wrapper.DataWrapper;

import com.thoughtworks.xstream.XStream;


/**
entry point for a filter chain used within conduits
@author Jan Hegewald
*/
public class WrapperFilterHead implements muscle.core.conduit.filter.WrapperFilter<DataWrapper> {

	WrapperFilter childFilter;
	DataTemplate template;
	
	
	//
	public WrapperFilterHead(WrapperFilter newChildFilter, DataTemplate newTemplate) throws muscle.exception.DataTemplateMismatchException {
	
		childFilter = newChildFilter;
		template = newTemplate;
		
		if( !DataTemplate.match(template, childFilter.getInTemplate()) ) {			
			throw new muscle.exception.DataTemplateMismatchException(template.toString()+" vs. "+childFilter.getInTemplate().toString());
		}
	}
	
	
	// feed filter chain with data
	public void put(DataWrapper inData) {

		// feed filter chain
		childFilter.put(inData);
	}
	
	
	//
	public DataTemplate getInTemplate() {
	
		return template;
	}
}


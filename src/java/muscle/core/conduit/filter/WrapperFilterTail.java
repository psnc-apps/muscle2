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


/**
exit of a filter chain used within conduits
@author Jan Hegewald
*/
abstract public class WrapperFilterTail implements muscle.core.conduit.filter.WrapperFilter<DataWrapper> {

	DataTemplate template;


	//
	public WrapperFilterTail(DataTemplate newTemplate) {

		this.template = newTemplate;
	}


	/**
	end of filter chain
	overwrite this method to further process data
	*/
	abstract public void result(DataWrapper resultData);


	//
	public void put(DataWrapper inData) {

		this.result(inData);
	}


	//
	public DataTemplate getInTemplate() {

		return this.template;
	}
}


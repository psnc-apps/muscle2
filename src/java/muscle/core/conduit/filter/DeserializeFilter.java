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
import utilities.MiscTool;


/**
deserialize a byte array
@author Jan Hegewald
*/
public class DeserializeFilter implements Filter<byte[]> {

	private Filter childFilter;


	//
	public DeserializeFilter(Filter newChildFilter) {

		this.childFilter = newChildFilter;
	}


	//
	public void put(byte[] subject) {

		Object out = MiscTool.deserialize(subject);

		this.childFilter.put(out);
	}


	public DataTemplate getInTemplate() {
		// TODO Auto-generated method stub
		return null;
	}

}


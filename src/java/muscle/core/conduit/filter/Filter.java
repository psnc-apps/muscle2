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

//import muscle.core.DataTemplate;


/**
interface for conduit filters
*/
public interface Filter<E> {

	/**
	usually modify data here and pass to next filter/device)
	note: usually the filters shoult try to modify the data in place, so do not cache the pointer to the data passed to the next filter, as its contents might be changed
	*/
	public void put(E subject);

	/**
	description of incomming data. most filters try to generate this at runtime from the in-template of their successive filter
	*/
	//public DataTemplate getInTemplate();
}


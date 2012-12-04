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

import java.io.Serializable;
import muscle.core.model.Observation;

/**
interface for conduit filters
*/
public interface Filter<E extends Serializable,F extends Serializable> {
	/** Set the queue containing the incoming messages.
	 * 
	 * Unless apply() is called after a message has been added to the queue, it is not guaranteed to be read.
	 */
	public void queue(Observation<E> obs);
	
	/** Indicate that the incoming queue is non-empty. */
	public void apply();
	
	public void setNextFilter(Filter<F,?> filter);
	
	/** Whether the filter is currently processing something. */
	public boolean isProcessing();
}


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

import java.util.Queue;

/**
exit of a filter chain used within conduits
@author Jan Hegewald
*/
public abstract class FilterTail<E> implements QueueConsumer<E> {
	private Queue<E> incomingQueue;
	
	/**
	end of filter chain
	overwrite this method to further process data
	*/
	public abstract void result(E resultData);

	public void setIncomingQueue(Queue<E> queue) {
		this.incomingQueue = queue;
	}

	public void apply() {
		while (!this.incomingQueue.isEmpty()) {
			this.result(this.incomingQueue.remove());
		}
	}
}

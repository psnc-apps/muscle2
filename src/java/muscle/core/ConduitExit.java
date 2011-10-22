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

package muscle.core;

import java.util.concurrent.BlockingQueue;

/**
this is the (remote) tail of a conduit,
an exit receives data from the conduit agent
@author Jan Hegewald
*/
public class ConduitExit<T> { // generic T will be the underlying unwrapped data, e.g. double[]
	private final BlockingQueue<T> queue;

	public ConduitExit(ConduitExitController<T> control) {
		this.queue = control.getQueue();
	}

	/**
	return our unwrapped data once, or null if the model has to stop computing
	*/
	public T receive() {
		try {
			return this.queue.take();
		} catch (InterruptedException ex) {
			return null;
		}
	}
}

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

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.messaging.Observation;

/**
this is the (remote) tail of a conduit,
an exit receives data from the conduit agent
@author Jan Hegewald
*/
public class ConduitExit<T extends Serializable> { // generic T will be the underlying unwrapped data, e.g. double[]
	private final BlockingQueue<Observation<T>> queue;
	private final ConduitExitController<T> controller;
	private final static Logger logger = Logger.getLogger(ConduitExit.class.getName());

	public ConduitExit(ConduitExitController<T> control) {
		this.queue = control.getQueue();
		this.controller = control;
	}

	/**
	 * Receive one piece of data. Returns null if the model should stop computing.
	*/
	public T receive() {
		try {
			Observation<T> obs = this.queue.take();
			
			if (obs == null)
				return null;
			
			// Update the willStop timestamp only when the message is received by the Instance.
			controller.setNextTimestamp(obs.getNextTimestamp());
			return obs.getData();
		} catch (InterruptedException ex) {
			logger.log(Level.WARNING, "Receiving message interrupted.", ex);
			return null;
		}
	}
}

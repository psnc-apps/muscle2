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
import muscle.exception.MUSCLEConduitExhaustedException;

/**
 * A ConduitExit outputs messages that are sent over a conduit.
 * Messages are accessed over the blocking receive() function, which returns only messages of a single type.
 * When no more messages will be sent over the conduit, due to the conduit stopping,
 * hasNext() will return false and receive() will throw an exception.
 * It is not thread-safe, in the sense that only a single thread may call hasNext() and receive().
 * 
 * @author Joris Borgdorff
*/
public class ConduitExit<T extends Serializable> { // generic T will be the underlying unwrapped data, e.g. double[]
	private final BlockingQueue<Observation<T>> queue;
	private final ConduitExitController<T> controller;
	private final static Logger logger = Logger.getLogger(ConduitExit.class.getName());
	private volatile boolean isDone;
	private Observation<T> nextElem;

	public ConduitExit(ConduitExitController<T> control) {
		this.queue = control.getQueue();
		this.controller = control;
		this.isDone = false;
		this.nextElem = null;
	}

	/**
	 * Whether there will be a next piece of data. It is a blocking function,
	 * waiting for the next message or a signal that no more messages will come.
	 * If the sending end has stopped, or the current submodel is stopping, the
	 * result is false. As long as receive() is not called, subsequent
	 * calls to hasNext() return the same result. After hasNext() returns true, the subsequent call
	 * to receive() is guaranteed to return a result. Conversely, if hasNext() returns false, the
	 * subsequent call to receive() will throw a MUSCLEConduitExhaustedException.
	 */
	public boolean hasNext() {
		// As long as receive() is not called, return true.
		if (this.nextElem != null) return true;
		if (this.isDone) return false;
		try {
			this.nextElem = this.queue.take();
		} catch (InterruptedException ex) {
			logger.log(Level.WARNING, "Receiving message interrupted.", ex);
			this.isDone = true;
		}
		if (this.isDone) this.nextElem = null;
		else if (this.nextElem == null) this.isDone = true;
		
		return this.nextElem != null;
	}
	
	/**
	 * Receive one piece of data.
	 * The call is blocking, meaning that it won't return until data is received. Data returned does not need to be copied.
	 * 
	 * @throws MUSCLEConduitExhaustedException if hasNext would produce false.
	 * @return a piece of data
	 */
	public T receive() {
		if (!hasNext()) {
			throw new MUSCLEConduitExhaustedException("This submodel is stopping; its conduit can no longer be used.");
		}
		
		// Update the willStop timestamp only when the message is received by the Instance.
		this.controller.setNextTimestamp(this.nextElem.getNextTimestamp());
		T data = this.nextElem.getData();
		this.nextElem = null;
		return data;
	}
	
	void dispose() {
		this.isDone = true;
	}
}

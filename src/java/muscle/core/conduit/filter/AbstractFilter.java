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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import muscle.core.model.Observation;

/**
@author Joris Borgdorff
*/
public abstract class AbstractFilter<E extends Serializable,F extends Serializable> implements Filter<E,F> {
	protected BlockingQueue<Observation<E>> incomingQueue;
	protected Filter<F,?> nextFilter;
	private final static boolean applyDirect = Boolean.valueOf(System.getProperty("muscle.core.conduit.filter.applydirect", "true"));
	
	protected AbstractFilter() {
		if (!applyDirect) {
			incomingQueue = new LinkedBlockingQueue<Observation<E>>();
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void apply() {
		if (!applyDirect) {
			while (!incomingQueue.isEmpty()) {
				Observation<E> message = incomingQueue.remove();
				if (message != null) {
					if (message.hasNull()) {
						put((Observation<F>)message);
					} else {
						this.apply(message);
					}
				}
			}
		}
		nextFilter.apply();
	}
	
	@Override
	public void queue(Observation<E> obs) {
		if (applyDirect) {
			apply(obs);
		} else {
			this.incomingQueue.add(obs);
		}
	}
	
	/**
	 * Put a message intended for the next filter.
	 * @param message outgoing observation
	 */
	protected final void put(Observation<F> message) {
		this.nextFilter.queue(message);
	}
	
	/**
	 * Apply the filter to a single message.
	 *
	 *  To pass the modified message on, call put(F message).
	 */
	protected abstract void apply(Observation<E> subject);

	@Override
	public void setNextFilter(Filter<F,?> qc) {
		this.nextFilter = qc;
	}
	
	public synchronized boolean isProcessing() {
		return nextFilter.isProcessing();
	}
}

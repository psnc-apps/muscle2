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
import java.util.concurrent.LinkedBlockingQueue;

/**
@author Jan Hegewald
*/
public abstract class AbstractFilter<E,F> implements Filter<E,F> {
	protected Queue<E> incomingQueue;
	protected final Queue<F> outgoingQueue;
	protected QueueConsumer<F> consumer;
	
	protected AbstractFilter() {
		this.outgoingQueue = new LinkedBlockingQueue<F>();
	}
	
	public AbstractFilter(QueueConsumer<F> qc) {
		this();
		this.consumer = qc;
		this.consumer.setIncomingQueue(this.outgoingQueue);
	}
	
	public void apply() {
		if (incomingQueue == null) return;
		
		while (!incomingQueue.isEmpty()) {
			E message = incomingQueue.remove();
			if (message != null) {
				this.apply(message);
			}
		}
		
		consumer.apply();
	}
	
	protected void put(F message) {
		this.outgoingQueue.add(message);
	}
	
	/**
	 * Apply the filter to a single message.
	 *
	 *  To pass the modified message on, call put(F message).
	 */
	protected abstract void apply(E subject);

	public void setQueueConsumer(QueueConsumer<F> qc) {
		this.consumer = qc;
		this.consumer.setIncomingQueue(this.outgoingQueue);
	}

	public void setIncomingQueue(Queue<E> queue) {
		this.incomingQueue = queue;
	}
}

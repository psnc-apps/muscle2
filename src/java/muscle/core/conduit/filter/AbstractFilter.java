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
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.DataTemplate;
import muscle.core.model.Observation;
import muscle.util.data.SingleProducerConsumerBlockingQueue;

/**
@author Joris Borgdorff
*/
public abstract class AbstractFilter<E extends Serializable,F extends Serializable> implements Filter<E,F> {
	protected BlockingQueue<Observation<E>> incomingQueue;
	protected final BlockingQueue<Observation<F>> outgoingQueue;
	protected QueueConsumer<F> consumer;
	protected DataTemplate<E> inTemplate;
	
	protected AbstractFilter() {
		this.outgoingQueue = new SingleProducerConsumerBlockingQueue<Observation<F>>();
	}
	
	public AbstractFilter(QueueConsumer<F> qc) {
		this();
		this.consumer = qc;
		this.consumer.setIncomingQueue(this.outgoingQueue);
	}
	
	public void apply() {
		if (incomingQueue == null) return;
		while (!incomingQueue.isEmpty()) {
			Observation<E> message = incomingQueue.remove();
			if (message != null) {
				this.apply(message);
			}
		}
		consumer.apply();
	}
	
	protected void put(Observation<F> message) {
		try {
			this.outgoingQueue.put(message);
		} catch (InterruptedException ex) {
			Logger.getLogger(AbstractFilter.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	/**
	 * Apply the filter to a single message.
	 *
	 *  To pass the modified message on, call put(F message).
	 */
	protected abstract void apply(Observation<E> subject);

	public void setQueueConsumer(QueueConsumer<F> qc) {
		this.consumer = qc;
		this.consumer.setIncomingQueue(this.outgoingQueue);
		this.setInTemplate(this.consumer.getInTemplate());
	}
	
	public void setIncomingQueue(BlockingQueue<Observation<E>> queue) {
		this.incomingQueue = queue;
	}
	
	/** Sets the expected DataTemplate, based on the template of the consumer of this filter.
	 * Override if the DataTemplate is altered by using this filter.
	 */
	protected void setInTemplate(DataTemplate<F> consumerTemplate) {
		this.inTemplate = (DataTemplate<E>)consumerTemplate;		
	}
	
	public DataTemplate<E> getInTemplate() {
		return this.inTemplate;
	}
}

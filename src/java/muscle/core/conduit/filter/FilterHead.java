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

import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.util.data.SingleProducerConsumerBlockingQueue;

/**
entry point for a filter chain used within conduits
@author Jan Hegewald
*/
public class FilterHead<F> implements QueueProducer<F> {

	private QueueConsumer<F> consumer;
	private final BlockingQueue<F> outgoingQueue;
	
	public FilterHead(QueueConsumer<F> qc) {
		 outgoingQueue = new SingleProducerConsumerBlockingQueue<F>();
		 consumer = qc;
		 consumer.setIncomingQueue(outgoingQueue);
	}

	// feed filter chain with data
	public void put(F inData) {
		try {
			// feed filter chain
			outgoingQueue.put(inData);
			consumer.apply();
		} catch (InterruptedException ex) {
			Logger.getLogger(FilterHead.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public void setQueueConsumer(QueueConsumer<F> qc) {
		consumer = qc;
		consumer.setIncomingQueue(outgoingQueue);
	}
}
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.core.conduit.filter;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Joris Borgdorff
 */
public class ThreadedFilterHead<F> extends Thread implements QueueProducer<F> {
	private QueueConsumer<F> consumer;
	private final Queue<F> outgoingQueue;
	private boolean apply;
	private boolean isDone;
	
	public ThreadedFilterHead(QueueConsumer<F> consumer) {
		this.outgoingQueue = new LinkedBlockingQueue<F>();
		this.setQueueConsumer(consumer);
		this.isDone = false;
		this.apply = false;
	}
	
	public void put(F data) {
		outgoingQueue.add(data);
		apply();
	}
	
	public void run() {
		while (!isDone) {
			applyNext();
		}
	}
	
	private synchronized void apply() {
		this.apply = true;
		this.notify();
	}
	
	private synchronized void applyNext() {
		while (!apply && !isDone) {
			try {
				this.wait();
			} catch (InterruptedException ex) {
				Logger.getLogger(ThreadedFilterHead.class.getName()).log(Level.SEVERE, "Filter head interrupted", ex);
			}
		}

		if (this.apply) {
			this.consumer.apply();
			this.apply = false;
		}
	}

	public final void setQueueConsumer(QueueConsumer<F> qc) {
		if (this.consumer != null) {
			this.consumer.setIncomingQueue(null);
		}
		this.consumer = qc;
		qc.setIncomingQueue(outgoingQueue);
		qc.apply();
	}
}

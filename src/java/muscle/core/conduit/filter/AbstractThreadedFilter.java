/*
 * 
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
public abstract class AbstractThreadedFilter<E,F> extends Thread implements Filter<E,F> {
	protected Queue<E> incomingQueue;
	protected final Queue<F> outgoingQueue;
	protected QueueConsumer<F> consumer;
	private boolean isDone;
	private boolean apply;
	
	protected AbstractThreadedFilter() {
		this.outgoingQueue = new LinkedBlockingQueue<F>();
		this.isDone = false;
	}
	
	public AbstractThreadedFilter(QueueConsumer<F> qc) {
		this();
		this.consumer = qc;
		this.consumer.setIncomingQueue(this.outgoingQueue);
	}
	
	public void setDone() {
		this.isDone = true; 
	}
	
	public void run() {
		while (!isDone) {
			if (!apply) {
				waitOnApply();
			}
			this.apply = false;
			consumeQueue();
		}
	}
	
	private synchronized void waitOnApply() {
		while (!apply && !isDone) {
			try {
				wait();
			} catch (InterruptedException ex) {
				Logger.getLogger(AbstractThreadedFilter.class.getName()).log(Level.SEVERE, "Filter was interrupted", ex);
			}
		}
	}
	
	protected void consumeQueue() {
		if (incomingQueue == null) return;

		while (!incomingQueue.isEmpty()) {
			E message = incomingQueue.remove();
			if (message != null) {
				this.apply(message);
			}
		}

		consumer.apply();
	}
	
	public synchronized void apply() {
		this.apply = true;
		this.notifyAll();
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

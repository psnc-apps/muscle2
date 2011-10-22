/*
 * 
 */
package muscle.core.conduit.filter;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import utilities.SafeThread;
import utilities.SafeTriggeredThread;

/**
 *
 * @author Joris Borgdorff
 */
public abstract class AbstractThreadedFilter<E,F> extends SafeTriggeredThread implements Filter<E,F> {
	protected Queue<E> incomingQueue;
	protected final Queue<F> outgoingQueue;
	protected QueueConsumer<F> consumer;
	
	protected AbstractThreadedFilter() {
		this.outgoingQueue = new LinkedBlockingQueue<F>();
	}
	
	public AbstractThreadedFilter(QueueConsumer<F> qc) {
		this();
		this.consumer = qc;
		this.consumer.setIncomingQueue(this.outgoingQueue);
	}
	
	protected void execute() {
		if (incomingQueue == null) return;

		while (!incomingQueue.isEmpty()) {
			E message = incomingQueue.remove();
			if (message != null) {
				this.apply(message);
			}
		}

		consumer.apply();
	}
	
	public void apply() {
		this.trigger();
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

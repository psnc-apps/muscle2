/*
 * 
 */
package muscle.core.conduit.filter;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
<<<<<<< HEAD
import java.util.logging.Logger;
import utilities.SafeQueueConsumerThread;
=======
import java.util.logging.Level;
import java.util.logging.Logger;
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0

/**
 *
 * @author Joris Borgdorff
 */
<<<<<<< HEAD
public abstract class AbstractThreadedFilter<E,F> extends SafeQueueConsumerThread<E> implements Filter<E,F> {
	protected final Queue<F> outgoingQueue;
	protected QueueConsumer<F> consumer;
	private boolean processingMessage;
	
	protected AbstractThreadedFilter() {
		this.outgoingQueue = new LinkedBlockingQueue<F>();
=======
public abstract class AbstractThreadedFilter<E,F> extends Thread implements Filter<E,F> {
	protected Queue<E> incomingQueue;
	protected final Queue<F> outgoingQueue;
	protected QueueConsumer<F> consumer;
	private boolean isDone;
	private boolean apply;
	
	protected AbstractThreadedFilter() {
		this.outgoingQueue = new LinkedBlockingQueue<F>();
		this.isDone = false;
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0
	}
	
	public AbstractThreadedFilter(QueueConsumer<F> qc) {
		this();
		this.consumer = qc;
		this.consumer.setIncomingQueue(this.outgoingQueue);
	}
	
<<<<<<< HEAD
	protected void execute(E message) {
		if (message != null) {
			this.apply(message);
=======
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
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0
		}

		consumer.apply();
	}
	
<<<<<<< HEAD
=======
	public synchronized void apply() {
		this.apply = true;
		this.notifyAll();
	}
	
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0
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
<<<<<<< HEAD
	
	@Override
	protected void handleInterruption(InterruptedException ex) {
		Logger.getLogger(getClass().toString()).severe("Filter interrupted.");
=======

	public void setIncomingQueue(Queue<E> queue) {
		this.incomingQueue = queue;
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0
	}
}

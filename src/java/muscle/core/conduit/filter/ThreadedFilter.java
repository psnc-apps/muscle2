/*
 * 
 */
package muscle.core.conduit.filter;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.client.LocalManager;
import muscle.core.DataTemplate;
import muscle.core.model.Observation;
import muscle.util.concurrency.SafeThread;
import muscle.util.data.SingleProducerConsumerBlockingQueue;

/**
 *
 * @author Joris Borgdorff
 */
public class ThreadedFilter<E extends Serializable> extends SafeThread implements Filter<E,E> {
	protected final BlockingQueue<Observation<E>> outgoingQueue;
	protected QueueConsumer<E> consumer;
	protected DataTemplate<E> inTemplate;	
	private BlockingQueue<Observation<E>> queue;

	protected ThreadedFilter() {
		super("Filter");
		this.outgoingQueue = new SingleProducerConsumerBlockingQueue<Observation<E>>();
		this.queue = null;
	}
	
	public ThreadedFilter(QueueConsumer<E> qc) {
		this();
		this.consumer = qc;
		this.consumer.setIncomingQueue(this.outgoingQueue);
	}
	
	protected void execute(Observation<E> message) {
		if (message != null) {
			this.apply(message);
		}
		consumer.apply();
	}
	
	protected void put(Observation<E> message) {
		try {
			this.outgoingQueue.put(message);
		} catch (InterruptedException ex) {
			Logger.getLogger(ThreadedFilter.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	/**
	 * Apply the filter to a single message.
	 *
	 *  To pass the modified message on, call put(F message).
	 */
	protected void apply(Observation<E> subject) {
		put(subject);
	}

	public void setQueueConsumer(QueueConsumer<E> qc) {
		this.consumer = qc;
		this.consumer.setIncomingQueue(this.outgoingQueue);
		this.setInTemplate(qc.getInTemplate());
	}
	
	@Override
	protected void handleInterruption(InterruptedException ex) {
		Logger.getLogger(getClass().toString()).severe("Filter interrupted.");
	}
	
	@Override
	protected void handleException(Throwable ex) {
		Logger.getLogger(getClass().toString()).log(Level.SEVERE, "Filter had exception.");
		LocalManager.getInstance().fatalException(ex);
	}
	
	/** Sets the expected DataTemplate, based on the template of the consumer of this filter.
	 * Override if the DataTemplate is altered by using this filter.
	 */
	protected void setInTemplate(DataTemplate<E> consumerTemplate) {
		this.inTemplate = consumerTemplate;		
	}
	
	public DataTemplate<E> getInTemplate() {
		return this.inTemplate;
	}
		
	protected synchronized boolean continueComputation() throws InterruptedException {
		while (!isDisposed() && (queue == null || queue.isEmpty())) {
			wait();
		}
		return !isDisposed();
	}

	public synchronized void setIncomingQueue(BlockingQueue<Observation<E>> queue) {
		this.queue = queue;
		this.notifyAll();
	}

	public synchronized void apply() {
		this.notifyAll();
	}

	@Override
	protected synchronized void execute() throws InterruptedException {
		execute(queue.remove());
	}

	public synchronized void dispose() {
		this.queue = null;
		super.dispose();
	}
}

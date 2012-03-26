/*
 * 
 */
package muscle.core.conduit.filter;

import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.utilities.parallelism.SafeQueueConsumerThread;
import utilities.data.SingleProducerConsumerBlockingQueue;

/**
 *
 * @author Joris Borgdorff
 */
public abstract class AbstractThreadedFilter<E,F> extends SafeQueueConsumerThread<E> implements Filter<E,F> {
	protected final BlockingQueue<F> outgoingQueue;
	protected QueueConsumer<F> consumer;
	
	protected AbstractThreadedFilter() {
		this.outgoingQueue = new SingleProducerConsumerBlockingQueue<F>(10);
	}
	
	public AbstractThreadedFilter(QueueConsumer<F> qc) {
		this();
		this.consumer = qc;
		this.consumer.setIncomingQueue(this.outgoingQueue);
	}
	
	protected void execute(E message) {
		if (message != null) {
			this.apply(message);
		}
		consumer.apply();
	}
	
	protected void put(F message) {
		try {
			this.outgoingQueue.put(message);
		} catch (InterruptedException ex) {
			Logger.getLogger(AbstractThreadedFilter.class.getName()).log(Level.SEVERE, null, ex);
		}
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
	
	@Override
	protected void handleInterruption(InterruptedException ex) {
		Logger.getLogger(getClass().toString()).severe("Filter interrupted.");
	}
}

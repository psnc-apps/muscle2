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
import muscle.util.data.SerializableData;
import muscle.util.data.SingleProducerConsumerBlockingQueue;
import muscle.util.serialization.DataConverter;
import muscle.util.serialization.SerializableDataConverter;

/**
 *
 * @author Joris Borgdorff
 */
public class ThreadedFilter<E extends Serializable> extends SafeThread implements Filter<E,E> {
	protected final BlockingQueue<Observation<E>> incomingQueue;
	private final DataConverter<E,SerializableData> converter;
	protected DataTemplate<E> inTemplate;	
	private Filter<E, ?> nextFilter;
	private boolean processing;

	protected ThreadedFilter() {
		super("Filter");
		this.incomingQueue = new SingleProducerConsumerBlockingQueue<Observation<E>>();
		this.converter = new SerializableDataConverter<E>();
	}
	
	/** Queue a message, without necessarily processing it. */
	@Override
	public synchronized void queue(Observation<E> obs) {
		this.incomingQueue.add(obs.privateCopy(converter));
		this.notifyAll();
	}

	/** Apply the filter to at least all the messages queued so far. */
	@Override
	public synchronized void apply() {
		this.notifyAll();
	}

	/**
	 * Put a message intended for the next filter.
	 * @param message outgoing observation
	 */
	protected void put(Observation<E> message) {
		this.nextFilter.queue(message);
	}
	
	/**
	 * Apply the filter to a single message.
	 *
	 *  To pass the modified message on, call put(F message).
	 */
	protected void apply(Observation<E> subject) {
		put(subject);
	}

	@Override
	public void setNextFilter(Filter<E,?> qc) {
		this.nextFilter = qc;
		this.setInTemplate(qc.getInTemplate());
	}

	@Override
	public void setInTemplate(DataTemplate<E> consumerTemplate) {
		this.inTemplate = consumerTemplate;		
	}
	
	@Override
	public DataTemplate<E> getInTemplate() {
		return this.inTemplate;
	}
	
	@Override
	protected synchronized boolean continueComputation() throws InterruptedException {
		while (!isDisposed() && (incomingQueue == null || incomingQueue.isEmpty())) {
			wait();
		}
		return !isDisposed();
	}
	
	@Override
	protected void execute() throws InterruptedException {
		Observation<E> message;
		synchronized (this) {
			message = incomingQueue.remove();
			this.processing = true;
		}
		if (message != null) {
			this.apply(message);
		}
		nextFilter.apply();
		synchronized (this) {
			this.processing = false;
		}
	}
	
	public synchronized boolean isProcessing() {
		return (this.processing || !this.incomingQueue.isEmpty() || nextFilter.isProcessing());
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
}

/*
 * 
 */
package muscle.core.conduit.filter;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.client.LocalManager;
import muscle.core.model.Observation;
import muscle.util.concurrency.SafeThread;
import muscle.util.data.SerializableData;
import muscle.util.serialization.DataConverter;
import muscle.util.serialization.SerializableDataConverter;

/**
 *
 * @author Joris Borgdorff
 */
public class ThreadedFilter<E extends Serializable> extends SafeThread implements Filter<E,E> {
	protected final BlockingQueue<Observation<E>> incomingQueue;
	private final DataConverter<E,SerializableData> converter;
	private Filter<E, ?> nextFilter;
	private boolean processing;

	protected ThreadedFilter() {
		super("Filter");
		this.incomingQueue = new LinkedBlockingQueue<Observation<E>>();
		this.converter = new SerializableDataConverter<E>();
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
			this.processing = true;
			message = incomingQueue.remove();
		}
		if (message != null) {
			this.nextFilter.queue(message);
			nextFilter.apply();
		}
		synchronized (this) {
			this.processing = false;
		}
	}
	
	/** Queue a message, without necessarily processing it. */
	@Override
	public void queue(Observation<E> obs) {
		Observation<E> obsCopy = obs.privateCopy(converter);
		synchronized (this) {
			this.incomingQueue.add(obsCopy);
			this.notifyAll();
		}
	}

	/** Apply the filter to at least all the messages queued so far. */
	@Override
	public synchronized void apply() {
		this.notifyAll();
	}
	
	/** A threadedfilter is processing when it is processing itself, or a thread further down the filterchain is. */
	@Override
	public synchronized boolean isProcessing() {
		return this.processing || !this.incomingQueue.isEmpty() || nextFilter.isProcessing();
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
	
	@Override
	public void setNextFilter(Filter<E,?> qc) {
		this.nextFilter = qc;
	}
}

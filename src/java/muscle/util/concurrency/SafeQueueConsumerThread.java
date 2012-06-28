/*
 * 
 */
package muscle.util.concurrency;

import java.util.Queue;
import muscle.core.conduit.filter.QueueConsumer;

/**
 *
 * @author Joris Borgdorff
 */
public abstract class SafeQueueConsumerThread<T> extends SafeThread implements QueueConsumer<T> {
	private Queue<T> queue;
	
	public SafeQueueConsumerThread(String name) {
		super(name);
		this.queue = null;
	}
	
	protected synchronized boolean continueComputation() throws InterruptedException {
		while (!isDisposed() && (queue == null || queue.isEmpty())) {
			wait();
		}
		return !isDisposed();
	}

	public synchronized void setIncomingQueue(Queue<T> queue) {
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

	protected abstract void execute(T element);

	public synchronized void dispose() {
		this.queue = null;
		super.dispose();
	}
}

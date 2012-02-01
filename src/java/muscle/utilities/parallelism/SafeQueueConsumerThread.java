/*
 * 
 */
package muscle.utilities.parallelism;

import java.util.Queue;
import muscle.core.conduit.filter.QueueConsumer;

/**
 *
 * @author Joris Borgdorff
 */
public abstract class SafeQueueConsumerThread<T> extends SafeThread implements QueueConsumer<T> {
	private Queue<T> queue;
	
	protected synchronized boolean continueComputation() throws InterruptedException {
		while (!isDone && (queue == null || queue.isEmpty())) {
			wait();
		}
		return !isDone;
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
		this.queue.clear();
		super.dispose();
	}
}

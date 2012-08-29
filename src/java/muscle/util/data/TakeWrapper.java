/*
 * 
 */

package muscle.util.data;

import java.util.concurrent.BlockingQueue;

/**
 *
 * @author Joris Borgdorff
 */
public class TakeWrapper<T> implements Takeable<T> {
	private final BlockingQueue<T> queue;
	public TakeWrapper(BlockingQueue<T> queue) {
		this.queue = queue;
	}

	@Override
	public T take() throws InterruptedException {
		return queue.take();
	}

	@Override
	public boolean isEmpty() {
		return queue.isEmpty();
	}
}

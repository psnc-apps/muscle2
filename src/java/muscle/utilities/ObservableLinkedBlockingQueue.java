/*
 * 
 */
package muscle.utilities;

import java.util.concurrent.LinkedBlockingQueue;
import muscle.core.conduit.filter.QueueConsumer;
import muscle.core.conduit.filter.QueueProducer;

/**
 *
 * @author Joris Borgdorff
 */
public class ObservableLinkedBlockingQueue<E> extends LinkedBlockingQueue<E>implements QueueProducer<E> {
	private QueueConsumer<E> obs;
	
	@Override
	public boolean add(E elem) {
		super.add(elem);
		obs.apply();
		return true;
	}

	public void setQueueConsumer(QueueConsumer<E> qc) {
		this.obs = qc;
	}
}

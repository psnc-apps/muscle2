/*
 * 
 */
package muscle.core.conduit.filter;

import java.io.Serializable;

/**
 * Produces messages on a queue.
 * The queue is passed to a queue consumer that may read the produced messages.
 * @author Joris Borgdorff
 */
public interface QueueProducer<F extends Serializable> {
	public void setQueueConsumer(QueueConsumer<F> qc);
}

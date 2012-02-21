/*
 * 
 */
package muscle.core.conduit.filter;

/**
 * Produces messages on a queue.
 * The queue is passed to a queue consumer that may read the produced messages.
 * @author Joris Borgdorff
 */
public interface QueueProducer<F> {
	public void setQueueConsumer(QueueConsumer<F> qc);
}

/*
 * 
 */
package muscle.core.conduit.filter;

/**
 *
 * @author jborgdo1
 */
public interface QueueProducer<F> {
	public void setQueueConsumer(QueueConsumer<F> qc);
}

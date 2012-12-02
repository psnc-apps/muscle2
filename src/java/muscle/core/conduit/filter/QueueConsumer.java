package muscle.core.conduit.filter;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import muscle.core.DataTemplate;
import muscle.core.model.Observation;

/**
 * Reads messages from an incoming queue. May be implemented synchronously or asynchronously.
 * @author Joris Borgdorff
 */
public interface QueueConsumer<E extends Serializable> {
	/** Set the queue containing the incoming messages.
	 * 
	 * Unless apply() is called after a message has been added to the queue, it is not guaranteed to be read.
	 */
	public void setIncomingQueue(BlockingQueue<Observation<E>> queue);
	
	/** Indicate that the incoming queue is non-empty. */
	public void apply();
	
	public DataTemplate<E> getInTemplate();
}

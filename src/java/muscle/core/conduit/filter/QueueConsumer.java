package muscle.core.conduit.filter;

import java.util.Queue;

/**
 *
 * @author Joris Borgdorff
 */
public interface QueueConsumer<E> {
	public void setIncomingQueue(Queue<E> queue);
	public void apply();
}

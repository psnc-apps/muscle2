/*
 * 
 */
package muscle.core.conduit.filter;

import muscle.core.DataTemplate;
import muscle.core.wrapper.Observation;

/**
 * A wrapper only consumes datawrappers with a certain template.
 * @author Joris Borgdorff
 */
public interface WrapperQueueConsumer<E> extends QueueConsumer<Observation<E>> {
	public DataTemplate<E> getInTemplate();
}

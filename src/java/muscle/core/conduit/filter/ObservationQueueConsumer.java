/*
 * 
 */
package muscle.core.conduit.filter;

import java.io.Serializable;
import muscle.core.DataTemplate;
import muscle.core.model.Observation;

/**
 * A wrapper only consumes datawrappers with a certain template.
 * @author Joris Borgdorff
 */
public interface ObservationQueueConsumer<E extends Serializable> extends QueueConsumer<Observation<E>> {
	public DataTemplate<E> getInTemplate();
}

/*
 * 
 */
package muscle.core.conduit.filter;

import muscle.core.DataTemplate;
import muscle.core.wrapper.DataWrapper;

/**
 *
 * @author Joris Borgdorff
 */
public interface WrapperQueueConsumer extends QueueConsumer<DataWrapper> {
	public DataTemplate getInTemplate();
}

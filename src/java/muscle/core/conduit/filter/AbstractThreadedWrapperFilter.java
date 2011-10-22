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
public abstract class AbstractThreadedWrapperFilter extends AbstractThreadedFilter<DataWrapper,DataWrapper> implements WrapperFilter {
	protected DataTemplate inTemplate;
	
	public void setQueueConsumer(WrapperQueueConsumer qc) {
		super.setQueueConsumer(qc);
		this.setInTemplate(qc.getInTemplate());
	}
	
	/** Sets the expected DataTemplate, based on the template of the consumer of this filter.
	 * Override if the DataTemplate is altered by using this filter.
	 */
	protected void setInTemplate(DataTemplate consumerTemplate) {
		this.inTemplate = consumerTemplate;		
	}
	
	public DataTemplate getInTemplate() {
		return this.inTemplate;
	}
}

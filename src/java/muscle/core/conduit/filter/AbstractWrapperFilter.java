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
public abstract class AbstractWrapperFilter extends AbstractFilter<DataWrapper,DataWrapper> implements WrapperFilter {
	protected DataTemplate inTemplate;
	
	public void setQueueConsumer(WrapperQueueConsumer qc) {
		super.setQueueConsumer(qc);
		this.setInTemplate(qc.getInTemplate());
	}
	
	protected void setInTemplate(DataTemplate consumerTemplate) {
		this.inTemplate = consumerTemplate;		
	}
	
	public DataTemplate getInTemplate() {
		return this.inTemplate;
	}
}

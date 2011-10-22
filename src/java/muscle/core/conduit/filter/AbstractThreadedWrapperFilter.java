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
public abstract class AbstractThreadedWrapperFilter<E,F> extends AbstractThreadedFilter<DataWrapper<E>,DataWrapper<F>> implements WrapperFilter<E,F> {
	protected DataTemplate<E> inTemplate;
	
	public void setQueueConsumer(WrapperQueueConsumer<F> qc) {
		super.setQueueConsumer(qc);
		this.setInTemplate(qc.getInTemplate());
	}
	
	/** Sets the expected DataTemplate, based on the template of the consumer of this filter.
	 * Override if the DataTemplate is altered by using this filter.
	 */
	protected void setInTemplate(DataTemplate<F> consumerTemplate) {
		this.inTemplate = (DataTemplate<E>)consumerTemplate;		
	}
	
	public DataTemplate<E> getInTemplate() {
		return this.inTemplate;
	}
}

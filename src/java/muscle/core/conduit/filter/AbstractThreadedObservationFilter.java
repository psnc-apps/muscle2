/*
 * 
 */
package muscle.core.conduit.filter;

import muscle.core.DataTemplate;
import muscle.core.wrapper.Observation;

/**
 *
 * @author Joris Borgdorff
 */
public abstract class AbstractThreadedObservationFilter<E,F> extends AbstractThreadedFilter<Observation<E>,Observation<F>> implements ObservationFilter<E,F> {
	protected DataTemplate<E> inTemplate;
	
	public void setQueueConsumer(ObservationQueueConsumer<F> qc) {
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

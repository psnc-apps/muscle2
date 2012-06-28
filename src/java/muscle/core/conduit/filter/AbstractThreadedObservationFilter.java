/*
 * 
 */
package muscle.core.conduit.filter;

import java.io.Serializable;
import muscle.core.DataTemplate;
import muscle.core.model.Observation;

/**
 *
 * @author Joris Borgdorff
 */
public abstract class AbstractThreadedObservationFilter<E extends Serializable,F extends Serializable> extends AbstractThreadedFilter<Observation<E>,Observation<F>> implements ObservationFilter<E,F> {
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

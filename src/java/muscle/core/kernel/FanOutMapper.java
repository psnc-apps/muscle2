/*
 * 
 */

package muscle.core.kernel;

import java.io.Serializable;
import muscle.core.ConduitExit;
import muscle.core.model.Observation;

/**
 * A fan-out mapper.
 * Use the value variable in to send in writeAll().
 * @author Joris Borgdorff
 */
public abstract class FanOutMapper<T extends Serializable> extends Mapper {
	protected ConduitExit<T> onlyExit;
	protected Observation<T> value;

	@SuppressWarnings("unchecked")
	public void addPortals() {
		super.addPortals();
		if (this.exits.size() != 1) {
			throw new IllegalStateException("A fan-out mapper only allows a single output, instead of " + exits.size());
		}
		onlyExit = this.exits.values().iterator().next().getExit();
	}

	@Override
	protected boolean continueComputation() {
		return onlyExit.hasNext();
	}
	
	@Override
	protected boolean readyForContinue() {
		return onlyExit.ready();
	}
	@Override
	protected boolean readyForReceive() {
		return onlyExit.ready();
	}
	
	@Override
	protected final void receiveAll() {
		value = onlyExit.receiveObservation();
	}
}

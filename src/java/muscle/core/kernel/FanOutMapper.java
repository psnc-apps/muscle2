/*
 * 
 */

package muscle.core.kernel;

import java.io.Serializable;
import muscle.core.ConduitExit;

/**
 * A fan-out mapper.
 * Use the value variable in to send in writeAll().
 * @author Joris Borgdorff
 */
public abstract class FanOutMapper extends Mapper {
	protected ConduitExit onlyExit;
	protected Serializable value;

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
	protected final void receiveAll() {
		value = onlyExit.receive();
	}
}

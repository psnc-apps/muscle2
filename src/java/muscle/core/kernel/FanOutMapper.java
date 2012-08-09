/*
 * 
 */

package muscle.core.kernel;

import java.io.Serializable;
import muscle.core.ConduitEntrance;
import muscle.core.ConduitExit;
import muscle.core.ConduitExitController;

/**
 *
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
	protected void readAll() {
		value = onlyExit.receive();
	}
}

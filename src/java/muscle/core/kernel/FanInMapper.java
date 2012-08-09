/*
 * 
 */

package muscle.core.kernel;

import java.io.Serializable;
import muscle.core.ConduitEntrance;
import muscle.core.ConduitExit;

/**
 * A fan-in mapper.
 * Set the value variable in readAll(), this will be sent.
 * @author Joris Borgdorff
 */
public abstract class FanInMapper extends Mapper {
	protected ConduitEntrance onlyEntrance;
	protected Serializable value;

	@Override
	public void addPortals() {
		super.addPortals();
		if (this.entrances.size() != 1) {
			throw new IllegalStateException("A fan-in mapper only allows a single input, instead of " + entrances.size());
		}
		onlyEntrance = this.entrances.values().iterator().next().getEntrance();
	}

	@Override
	protected final void sendAll() {
		onlyEntrance.send(value);
	}
}

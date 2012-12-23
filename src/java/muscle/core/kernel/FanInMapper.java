/*
 * 
 */

package muscle.core.kernel;

import java.io.Serializable;
import muscle.core.ConduitEntrance;
import muscle.core.model.Observation;

/**
 * A fan-in mapper.
 * Set the value variable in readAll(), this will be sent.
 * @author Joris Borgdorff
 */
public abstract class FanInMapper<T extends Serializable> extends Mapper {
	protected ConduitEntrance<T> onlyEntrance;
	protected Observation<T> value;

	@Override
	public void addPortals() {
		super.addPortals();
		if (this.entrances.size() != 1) {
			throw new IllegalStateException("A fan-in mapper only allows a single output, instead of " + entrances.size());
		}
		onlyEntrance = this.entrances.values().iterator().next().getEntrance();
	}

	@Override
	protected final void sendAll() {
		onlyEntrance.send(value);
	}
}

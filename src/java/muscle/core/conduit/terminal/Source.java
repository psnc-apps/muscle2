/*
 * 
 */

package muscle.core.conduit.terminal;

import java.io.Serializable;
import muscle.core.ConduitExit;
import muscle.core.ConduitExitController;
import muscle.core.model.Observation;
import muscle.util.data.Takeable;

/**
 * Generates data each time receive is called.
 * @author Joris Borgdorff
 */
public abstract class Source<T extends Serializable> extends Terminal implements ConduitExitController<T>, Takeable<Observation<T>> {
	private ConduitExit<T> exit;
	
	@Override
	public Takeable<Observation<T>> getMessageQueue() {
		return this;
	}
	@Override
	public void messageReceived(Observation<T> obs) {
		super.resetTime(obs.getNextTimestamp());
	}
	@Override
	public ConduitExit<T> getExit() {
		return this.exit;
	}
	@Override
	public void setExit(ConduitExit<T> exit) {
		this.exit = exit;
	}
		
	@Override
	public String toString() {
		return getIdentifier().getPortName() + "<" + getClass().getSimpleName();
	}
}

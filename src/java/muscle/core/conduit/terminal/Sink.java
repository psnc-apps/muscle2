/*
 * 
 */

package muscle.core.conduit.terminal;

import java.io.Serializable;
import muscle.core.ConduitEntrance;
import muscle.core.ConduitEntranceController;
import muscle.core.model.Observation;
import muscle.core.model.Timestamp;
import muscle.id.PortalID;

/**
 * Receives messages.
 * Override to implement useful behavior.
 * @author Joris Borgdorff
 */
public abstract class Sink<T extends Serializable> extends Terminal implements ConduitEntranceController<T> {
	private ConduitEntrance<T> entrance;
	
	public ConduitEntrance<T> getEntrance() {
		return this.entrance;
	}
	public void setEntrance(ConduitEntrance<T> entrance) {
		this.entrance = entrance;
	}
	
	public boolean waitUntilEmpty() throws InterruptedException {
		return true;
	}
	
	@Override
	public String toString() {
		return getIdentifier().getPortName() + ">" + getClass().getSimpleName();
	}
}

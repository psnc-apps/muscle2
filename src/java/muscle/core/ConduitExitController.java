/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.core;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import muscle.core.model.Observation;
import muscle.id.Identifiable;
import muscle.id.PortalID;
import muscle.util.data.Takeable;

/**
 *
 * @author Joris Borgdorff
 */
public interface ConduitExitController<T extends Serializable> extends Portal {
	/**
	 * The queue on which all messages will be placed.
	 * The ConduitExit uses this to get the messages.
	 */
	Takeable<Observation<T>> getMessageQueue();
	/** For the ConduitExit to signal that it will pass a message to the user. */
	void messageReceived(Observation<T> obs);
	/** Get the ConduitExit that is controlled. */
	public ConduitExit<T> getExit();
	/**
	 * Set the ConduitExit that will be controlled.
	 * Use only by MUSCLE during initialization phase.
	 */
	public void setExit(ConduitExit<T> exit);
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.core;

import java.io.Serializable;
import muscle.core.model.Observation;

/**
 * Controls what happens to data of the ConduitEntrance.
 * @author Joris Borgdorff
 */
public interface ConduitEntranceController<T extends Serializable> extends Portal {
	/** Send a message. */
	void send(Observation<T> msg);
	
	/** Get the ConduitEntrance that is controlled. */
	public ConduitEntrance getEntrance();
	
	/**
	 * Set the ConduitEntrance that will be controlled.
	 * Use only by MUSCLE during initialization phase.
	 */
	public void setEntrance(ConduitEntrance<T> entrance);
	/**
	 * Waits until all the messages of the ConduitEntrance are sent.
	 * Use only by MUSCLE during finalization phase.
	 */
	public boolean waitUntilEmpty() throws InterruptedException;
	
	public boolean hasTransmitter();
	
	public boolean isEmpty();
}

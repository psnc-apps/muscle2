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
	Takeable<Observation<T>> getMessageQueue();
	void messageReceived(Observation<T> obs);
	public ConduitExit<T> getExit();
	public void setExit(ConduitExit<T> exit);
}

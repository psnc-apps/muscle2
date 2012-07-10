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

/**
 *
 * @author Joris Borgdorff
 */
public interface ConduitExitController<T extends Serializable> extends Identifiable<PortalID>, Portal {
	BlockingQueue<Observation<T>> getMessageQueue();
	void messageReceived(Observation<T> obs);
	public ConduitExit<T> getExit();
	public void setExit(ConduitExit<T> exit);
}

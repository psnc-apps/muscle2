/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.core;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import muscle.core.ident.Identifiable;
import muscle.core.ident.PortalID;
import muscle.core.model.Observation;
import muscle.core.model.Timestamp;

/**
 *
 * @author Joris Borgdorff
 */
public interface ConduitExitController<T extends Serializable> extends Identifiable<PortalID> {
	BlockingQueue<Observation<T>> getMessageQueue();
	void messageReceived(Observation<T> obs);
	public String getLocalName();
	public ConduitExit<T> getExit();
	public void setExit(ConduitExit<T> exit);
	public Timestamp getSITime();
}

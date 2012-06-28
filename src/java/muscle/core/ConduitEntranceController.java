/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.core;

import java.io.Serializable;
import muscle.core.ident.Identifiable;
import muscle.core.ident.PortalID;
import muscle.core.model.Timestamp;

/**
 *
 * @author Joris Borgdorff
 */
public interface ConduitEntranceController<T extends Serializable> extends Identifiable<PortalID> {
	void send(T data, Timestamp currentTime, Timestamp next);
	public String getLocalName();
	public ConduitEntrance getEntrance();
	public void setEntrance(ConduitEntrance<T> entrance);
	public Timestamp getSITime();
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.core;

import java.io.Serializable;
import muscle.core.model.Timestamp;
import muscle.id.Identifiable;
import muscle.id.PortalID;

/**
 *
 * @author Joris Borgdorff
 */
public interface ConduitEntranceController<T extends Serializable> extends Identifiable<PortalID>, Portal {
	public void dispose();
	void send(T data, Timestamp currentTime, Timestamp next);
	public ConduitEntrance getEntrance();
	public void setEntrance(ConduitEntrance<T> entrance);
}

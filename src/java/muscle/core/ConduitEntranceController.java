/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.core;

import java.io.Serializable;
import muscle.core.model.Observation;

/**
 *
 * @author Joris Borgdorff
 */
public interface ConduitEntranceController<T extends Serializable> extends Portal {
	void send(Observation<T> msg);
	public ConduitEntrance getEntrance();
	public void setEntrance(ConduitEntrance<T> entrance);
	public boolean waitUntilEmpty() throws InterruptedException;
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.core.conduit;

import java.util.Queue;
import muscle.core.ConduitEntranceController;
import muscle.core.DataTemplate;
import muscle.core.conduit.communication.Transmitter;
import muscle.core.conduit.filter.QueueConsumer;
import muscle.core.ident.PortalID;
import muscle.core.kernel.InstanceController;

/**
 *
 * @author Joris Borgdorff
 */
public class ConduitEntrance<E> implements QueueConsumer<E> {
	private final InstanceController controller;
	private final PortalID portalID;
	private Transmitter<E,?> transmitter;

	public ConduitEntrance(InstanceController instanceController, PortalID portalID) {
		this.controller = instanceController;
		this.portalID = portalID;
	}
	
	public void setTransmitter(Transmitter<E,?> trans) {
		this.transmitter = trans;
	}
	
	public ConduitEntranceController<E> getEntrance(String newPortalName) {
		return new ConduitEntranceController<E>(new PortalID(newPortalName, controller.getID()), controller, 1, null);
	}

	public void setIncomingQueue(Queue<E> queue) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void apply() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}

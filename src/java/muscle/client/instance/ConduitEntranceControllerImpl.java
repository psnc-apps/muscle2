/*
 * 
 */
package muscle.client.instance;

import java.io.Serializable;
import muscle.client.communication.Transmitter;
import muscle.core.ConduitEntranceController;

/**
 *
 * @author jborgdo1
 */
public interface ConduitEntranceControllerImpl<T extends Serializable> extends ConduitEntranceController<T> {
	public void start();
	public void setTransmitter(Transmitter<T,?> trans);

	public void setSharedData();
}

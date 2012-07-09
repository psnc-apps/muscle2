/*
 * 
 */
package muscle.client.communication;

import java.io.Serializable;
import muscle.client.communication.message.Signal;
import muscle.id.Identifier;
import muscle.id.PortalID;
import muscle.core.model.Observation;

/**
 *
 * @author jborgdo1
 */
public interface Transmitter<E extends Serializable,F,Q extends Identifier,P extends PortalID<Q>> extends CommunicatingPoint<Observation<E>, F, Q, P> {
	public void signal(Signal signal);
	public void transmit(Observation<E> msg);
}

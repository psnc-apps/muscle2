/*
 * 
 */
package muscle.core.conduit.communication;

import java.io.Serializable;
import muscle.core.ident.Identifier;
import muscle.core.ident.PortalID;
import muscle.core.messaging.Message;
import muscle.core.messaging.signal.Signal;
import muscle.core.messaging.Observation;

/**
 *
 * @author jborgdo1
 */
public interface Transmitter<E extends Serializable,F,Q extends Identifier,P extends PortalID<Q>> extends CommunicatingPoint<Observation<E>, F, Q, P> {
	public void signal(Signal signal);
	public void transmit(Observation<E> msg);
}

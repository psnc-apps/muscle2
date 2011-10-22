/*
 * 
 */
package muscle.core.conduit.communication;

import muscle.core.ident.Identifier;
import muscle.core.ident.PortalID;
import muscle.core.messaging.Message;
import muscle.core.messaging.signal.Signal;

/**
 *
 * @author jborgdo1
 */
public interface Transmitter<E,F,Q extends Identifier,P extends PortalID<Q>> extends CommunicatingPoint<E, F, Q, P> {
	public void signal(Signal signal);
	public void transmit(Message<E> signal);
}

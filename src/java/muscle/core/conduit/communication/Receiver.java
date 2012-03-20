/*
 * 
 */
package muscle.core.conduit.communication;

import muscle.core.ident.Identifier;
import muscle.core.ident.PortalID;
import muscle.core.messaging.signal.Signal;

/**
 *
 * @author jborgdo1
 */
public interface Receiver<E, F, Q extends Identifier, P extends PortalID<Q>> extends CommunicatingPoint<E,F,Q,P> {
	/** Receives a message. Will return null if no more messages can be received */
	public E receive();
	
	public boolean hasSignal();
	public Signal getSignal();
}

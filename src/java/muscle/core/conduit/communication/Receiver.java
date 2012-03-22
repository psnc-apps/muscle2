/*
 * 
 */
package muscle.core.conduit.communication;

import java.io.Serializable;
import muscle.core.ident.Identifier;
import muscle.core.ident.PortalID;
import muscle.core.messaging.Message;

/**
 *
 * @author jborgdo1
 */
public interface Receiver<E extends Serializable, F, Q extends Identifier, P extends PortalID<Q>> extends CommunicatingPoint<Message<E>,F,Q,P> {
	/** Receives a message. Will return null if no more messages can be received */
	public Message<E> receive();
	
	void put(F msg);
}

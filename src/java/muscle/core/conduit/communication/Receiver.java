/*
 * 
 */
package muscle.core.conduit.communication;

import muscle.core.messaging.Message;

/**
 *
 * @author jborgdo1
 */
public interface Receiver<E, F> extends CommunicatingPoint<Message<E>,F> {
	/** Receives a message. Will return null if no more messages can be received */
	public Message<E> receive();
}

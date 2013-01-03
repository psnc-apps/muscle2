/*
 * 
 */
package muscle.client.communication;

import java.io.Serializable;
import muscle.client.communication.message.Message;

/**
 *
 * @author jborgdo1
 */
public interface Receiver<E extends Serializable, F> extends CommunicatingPoint<Message<E>,F> {
	/** Receives a message. Will return null if no more messages can be received */
	public Message<E> receive();
	
	void put(F msg);
}

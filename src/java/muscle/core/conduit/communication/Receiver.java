/*
 * 
 */
package muscle.core.conduit.communication;

import muscle.core.ident.PortalID;
import muscle.core.messaging.Message;
import muscle.core.messaging.serialization.DataConverter;

/**
 *
 * @author jborgdo1
 */
public interface Receiver<E, F> {
	public void setTransmittingPort(PortalID id);
	/** Receives a message. Will return null if no more messages can be received */
	public Message<E> receive();
	public void setDeserializer(DataConverter<Message<E>,F> deserializer);
}

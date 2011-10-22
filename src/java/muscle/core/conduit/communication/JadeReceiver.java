/*
 * 
 */
package muscle.core.conduit.communication;

import jade.lang.acl.ACLMessage;
import muscle.core.ident.PortalID;
import muscle.core.messaging.Message;
import muscle.core.messaging.serialization.DataConverter;

/**
 *
 * @author Joris Borgdorff
 */
public class JadeReceiver<T> implements Receiver<T, ACLMessage> {
	@Override
	public void setDeserializer(DataConverter<Message<T>, ACLMessage> deserializer) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setTransmittingPort(PortalID id) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Message<T> receive() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}

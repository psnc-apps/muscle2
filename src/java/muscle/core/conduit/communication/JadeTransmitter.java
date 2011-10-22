/*
 * 
 */
package muscle.core.conduit.communication;

import jade.core.Agent;
import muscle.core.ident.JadeIdentifier;
import muscle.core.ident.JadePortalID;
import muscle.core.messaging.Message;
import muscle.core.messaging.jade.DataMessage;
import muscle.core.messaging.jade.ObservationMessage;
import muscle.core.messaging.signal.Signal;

/**
 *
 * @author Joris Borgdorff
 */
public class JadeTransmitter<T> extends AbstractCommunicatingPoint<T, byte[],JadeIdentifier,JadePortalID> implements Transmitter<T, byte[],JadeIdentifier,JadePortalID> {
	private Agent senderAgent;
	
	public JadeTransmitter(Agent senderAgent) {
		this.senderAgent = senderAgent;
	}
	
	public void transmit(Message<T> msg) {
		if (converter == null) {
			throw new IllegalStateException("Can not send message without serialization");
		}
		if (!(msg instanceof ObservationMessage)) {
			throw new IllegalArgumentException("Can only send data messages");
		}
		ObservationMessage<T> dmsg = (ObservationMessage<T>) msg;
		
		byte[] rawData = null;
		rawData = converter.serialize(dmsg.getRawData());
		dmsg.setByteSequenceContent(rawData);
		dmsg.setRecipient(portalID);
		dmsg.store(null, null);
		
		// send data to target agent
		senderAgent.send(dmsg);
		dmsg.setByteSequenceContent(null);
	}

	public void signal(Signal signal) {
		DataMessage<Signal> dmsg = new DataMessage<Signal>();
		dmsg.setRecipient(portalID.getOwnerID());
		dmsg.store(signal, null);
		senderAgent.send(dmsg);
	}
}

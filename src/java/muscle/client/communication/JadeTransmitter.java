/*
 * 
 */
package muscle.client.communication;

import jade.core.Agent;
import java.io.Serializable;
import muscle.client.communication.message.JadeObservationMessage;
import muscle.client.communication.message.Signal;
import muscle.client.ident.JadeIdentifier;
import muscle.client.ident.JadePortalID;
import muscle.core.model.Observation;

/**
 *
 * @author Joris Borgdorff
 */
public class JadeTransmitter<T extends Serializable> extends AbstractCommunicatingPoint<Observation<T>, byte[],JadeIdentifier,JadePortalID> implements Transmitter<T, byte[],JadeIdentifier,JadePortalID> {
	private Agent senderAgent;
	
	public JadeTransmitter(Agent senderAgent) {
		this.senderAgent = senderAgent;
	}
	
	public void transmit(Observation<T> obs) {
		if (converter == null) {
			throw new IllegalStateException("Can not send message without serialization");
		}
		JadeObservationMessage<T> dmsg = new JadeObservationMessage<T>();
		byte[] rawData = converter.serialize(obs);
		dmsg.setByteSequenceContent(rawData);
		dmsg.setRecipient(portalID);
		
		// send data to target agent
		senderAgent.send(dmsg);
	}

	public void signal(Signal signal) {
		JadeObservationMessage<T> dmsg = new JadeObservationMessage<T>();
		dmsg.setRecipient(portalID.getOwnerID());
		dmsg.setSignal(signal);
		senderAgent.send(dmsg);
	}
}

/*
 * 
 */
package muscle.client.communication;

import jade.core.Agent;
import java.io.Serializable;
import muscle.client.communication.message.JadeObservationMessage;
import muscle.client.communication.message.Signal;
import muscle.client.id.JadeIdentifier;
import muscle.client.id.JadePortalID;
import muscle.core.model.Observation;
import muscle.util.serialization.DataConverter;

/**
 *
 * @author Joris Borgdorff
 */
public class JadeTransmitter<T extends Serializable> extends AbstractCommunicatingPoint<Observation<T>, byte[],JadeIdentifier,JadePortalID> implements Transmitter<T, byte[]> {
	private Agent senderAgent;
	
	public JadeTransmitter(Agent senderAgent, DataConverter<Observation<T>, byte[]> converter, JadePortalID portalID) {
		super(converter, portalID);
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

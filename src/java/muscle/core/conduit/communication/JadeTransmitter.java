/*
 * 
 */
package muscle.core.conduit.communication;

import jade.core.Agent;
import muscle.core.ident.JadeIdentifier;
import muscle.core.ident.JadePortalID;
import muscle.core.messaging.jade.DataMessage;
import muscle.core.messaging.jade.ObservationMessage;
import muscle.core.messaging.signal.Signal;
import muscle.core.wrapper.Observation;

/**
 *
 * @author Joris Borgdorff
 */
public class JadeTransmitter<T> extends AbstractCommunicatingPoint<Observation<T>, byte[],JadeIdentifier,JadePortalID> implements Transmitter<T, byte[],JadeIdentifier,JadePortalID> {
	private Agent senderAgent;
	
	public JadeTransmitter(Agent senderAgent) {
		this.senderAgent = senderAgent;
	}
	
	public void transmit(Observation<T> obs) {
		if (converter == null) {
			throw new IllegalStateException("Can not send message without serialization");
		}
		ObservationMessage<T> dmsg = new ObservationMessage<T>();
		byte[] rawData = converter.serialize(obs);
		dmsg.setByteSequenceContent(rawData);
		dmsg.setRecipient(portalID);
		
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

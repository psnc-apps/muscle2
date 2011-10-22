/*
 * 
 */
package muscle.core.conduit.communication;

import jade.core.Agent;
<<<<<<< HEAD
import muscle.core.ident.JadeIdentifier;
import muscle.core.ident.JadePortalID;
import muscle.core.messaging.jade.DataMessage;
import muscle.core.messaging.jade.ObservationMessage;
import muscle.core.messaging.signal.Signal;
import muscle.core.wrapper.Observation;
=======
import jade.lang.acl.ACLMessage;
import java.io.Serializable;
import muscle.core.ident.PortalID;
import muscle.core.messaging.Message;
import muscle.core.messaging.jade.DataMessage;
import muscle.core.messaging.serialization.ByteDataConverter;
import muscle.core.messaging.serialization.DataConverter;
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0

/**
 *
 * @author Joris Borgdorff
 */
<<<<<<< HEAD
public class JadeTransmitter<T> extends AbstractCommunicatingPoint<Observation<T>, byte[],JadeIdentifier,JadePortalID> implements Transmitter<T, byte[],JadeIdentifier,JadePortalID> {
=======
public class JadeTransmitter<T> implements Transmitter<T, byte[]> {
	private DataConverter<T, byte[]> serializer;
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0
	private Agent senderAgent;
	
	public JadeTransmitter(Agent senderAgent) {
		this.senderAgent = senderAgent;
	}
	
<<<<<<< HEAD
	public void transmit(Observation<T> obs) {
		if (converter == null) {
			throw new IllegalStateException("Can not send message without serialization");
		}
		ObservationMessage<T> dmsg = new ObservationMessage<T>();
		byte[] rawData = converter.serialize(obs);
		dmsg.setByteSequenceContent(rawData);
		dmsg.setRecipient(portalID);
		
=======
	public void transmit(Message<T> msg) {
		if (serializer == null) {
			throw new IllegalStateException("Can not send message without serialization");
		}
		if (!(msg instanceof DataMessage)) {
			throw new IllegalArgumentException("Can only send data messages");
		}
		DataMessage<T> dmsg = (DataMessage<T>) msg;
		assert !dmsg.hasByteSequenceContent();

		byte[] rawData = null;
		rawData = serializer.serialize(dmsg.getData());
// 		rawData = MiscTool.gzip(dmsg.getStored());

		dmsg.setByteSequenceContent(rawData);
		dmsg.store(null, null);

>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0
		// send data to target agent
		senderAgent.send(dmsg);
		dmsg.setByteSequenceContent(null);
	}

<<<<<<< HEAD
	public void signal(Signal signal) {
		DataMessage<Signal> dmsg = new DataMessage<Signal>();
		dmsg.setRecipient(portalID.getOwnerID());
		dmsg.store(signal, null);
		senderAgent.send(dmsg);
=======
	public void setSerializer(DataConverter<T, byte[]> serializer) {
		this.serializer = serializer;
	}

	public void setReceivingPort(PortalID id) {
		throw new UnsupportedOperationException("Not supported yet.");
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0
	}
}

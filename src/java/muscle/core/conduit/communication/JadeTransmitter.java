/*
 * 
 */
package muscle.core.conduit.communication;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import java.io.Serializable;
import muscle.core.ident.PortalID;
import muscle.core.messaging.Message;
import muscle.core.messaging.jade.DataMessage;
import muscle.core.messaging.serialization.ByteDataConverter;
import muscle.core.messaging.serialization.DataConverter;

/**
 *
 * @author Joris Borgdorff
 */
public class JadeTransmitter<T> implements Transmitter<T, byte[]> {
	private DataConverter<T, byte[]> serializer;
	private Agent senderAgent;
	
	public JadeTransmitter(Agent senderAgent) {
		this.senderAgent = senderAgent;
	}
	
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

		// send data to target agent
		senderAgent.send(dmsg);
		dmsg.setByteSequenceContent(null);
	}

	public void setSerializer(DataConverter<T, byte[]> serializer) {
		this.serializer = serializer;
	}

	public void setReceivingPort(PortalID id) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}

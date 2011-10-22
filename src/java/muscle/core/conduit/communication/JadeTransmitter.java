/*
 * 
 */
package muscle.core.conduit.communication;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import java.io.IOException;
import muscle.Constant;
import muscle.core.ident.PortalID;
import muscle.core.messaging.Message;
import muscle.core.messaging.jade.DataMessage;
import muscle.core.messaging.serialization.DataConverter;
import muscle.core.messaging.signal.DetachConduitSignal;
import muscle.core.messaging.signal.Signal;
import muscle.exception.MUSCLERuntimeException;

/**
 *
 * @author Joris Borgdorff
 */
public class JadeTransmitter<T> implements Transmitter<T, byte[]> {
	private DataConverter<T, byte[]> serializer;
	private Agent senderAgent;
	private PortalID recvPortalID;
	
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
		this.recvPortalID = id;
	}

	public void signal(Signal signal) {
		if (signal instanceof DetachConduitSignal) {
			this.senderAgent.send(getDetachDstMessage());
		}
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	private ACLMessage getDetachDstMessage() {
		// bulid message which tells the conduit to detach this portal
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol(Constant.Protocol.PORTAL_DETACH);
		try {
			msg.setContentObject(this.getClass());
		} catch (IOException e) {
			throw new MUSCLERuntimeException();
		}

		msg.addReceiver(recvPortalID.getAID());
		return msg;
	}
	
	
//	public void setDestination(AID newDstAgent, String newDstSink) {
//		// allow only once to connect this sender
//		if (dstAgent != null) {
//			throw new IllegalStateException("already connected to <" + dstAgent + ":" + dstSink + ">");
//		}
//
//		dstAgent = newDstAgent;
//		dstSink = newDstSink;
//
//		// set up message dummy for outgoing data messages
//		dataMessage = new DataMessage(dstSink);
//		dataMessage.addReceiver(dstAgent);
//	}
}

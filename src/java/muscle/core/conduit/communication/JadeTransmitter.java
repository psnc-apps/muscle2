/*
 * 
 */
package muscle.core.conduit.communication;

import jade.core.Agent;
import muscle.core.messaging.Message;
import muscle.core.messaging.jade.DataMessage;
import muscle.core.messaging.jade.ObservationMessage;
import muscle.core.messaging.signal.Signal;

/**
 *
 * @author Joris Borgdorff
 */
public class JadeTransmitter<T> extends AbstractCommunicatingPoint<T, byte[]> implements Transmitter<T, byte[]> {
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
		dmsg.store(null, null);
		dmsg.addReceiver(portalID.getAID());
		
		// send data to target agent
		senderAgent.send(dmsg);
		dmsg.setByteSequenceContent(null);
	}

	public void signal(Signal signal) {
		DataMessage<Signal> dmsg = new DataMessage<Signal>();
		dmsg.setSinkId(Signal.class.toString());
		dmsg.store(signal, null);
		dmsg.addReceiver(portalID.getAID());
		senderAgent.send(dmsg);
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

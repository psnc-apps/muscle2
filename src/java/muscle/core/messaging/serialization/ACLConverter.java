/*
 * 
 */
package muscle.core.messaging.serialization;

import jade.lang.acl.ACLMessage;
import muscle.core.messaging.Message;
import muscle.core.messaging.jade.DataMessage;
import muscle.core.messaging.jade.ObservationMessage;

/**
 *
 * @author Joris Borgdorff
 */
public class ACLConverter<E> implements DataConverter<Message<E>, ACLMessage> {
	private final static String SINKID_KEY = DataMessage.class.toString() + "#sinkId";
	
	public ACLMessage serialize(Message<E> data) {
		if (data instanceof ACLMessage) {
			return (ACLMessage)data;
		}
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	public Message<E> deserialize(ACLMessage aclmsg) {
		String sid;
		// This message was not sent to be converted to a datamessage
		if ((sid = aclmsg.getUserDefinedParameter(ObservationMessage.OBSERVATION_KEY)) == null) {
			return null;
		}

		// copy some relevant settings from the 
		ObservationMessage<E> dmsg = new ObservationMessage<E>();
		dmsg.setSinkId(sid);
		dmsg.setSender(aclmsg.getSender());
		dmsg.setLanguage(aclmsg.getLanguage());
		dmsg.setProtocol(aclmsg.getProtocol());
		dmsg.setPerformative(aclmsg.getPerformative());
		dmsg.setEnvelope(aclmsg.getEnvelope());
		dmsg.setConversationId(aclmsg.getConversationId());
		dmsg.setByteSequenceContent(aclmsg.getByteSequenceContent());

		return dmsg;
	}
}

/*
 * 
 */
package muscle.core.messaging.serialization;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import muscle.core.ident.IDType;
import muscle.core.ident.JadeAgentID;
import muscle.core.ident.PortalID;
import muscle.core.messaging.jade.DataMessage;
import muscle.core.messaging.jade.ObservationMessage;

/**
 *
 * @author Joris Borgdorff
 */
public class ACLConverter<E> implements DataConverter<DataMessage<E>, ACLMessage> {
	public ACLMessage serialize(DataMessage<E> data) {
		if (data instanceof ACLMessage) {
			return (ACLMessage)data;
		}
		throw new IllegalArgumentException("Can only serialize ACLMessage");
	}
	
	public DataMessage<E> deserialize(ACLMessage aclmsg) {
		boolean obs = true;
		String sid = aclmsg.getUserDefinedParameter(ObservationMessage.OBSERVATION_KEY);
		if (sid == null) {
			obs = false;
			sid = aclmsg.getUserDefinedParameter(DataMessage.DATA_KEY);
		}
		// This message was not sent to be converted to a datamessage
		if (sid == null) {
			return null;
		}

		long byteCount = 0;
		E data = null;
		if (aclmsg.hasByteSequenceContent()) {
			// deserialize message content
			byte[] rawData = aclmsg.getByteSequenceContent();
			aclmsg.setByteSequenceContent(null);
			byteCount = rawData.length;

			data = new ByteDataConverter<E>().deserialize(rawData);
		} else {
			// can not handle empty DataMessage
			return null;
		}
		
		// copy some relevant settings from the
		String type = aclmsg.getUserDefinedParameter(ObservationMessage.TYPE_KEY);
		
		JadeAgentID recp = new JadeAgentID((AID)aclmsg.getAllReceiver().next());
		if (type.equals(IDType.port.toString())) {
			recp = new PortalID(sid, recp);
		}

		DataMessage<E> dmsg = obs ? new ObservationMessage() : new DataMessage<E>();
		dmsg.store(data, byteCount);
		dmsg.setRecipient(recp);

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

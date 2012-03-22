/*
 * 
 */
package muscle.core.messaging.serialization;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.Boot;
import muscle.core.ident.IDType;
import muscle.core.ident.JadeIdentifier;
import muscle.core.ident.Resolver;
import muscle.core.messaging.Observation;
import muscle.core.messaging.jade.DataMessage;
import muscle.core.messaging.jade.ObservationMessage;

/**
 * @author Joris Borgdorff
 */
public class ACLConverter<E extends Serializable> extends AbstractDataConverter<ObservationMessage<E>, ACLMessage> {
	private final DataConverter<Observation<E>,byte[]> byteConverter;
	
	public ACLConverter(DataConverter<Observation<E>,byte[]> byteConverter) {
		this.byteConverter = byteConverter;
	}
	
	public ACLMessage serialize(ObservationMessage<E> data) {
		if (data instanceof ACLMessage) {
			return (ACLMessage)data;
		}
		throw new IllegalArgumentException("Can only serialize ACLMessage");
	}
	
	public ObservationMessage<E> deserialize(ACLMessage aclmsg) {
		String sid = aclmsg.getUserDefinedParameter(ObservationMessage.OBSERVATION_KEY);
		if (sid == null) {
			sid = aclmsg.getUserDefinedParameter(DataMessage.DATA_KEY);
		}
		// This message was not sent to be converted to a datamessage
		if (sid == null) {
			return null;
		}

		long byteCount = 0;
		Observation<E> data = null;
		String signal = aclmsg.getUserDefinedParameter("signal");
		if (aclmsg.hasByteSequenceContent()) {
			// deserialize message content
			byte[] rawData = aclmsg.getByteSequenceContent();
			aclmsg.setByteSequenceContent(null);
			byteCount = rawData.length;

			data = byteConverter.deserialize(rawData);
		} else if (signal == null) {
			// If no data nor signal encountered, something is wrong.
			return null;
		}
		
		// copy some relevant settings from the
		String type = aclmsg.getUserDefinedParameter(ObservationMessage.TYPE_KEY);
		JadeIdentifier recp;
		try {
			Resolver r = Boot.getInstance().getResolver();
			recp = (JadeIdentifier)r.getIdentifier(sid, IDType.valueOf(type));
			if (!recp.isResolved()) {
				recp.resolve((AID)aclmsg.getAllReceiver().next(), null);
			}

			ObservationMessage<E> dmsg = new ObservationMessage<E>();
			dmsg.setSignal(signal);
			dmsg.store(data, byteCount);
			dmsg.setRecipient(recp);

			//dmsg.setSender(aclmsg.getSender());
			dmsg.setLanguage(aclmsg.getLanguage());
			dmsg.setProtocol(aclmsg.getProtocol());
			dmsg.setPerformative(aclmsg.getPerformative());
			dmsg.setEnvelope(aclmsg.getEnvelope());
			dmsg.setConversationId(aclmsg.getConversationId());
			dmsg.setByteSequenceContent(aclmsg.getByteSequenceContent());

			return dmsg;
		} catch (InterruptedException ex) {
			Logger.getLogger(ACLConverter.class.getName()).log(Level.SEVERE, "ACLConverter interrupted in search for resolver.", ex);
			return null;
		}
	}
}

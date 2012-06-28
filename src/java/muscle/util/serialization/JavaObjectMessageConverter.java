/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.util.serialization;

import java.io.Serializable;
import muscle.client.communication.message.BasicMessage;
import muscle.core.model.Observation;

/**
 *
 * @author Joris Borgdorff
 */
public class JavaObjectMessageConverter<T extends Serializable> extends AbstractDataConverter<BasicMessage<T>,BasicMessage<byte[]>> {
	private final ByteJavaObjectConverter<T> subconverter;
	public JavaObjectMessageConverter() {
		this.subconverter = new ByteJavaObjectConverter<T>();
	}
		
	@Override
	public BasicMessage<byte[]> serialize(BasicMessage<T> msg) {
		Observation<T> obs = msg.getObservation();
		byte[] data = this.subconverter.serialize(obs.getData());
		return new BasicMessage<byte[]>(data, obs.getTimestamp(), obs.getNextTimestamp(), msg.getRecipient());
	}

	@Override
	public BasicMessage<T> deserialize(BasicMessage<byte[]> msg) {
		Observation<byte[]> obs = msg.getObservation();
		T data = this.subconverter.deserialize(obs.getData());
		return new BasicMessage<T>(data, obs.getTimestamp(), obs.getNextTimestamp(), msg.getRecipient());
	}
}

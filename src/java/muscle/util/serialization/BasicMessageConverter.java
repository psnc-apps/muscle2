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
public class BasicMessageConverter<E extends Serializable,F extends Serializable> extends AbstractDataConverter<BasicMessage<E>,BasicMessage<F>> {
	private final DataConverter<E, F> subconverter;
	public BasicMessageConverter(DataConverter<E,F> subconverter) {
		this.subconverter = subconverter;
	}
	
	@Override
	public BasicMessage<F> serialize(BasicMessage<E> data) {
		if (data.isSignal()) {
			return (BasicMessage<F>)data;
		} else {
			Observation<E> obs = data.getObservation();
			F newData = subconverter.serialize(obs.getData());
			return new BasicMessage<F>(newData, obs.getTimestamp(), obs.getNextTimestamp(), data.getRecipient());
		}
	}

	@Override
	public BasicMessage<E> deserialize(BasicMessage<F> data) {
		if (data.isSignal()) {
			return (BasicMessage<E>)data;
		} else {
			Observation<F> obs = data.getObservation();
			E newData = subconverter.deserialize(obs.getData());
			return new BasicMessage<E>(newData, obs.getTimestamp(), obs.getNextTimestamp(), data.getRecipient());
		}
	}
}

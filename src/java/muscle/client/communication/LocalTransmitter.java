/*
 * 
 */
package muscle.client.communication;

import java.io.Serializable;
import muscle.client.communication.message.BasicMessage;
import muscle.client.communication.message.LocalDataHandler;
import muscle.client.communication.message.Message;
import muscle.client.communication.message.Signal;
import muscle.core.model.Observation;
import muscle.id.InstanceID;
import muscle.id.PortalID;
import muscle.util.serialization.DataConverter;

/**
 *
 * @author Joris Borgdorff
 */
public class LocalTransmitter<T extends Serializable> extends AbstractCommunicatingPoint<Observation<T>, Observation<T>> implements Transmitter<T, Observation<T>> {
	private final LocalDataHandler dataHandler;
	
	public LocalTransmitter(LocalDataHandler dataHandler, DataConverter<Observation<T>, Observation<T>> converter, PortalID portalID) {
		super(converter, portalID);
		this.dataHandler = dataHandler;
	}

	public final void transmit(Observation<T> obs) {
		// If converter is null here, we made a mistake creating the transmitter.
		// We need to copy here, so that the data on the receiving end is independent from the sending end.
		Message<T> msg = new BasicMessage<T>(converter.serialize(obs), portalID);
		this.dataHandler.put(msg);
	}

	public final void signal(Signal signal) {
		@SuppressWarnings("unchecked")
		Message<T> msg = new BasicMessage(signal, portalID);
		this.dataHandler.put(msg);
	}
}

/*
 * 
 */
package muscle.client.communication;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.client.communication.message.*;
import muscle.core.model.Observation;
import muscle.id.InstanceID;
import muscle.id.PortalID;

/**
 *
 * @author Joris Borgdorff
 */
public class LocalTransmitter<T extends Serializable> extends AbstractCommunicatingPoint<Observation<T>, Observation<T>,InstanceID,PortalID<InstanceID>> implements Transmitter<T, Observation<T>,InstanceID,PortalID<InstanceID>> {
	private final static Logger logger = Logger.getLogger(LocalTransmitter.class.getName());
	private final LocalDataHandler dataHandler;
	
	public LocalTransmitter(LocalDataHandler dataHandler) {
		this.dataHandler = dataHandler;
	}

	// Synchronized: can only transmit one signal or message at a time.
	public synchronized void transmit(Observation<T> obs) {
		if (this.isDisposed()) {
			logger.log(Level.WARNING, "Transmitter is disposed of; unable to send observation to {0}", portalID);
			return;
		}
		// If converter is null here, we made a mistake creating the transmitter.
		// We need to copy here, so that the data on the receiving end is independent from the sending end.
		Message<T> msg = new BasicMessage<T>(converter.serialize(obs), portalID);
		this.dataHandler.put(msg);
	}

	// Synchronized: can only transmit one signal or message at a time.
	public synchronized void signal(Signal signal) {
		if (this.isDisposed()) {
			if (!(signal instanceof DetachConduitSignal))
				logger.log(Level.WARNING, "Transmitter is disposed of; unable to send signal {0} to {1}", new Object[] {signal, portalID});

			return;
		}
		Message<T> msg = new BasicMessage(signal, portalID);
		this.dataHandler.put(msg);
	}
}

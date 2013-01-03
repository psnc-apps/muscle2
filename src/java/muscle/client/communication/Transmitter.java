/*
 * 
 */
package muscle.client.communication;

import java.io.Serializable;
import muscle.client.communication.message.Signal;
import muscle.core.model.Observation;

/**
 *
 * @author jborgdo1
 */
public interface Transmitter<E extends Serializable,F> extends CommunicatingPoint<Observation<E>,F> {
	public void signal(Signal signal);
	public void transmit(Observation<E> msg);
}

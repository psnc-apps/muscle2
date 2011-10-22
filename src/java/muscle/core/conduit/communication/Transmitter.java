/*
 * 
 */
package muscle.core.conduit.communication;

import muscle.core.messaging.Message;
import muscle.core.messaging.signal.Signal;

/**
 *
 * @author jborgdo1
 */
public interface Transmitter<E,F> extends CommunicatingPoint<E, F> {
	public void signal(Signal signal);
	public void transmit(Message<E> signal);
}

/*
 * 
 */
package muscle.core.messaging;

import muscle.core.messaging.signal.Signal;

/**
 * @author Joris Borgdorff
 */
public interface Message<E> {
	public E getData();
	public Observation<E> getObservation();
	public Timestamp getTimestampNextEvent();
}

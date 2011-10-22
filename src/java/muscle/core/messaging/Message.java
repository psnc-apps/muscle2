/*
 * 
 */
package muscle.core.messaging;

import muscle.core.ident.Identifier;
import muscle.core.wrapper.Observation;

/**
 * @author Joris Borgdorff
 */
public interface Message<E> {
	public E getRawData();
	public Observation<E> getObservation();
	public Timestamp getTimestampNextEvent();
	public Identifier getRecipient();
}

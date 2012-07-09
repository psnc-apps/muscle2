/*
 * 
 */
package muscle.client.communication.message;

import java.io.Serializable;
import muscle.id.Identifier;
import muscle.core.model.Observation;

/**
 * @author Joris Borgdorff
 */
public interface Message<E extends Serializable> {
	public E getRawData();
	public Observation<E> getObservation();
	public Identifier getRecipient();
	public boolean isSignal();
	public Signal getSignal();
}

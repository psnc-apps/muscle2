/*
 * 
 */
package muscle.client.communication.message;

import java.io.Serializable;
import muscle.core.model.Observation;
import muscle.id.Identifier;

/**
 * @author Joris Borgdorff
 */
public interface Message<E extends Serializable> extends Serializable {
	public E getRawData();
	public Observation<E> getObservation();
	public Identifier getRecipient();
	public boolean isSignal();
	public Signal getSignal();
}

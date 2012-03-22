/*
 * 
 */
package muscle.core.messaging;

import java.io.Serializable;
import muscle.core.ident.Identifier;
import muscle.core.messaging.signal.Signal;

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

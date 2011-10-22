/*
 * 
 */
package muscle.core.messaging;

import java.io.Serializable;
import muscle.core.ident.Identifier;
import muscle.core.wrapper.Observation;

/**
 *
 * @author Joris Borgdorff
 */
public class BasicMessage<E> implements Message<E>, Serializable {
	private final Observation<E> obs;
	private final Identifier recv;
	
	public BasicMessage(E data, Timestamp time, Timestamp nextTime, Identifier recipient) {
		this (new Observation<E>(data, time, nextTime), recipient);
	}
	
	public BasicMessage(Observation<E> obs, Identifier recipient) {
		this.obs = obs;
		this.recv = recipient;
	}

	public E getRawData() {
		return obs.getData();
	}

	public Observation<E> getObservation() {
		return obs;
	}

	public Identifier getRecipient() {
		return this.recv;
	}
}

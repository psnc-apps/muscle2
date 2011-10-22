/*
 * 
 */
package muscle.core.messaging;

import java.io.Serializable;
import muscle.core.wrapper.Observation;

/**
 *
 * @author Joris Borgdorff
 */
public class BasicMessage<E> implements Message<E>, Serializable {
	private final Observation<E> obs;
	private final Timestamp nextTime;
	
	public BasicMessage(E data, Timestamp time, Timestamp nextTime) {
		this (new Observation<E>(data, time), nextTime);
	}
	
	public BasicMessage(Observation<E> obs, Timestamp nextTime) {
		this.obs = obs;
		this.nextTime = nextTime;
	}

	public E getRawData() {
		return obs.getData();
	}

	public Observation<E> getObservation() {
		return obs;
	}

	public Timestamp getTimestampNextEvent() {
		return nextTime;
	}
}

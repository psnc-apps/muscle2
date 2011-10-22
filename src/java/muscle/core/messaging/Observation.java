/*
 * 
 */
package muscle.core.messaging;

import java.io.Serializable;

/**
 *
 * @author Joris Borgdorff
 */
public class Observation<T> implements Serializable {
	private final Timestamp timestamp;
	private final T data;

	public Observation(T data, Timestamp time) {
		this.data = data;
		this.timestamp = time;
	}
	
	public T getData() {
		return this.data;
	}
	
	public Timestamp getTimestamp() {
		return this.timestamp;
	}
}

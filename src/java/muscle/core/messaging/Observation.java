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
	private Timestamp timestamp;
	private T data;

	public T getData() {
		return this.data;
	}
	
	public void setData(T data) {
		this.data = data;
	}
	
	public Timestamp getTimestamp() {
		return this.timestamp;
	}
	
	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}
}

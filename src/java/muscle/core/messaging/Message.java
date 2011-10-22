/*
 * 
 */
package muscle.core.messaging;

/**
 * @author Joris Borgdorff
 */
public interface Message<E> {
	public E getData();
	public Observation<E> getObservation();
	public Timestamp getTimestampNextEvent();
}

/*
 * 
 */
package muscle.core.messaging;

<<<<<<< HEAD
import muscle.core.ident.Identifier;
import muscle.core.wrapper.Observation;

=======
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0
/**
 * @author Joris Borgdorff
 */
public interface Message<E> {
<<<<<<< HEAD
	public E getRawData();
	public Observation<E> getObservation();
	public Identifier getRecipient();
=======
	public E getData();
	public Observation<E> getObservation();
	public Timestamp getTimestampNextEvent();
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0
}

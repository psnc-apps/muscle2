/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.core.messaging.jade;

import muscle.core.messaging.Message;
import muscle.core.messaging.Timestamp;
import muscle.core.wrapper.Observation;

/**
 *
 * @author Joris Borgdorff
 */
public class ObservationMessage<E> extends DataMessage<Observation<E>> implements Message<E> {
	public final static String MESSAGE_TYPE_KEY = ObservationMessage.class.toString() + "#sinkId";
	
	public ObservationMessage(String sid) {
		super(MESSAGE_TYPE_KEY, sid);
	}
	
	public E getRawData() {
		Observation<E> obs = this.getObservation();
		return obs.getData();
	}

	public Observation<E> getObservation() {
		return getData();
	}

	public Timestamp getTimestampNextEvent() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}

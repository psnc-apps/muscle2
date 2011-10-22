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
	public final static String OBSERVATION_KEY = ObservationMessage.class.toString() + "#sinkId";
	
	public void setSinkId(String sid) {
		setSinkId(OBSERVATION_KEY, sid);
	}
	
	public E getRawData() {
		return getData().getData();
	}

	public Observation<E> getObservation() {
		return getData();
	}

	public Timestamp getTimestampNextEvent() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}

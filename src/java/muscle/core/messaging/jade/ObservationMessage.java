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
	public final static String OBSERVATION_KEY = ObservationMessage.class.getName() + "#id";
	
	@Override
	protected void setIdentifierString(String name, String type, String portname) {
		this.setIdentifierString(OBSERVATION_KEY, name, type, portname);
	}
	
	public E getRawData() {
		return getData().getData();
	}

	public Observation<E> getObservation() {
		return getData();
	}
}

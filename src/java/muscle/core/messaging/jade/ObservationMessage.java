/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.core.messaging.jade;

import java.io.Serializable;
import muscle.core.messaging.Message;
import muscle.core.messaging.Observation;

/**
 *
 * @author Joris Borgdorff
 */
public class ObservationMessage<E extends Serializable> extends DataMessage<Observation<E>> implements Message<E> {
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

package examples.pingpongsubmodel;

import java.io.Serializable;
import muscle.core.kernel.Submodel;
import muscle.core.model.Observation;
import muscle.core.model.Timestamp;

/**
 * Receives at 'in' exit byte array and writes them to 'out' exit
 */
public class Pong extends Submodel {
	private Serializable data;

	protected Timestamp init(Timestamp previousTime) {
		Observation obs = in("in").receiveObservation();
		data = obs.getData();
		return obs.getTimestamp();
	}
	
	@SuppressWarnings("unchecked")
	protected void finalObservation() {
		out("out").send(data);
	}
	
	protected boolean restartSubmodel() {
		return in("in").hasNext();
	}
}

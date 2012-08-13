/*
 * 
 */

package examples.simplesubmodel;

import muscle.core.kernel.Submodel;
import muscle.core.model.Timestamp;

/**
 *
 * @author Joris Borgdorff
 */
public class Sender extends Submodel {
	private double[][] data;
	
	protected Timestamp init(Timestamp previousTime) {
		data = new double[][] {{2, 1}, {3, 2}};
		return super.init(previousTime);
	}
	
	@SuppressWarnings("unchecked")
	protected void intermediateObservation() {
		out("data").send(data);
	}
}

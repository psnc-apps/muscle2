/*
 * 
 */

package examples.simplesubmodel;

import muscle.core.Scale;
import muscle.core.kernel.Submodel;
import muscle.core.messaging.Duration;

/**
 *
 * @author Joris Borgdorff
 */
public class Sender extends Submodel {
	private double[][] data;
	
	protected void init() {
		data = new double[2][2];
		data[0][0] = 2d;
		data[1][0] = 3d;
		data[0][1] = 1d;
		data[1][1] = 2d;
	}
	
	protected void intermediateObservation() {
		out("data").send(data);
	}
	
	public Scale getScale() {
		return new Scale(new Duration(1d));
	}
}

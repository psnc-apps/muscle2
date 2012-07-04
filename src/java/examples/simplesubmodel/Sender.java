/*
 * 
 */

package examples.simplesubmodel;

import muscle.core.Scale;
import muscle.core.kernel.Submodel;
import muscle.core.model.Distance;

/**
 *
 * @author Joris Borgdorff
 */
public class Sender extends Submodel {
	private double[][] data;
	
	protected void init() {
		data = new double[][] {{2, 1}, {3, 2}};
	}
	
	protected void intermediateObservation() {
		out("data").send(data);
	}
	
	public Scale getScale() {
		return new Scale(new Distance(1d));
	}
}

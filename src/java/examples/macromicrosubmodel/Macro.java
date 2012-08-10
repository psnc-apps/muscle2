/*
 * 
 */

package examples.macromicrosubmodel;

import muscle.core.kernel.Submodel;
import muscle.core.model.Timestamp;

/**
 *
 * @author Joris Borgdorff
 */
public class Macro extends Submodel {
	private double[][] data;
	
	protected Timestamp init(Timestamp previousTime) {
		data = new double[][] {{2, 1}, {3, 2}};
		
		// Use default reasoning for initial time
		return super.init(previousTime);
	}
	
	@SuppressWarnings("unchecked")
	protected void intermediateObservation() {
		out("macroObs").send(data);
	}
	
	protected void solvingStep() {
		data[0][0] = (Double)in("microObs").receive();
		System.out.println("Macro will use value " + data[0][0] + " from micro");
	}
}

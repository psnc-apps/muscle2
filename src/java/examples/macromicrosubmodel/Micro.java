/*
 * 
 */

package examples.macromicrosubmodel;

import muscle.core.kernel.Submodel;
import muscle.core.model.Observation;
import muscle.core.model.Timestamp;

/**
 *
 * @author Joris Borgdorff
 */
public class Micro extends Submodel {
	private double firstValue;
	
	protected Timestamp init(Timestamp previousTime) {
		Observation obs = in("macroObs").receiveObservation();
		double[][] data = (double[][])obs.getData();
		System.out.println("Got matrix:");
		System.out.println("(" + data[0][0] + "\t" + data[1][0] + "\t)");
		System.out.println("(" + data[0][1] + "\t" + data[1][1] + "\t)");
		System.out.println();
		firstValue = data[0][0];
		
		// Initialize (t_0) at time of initial message
		return obs.getTimestamp();
	}
	
	
	protected void solvingStep() {
		firstValue++;
	}
	
	protected void finalObservation() {
		out("microObs").send(firstValue);
	}
	
	protected boolean restartSubmodel() {
		return in("macroObs").hasNext();
	}
}

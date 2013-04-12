/*
 * 
 */

package examples.terminals;

import muscle.core.kernel.Submodel;
import muscle.core.model.Timestamp;

/**
 *
 * @author Joris Borgdorff
 */
public class Processor extends Submodel {
	double[] data;
	
	@Override
	public Timestamp init(Timestamp prevOrigin) {
		data = (double[]) in("initialData").receive();
		return super.init(prevOrigin);
	}
	
	@Override
	public void intermediateObservation() {
		boolean[] isLarge = new boolean[data.length];
		for (int i = 0; i < data.length; i++) {
			isLarge[i] = (data[i] > 2);
		}
		log("Sending data...");
		out("largeMask").send(isLarge);
	}

	@Override
	public void solvingStep() {
		log("Processing data...");
		for (int i = 0; i < data.length; i++) {
			data[i]++;
		}
	}
}

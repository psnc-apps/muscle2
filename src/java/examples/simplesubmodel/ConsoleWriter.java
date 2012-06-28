/*
 * 
 */

package examples.simplesubmodel;

import muscle.core.Scale;
import muscle.core.kernel.Submodel;
import muscle.core.model.Duration;

/**
 *
 * @author Joris Borgdorff
 */
public class ConsoleWriter extends Submodel {
	protected void solvingStep() {
		double[][] data = (double[][])in("data").receive();
		
		System.out.println("Got matrix:");
		System.out.println("(" + data[0][0] + "\t" + data[1][0] + "\t)");
		System.out.println("(" + data[0][1] + "\t" + data[1][1] + "\t)");
		System.out.println();
	}

	public Scale getScale() {
		return new Scale(new Duration(1d));
	}
}

package examples.pingpongsubmodel;

import muscle.core.ConduitExitController;
import muscle.core.kernel.FanInMapper;

/**
 *
 * @author Joris Borgdorff
 */
public class PongCombiner extends FanInMapper {
	@Override
	protected void receiveAll() {
		for (ConduitExitController ec : this.exits.values()) {
			// Overwrite it and take the last value
			value = ec.getExit().receiveObservation();
		}
	}
}

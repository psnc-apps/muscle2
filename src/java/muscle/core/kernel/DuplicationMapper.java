/*
 * 
 */

package muscle.core.kernel;

import muscle.core.ConduitEntranceController;

/**
 *
 * @author Joris Borgdorff
 */
public class DuplicationMapper extends FanOutMapper {
	protected void writeAll() {
		for (ConduitEntranceController ec : this.entrances.values()) {
			ec.getEntrance().send(value);
		}
	}
}

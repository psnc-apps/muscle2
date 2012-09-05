/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.core.conduit.terminal;

import muscle.core.model.Observation;

/**
 * A sink that discards all information
 * @author Joris Borgdorff
 */
public class NullSink extends Sink {
	@Override
	public void send(Observation msg) {
		// Return void
	}
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.core.conduit.communication;

import muscle.core.messaging.Observation;
import muscle.core.messaging.signal.Signal;

/**
 *
 * @author Joris Borgdorff
 */
class LocalTransmitter extends AbstractCommunicatingPoint implements Transmitter {
	public LocalTransmitter() {
	}

	@Override
	public void signal(Signal signal) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void transmit(Observation msg) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}

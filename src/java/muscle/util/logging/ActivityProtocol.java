/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.util.logging;

import muscle.util.serialization.Protocol;
import muscle.util.serialization.ProtocolSerializer;

/**
 *
 * @author joris
 */
public enum ActivityProtocol implements Protocol {
	BEGIN_SEND(1), END_SEND(2), BEGIN_RECEIVE(3), END_RECEIVE(4), START(5), STOP(6), CONNECTED(7), RECEIVE_FAILED(8);

	private final int num;
	public final static ProtocolSerializer<ActivityProtocol> handler = new ProtocolSerializer<ActivityProtocol>(values());
	ActivityProtocol(int n) { num = n; }
	@Override
	public int intValue() { return num; }
}

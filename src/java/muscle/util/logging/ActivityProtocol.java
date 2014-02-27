/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.util.logging;

import muscle.id.IDType;
import muscle.util.serialization.Protocol;
import muscle.util.serialization.ProtocolSerializer;

/**
 *
 * @author joris
 */
public enum ActivityProtocol implements Protocol {
	INIT(0, IDType.container), FINALIZE(9, IDType.container),
	BEGIN_SEND(1, IDType.port), END_SEND(2, IDType.port), CONNECTED(7, IDType.port),
	BEGIN_RECEIVE(3, IDType.port), END_RECEIVE(4, IDType.port), RECEIVE_FAILED(8, IDType.port),
	START(5, IDType.instance), STOP(6, IDType.instance);
	
	private final int num;
	public final IDType type;
	public final static ProtocolSerializer<ActivityProtocol> handler = new ProtocolSerializer<ActivityProtocol>(values());
	ActivityProtocol(int n, IDType t) { num = n; type = t; }
	@Override
	public int intValue() { return num; }
}

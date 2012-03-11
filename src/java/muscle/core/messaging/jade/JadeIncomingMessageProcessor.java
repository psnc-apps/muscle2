/*
Copyright 2008,2009 Complex Automata Simulation Technique (COAST) consortium

GNU Lesser General Public License

This file is part of MUSCLE (Multiscale Coupling Library and Environment).

MUSCLE is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

MUSCLE is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
 */
package muscle.core.messaging.jade;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.conduit.communication.JadeReceiver;
import muscle.core.ident.Identifier;
import muscle.core.messaging.serialization.ACLConverter;
import muscle.core.messaging.serialization.ByteJavaObjectConverter;
import utilities.data.ArrayMap;

/**
process the agent message queue from a sub-thread of the agents main thread
this allows us to actively push messages to their individual sinks
@author Jan Hegewald
 */
public class JadeIncomingMessageProcessor extends CyclicBehaviour {
	private final Map<Identifier, JadeReceiver> receivers;
	private static final Logger logger = Logger.getLogger(JadeIncomingMessageProcessor.class.getName());
	private final Agent owner;
	private final ACLConverter deserializer;

	public JadeIncomingMessageProcessor(Agent owner) {
		this.receivers = new ArrayMap<Identifier, JadeReceiver>();
		this.owner = owner;
		this.deserializer = new ACLConverter(new ByteJavaObjectConverter());
	}
	
	public void addReceiver(Identifier id, JadeReceiver recv) {
		this.receivers.put(id, recv);
	}
	
	/** Deserialize, process, and send message to receiver*/
	public void action() {
		ACLMessage msg = owner.receive();
		while (msg != null) {
			DataMessage dmsg = deserializer.deserialize(msg);
			if (dmsg != null) {
				Identifier id = dmsg.getRecipient();
				JadeReceiver recv = receivers.get(id);
				if (recv == null) {
					if (msg.getUserDefinedParameter("signal") != null) {
						logger.log(Level.INFO, "signal intended for removed agent {0} received: {1}", new Object[] {id, msg.getUserDefinedParameter("signal")});
					}
					else {
						logger.log(Level.SEVERE, "no source for <{0}> found, dropping data message", id);
					}
				}
				else {
					recv.put(dmsg);
				}
			}
			msg = owner.receive();
		}
		block();
	}
	
	/**
	 * maximum buffer size in bytes
	 */
	protected static long suggestedMaxBufferSize() {
		return 1042 * 1042 * 800; // 800MB
	}
}

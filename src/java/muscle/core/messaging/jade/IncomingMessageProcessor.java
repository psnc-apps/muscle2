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

import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.conduit.communication.JadeReceiver;
import muscle.core.ident.Identifier;
import muscle.core.ident.JadeAgentID;
import utilities.ArrayMap;
import utilities.SafeQueueConsumerThread;

/**
process the agent message queue from a sub-thread of the agents main thread
this allows us to actively push messages to their individual sinks
@author Jan Hegewald
 */
public class IncomingMessageProcessor extends SafeQueueConsumerThread<DataMessage<?>> {
	private final Map<Identifier, JadeReceiver> receivers;
	private static final Logger logger = Logger.getLogger(IncomingMessageProcessor.class.getName());

	public IncomingMessageProcessor(Queue<DataMessage<?>> newQueue) {
		receivers = new ArrayMap<Identifier, JadeReceiver>();
		super.setIncomingQueue(newQueue);
	}
	
	public void addReceiver(Identifier id, JadeReceiver recv) {
		this.receivers.put(id, recv);
	}
	
	@Override
	protected void execute(DataMessage dmsg) {
		if (dmsg != null) {
			JadeAgentID id = dmsg.getRecipient();
			JadeReceiver recv = receivers.get(id);
			if (recv == null)
				logger.log(Level.SEVERE, "no source for <{0}> found, dropping data message", id);

			recv.put(dmsg);
		}
	}

	public void start() {
		Logger.getLogger(IncomingMessageProcessor.class.getName()).log(Level.INFO, "starting {0}", getClass());
		super.start();
	}

	@Override
	protected void handleInterruption(InterruptedException ex) {
		throw new RuntimeException(ex);
	}
	
	/**
	 * maximum buffer size in bytes
	 */
	protected static long suggestedMaxBufferSize() {
		return 1042 * 1042 * 800; // 800MB
	}
}

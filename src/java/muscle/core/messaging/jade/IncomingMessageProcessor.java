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

import java.util.Queue;
import java.util.logging.Level;
import muscle.core.MultiDataAgent;
import muscle.core.conduit.filter.QueueConsumer;
import muscle.core.messaging.signal.Signal;
import utilities.MiscTool;

/**
process the agent message queue from a sub-thread of the agents main thread
this allows us to actively push messages to their individual sinks
@author Jan Hegewald
 */
public class IncomingMessageProcessor extends java.lang.Thread implements QueueConsumer<DataMessage> {
	private final MultiDataAgent owner;
	private boolean shouldRun = true;
	private final Queue<DataMessage> queue;

	public IncomingMessageProcessor(MultiDataAgent newOwner, Queue<DataMessage> newQueue) {
		owner = newOwner;
		queue = newQueue;
	}

	@Override
	public void apply() {
		if (shouldRun) {
			synchronized(this) {
				if (shouldRun) {
					notifyAll();
				}
			}
		}
	}
	
	public synchronized void pause() {
		shouldRun = false;
		this.notifyAll();
	}

	@Override
	public void run() {
		owner.getLogger().log(Level.INFO, "starting {0}", getClass());

		while (shouldRun) {
			// blocking poll for next message

			DataMessage dmsg = null;
			synchronized (this) {
				while (shouldRun && ((dmsg = queue.poll()) == null)) {
					try {
						this.wait();
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}

				if (dmsg != null) {
					put(dmsg);
				}
			}
		}
	}

	private void put(DataMessage dmsg) {
		// deserialize message content and store it in the message object
		long byteCount = 0;
		if (dmsg.hasByteSequenceContent()) {
			Object data = null;
			// deserialize message content
			byte[] rawData = dmsg.getByteSequenceContent();
			dmsg.setByteSequenceContent(null);
			byteCount = rawData.length;

			data = MiscTool.deserialize(rawData);
//			data = MiscTool.gunzip(rawData);
			rawData = null;

			dmsg.store(data, byteCount);
		} else {
			throw new muscle.exception.MUSCLERuntimeException("[" + owner.getLocalName() + "] can not handle empty DataMessage");
		}
		
		if (dmsg.getSinkID().equals(Signal.class.toString())) {
			owner.handleRemoteSignal(dmsg);
		}
		else {
			owner.handleDataMessage(dmsg, byteCount);
		}
	}
	
	@Override
	public void setIncomingQueue(Queue<DataMessage> queue) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}

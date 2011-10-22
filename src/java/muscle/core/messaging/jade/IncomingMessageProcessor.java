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
import muscle.core.messaging.serialization.ByteDataConverter;
import muscle.core.messaging.signal.Signal;
import utilities.SafeThread;

/**
process the agent message queue from a sub-thread of the agents main thread
this allows us to actively push messages to their individual sinks
@author Jan Hegewald
 */
public class IncomingMessageProcessor extends SafeThread implements QueueConsumer<ObservationMessage<?>> {
	private final MultiDataAgent owner;
	private final Queue<ObservationMessage<?>> queue;

	public IncomingMessageProcessor(MultiDataAgent newOwner, Queue<ObservationMessage<?>> newQueue) {
		owner = newOwner;
		queue = newQueue;
	}

	@Override
	public synchronized void apply() {
		this.notifyAll();
	}
	
	@Override
	protected void execute() throws InterruptedException {
		ObservationMessage dmsg = queue.remove();
		
		if (dmsg != null) {
			put(dmsg);
		}
	}
	
	/**
	 * Wait until queue is non-empty. Returns false if the thread should halt.
	 */
	protected synchronized boolean continueComputation() throws InterruptedException {
		while (!isDone && queue.isEmpty()) {
			wait();
		}
		return !isDone;
	}

	public void start() {
		owner.getLogger().log(Level.INFO, "starting {0}", getClass());
		super.start();
	}

	private void put(ObservationMessage dmsg) {
		// deserialize message content and store it in the message object
		long byteCount = 0;
		if (dmsg.hasByteSequenceContent()) {
			Object data = null;
			// deserialize message content
			byte[] rawData = dmsg.getByteSequenceContent();
			dmsg.setByteSequenceContent(null);
			byteCount = rawData.length;

			data = new ByteDataConverter().deserialize(rawData);
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
	public void setIncomingQueue(Queue<ObservationMessage<?>> queue) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	protected void handleInterruption(InterruptedException ex) {
		throw new RuntimeException(ex);
	}
}

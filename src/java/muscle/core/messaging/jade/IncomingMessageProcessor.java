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
<<<<<<< HEAD
import java.util.logging.Logger;
import muscle.core.conduit.communication.JadeReceiver;
import muscle.core.ident.Identifier;
import muscle.core.messaging.serialization.ACLConverter;
import muscle.core.messaging.serialization.ByteJavaObjectConverter;
import utilities.ArrayMap;
=======
import muscle.core.MultiDataAgent;
import muscle.core.conduit.filter.QueueConsumer;
import muscle.core.messaging.serialization.ByteDataConverter;
import muscle.core.messaging.signal.Signal;
import utilities.MiscTool;
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0

/**
process the agent message queue from a sub-thread of the agents main thread
this allows us to actively push messages to their individual sinks
@author Jan Hegewald
 */
<<<<<<< HEAD
public class IncomingMessageProcessor extends CyclicBehaviour {
	private final Map<Identifier, JadeReceiver> receivers;
	private static final Logger logger = Logger.getLogger(IncomingMessageProcessor.class.getName());
	private final Agent owner;
	private final ACLConverter deserializer;

	public IncomingMessageProcessor(Agent owner) {
		this.receivers = new ArrayMap<Identifier, JadeReceiver>();
		this.owner = owner;
		this.deserializer = new ACLConverter(new ByteJavaObjectConverter());
=======
public class IncomingMessageProcessor extends java.lang.Thread implements QueueConsumer<DataMessage<?>> {
	private final MultiDataAgent owner;
	private boolean shouldRun = true;
	private final Queue<DataMessage<?>> queue;

	public IncomingMessageProcessor(MultiDataAgent newOwner, Queue<DataMessage<?>> newQueue) {
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
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0
	}
	
	public void addReceiver(Identifier id, JadeReceiver recv) {
		this.receivers.put(id, recv);
	}
	
	/** Deserialize, process, and send message to receiver*/
	public void action() {
		ACLMessage msg = owner.receive();
		if (msg != null) {
			DataMessage dmsg = deserializer.deserialize(msg);
			if (dmsg != null) {
				Identifier id = dmsg.getRecipient();
				JadeReceiver recv = receivers.get(id);
				if (recv == null) {
					logger.log(Level.SEVERE, "no source for <{0}> found, dropping data message", id);
				}
				else {
					recv.put(dmsg);
				}
			}
		}
<<<<<<< HEAD
		block();
	}
	
	/**
	 * maximum buffer size in bytes
	 */
	protected static long suggestedMaxBufferSize() {
		return 1042 * 1042 * 800; // 800MB
=======
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
	public void setIncomingQueue(Queue<DataMessage<?>> queue) {
		throw new UnsupportedOperationException("Not supported yet.");
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0
	}
}

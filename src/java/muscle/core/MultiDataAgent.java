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
package muscle.core;

import jade.core.AID;

import java.util.logging.Level;
import muscle.logging.AgentLogger;

import java.util.logging.Logger;
import jade.core.Location;
import jade.core.MessageQueue;
import muscle.core.messaging.jade.IncomingMessageProcessor;
import muscle.core.messaging.jade.ObservationMessage;
import muscle.core.messaging.signal.Signal;
import muscle.core.messaging.signal.QueueLimitExceededSignal;
import muscle.core.messaging.signal.QueueWithinLimitSignal;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import javatool.ClassTool;
import muscle.core.ident.Identifier;
import muscle.core.ident.JadeAgentID;
import muscle.core.kernel.InstanceController;
import muscle.core.messaging.Message;
import muscle.core.messaging.SinkObserver;
import muscle.core.messaging.jade.DataMessage;
import muscle.core.messaging.jade.SortingMessageQueue;
import muscle.core.messaging.serialization.ByteDataConverter;
import muscle.utilities.ObservableLinkedBlockingQueue;

/**
JADE agent which filters incoming data messages and passes them to multiple message sinks
@author Jan Hegewald
 */
public abstract class MultiDataAgent extends jade.core.Agent implements SinkObserver<ObservationMessage<?>>, InstanceController {
	private transient IncomingMessageProcessor messageProcessor;
	private static final transient Logger logger = AgentLogger.getLogger(MultiDataAgent.class.getName());
	private volatile long bufferSizeCount = 0;
	private ObservableLinkedBlockingQueue<ObservationMessage<?>> nonACLQueue = new ObservableLinkedBlockingQueue<ObservationMessage<?>>();
	private final List<AID> toldToPauseList = Collections.synchronizedList(new LinkedList<AID>());

	private List<ConduitExitController<?>> dataSources = new ArrayList<ConduitExitController<?>>(); // these are the conduit exits
	private List<ConduitEntranceController<?>> dataSinks = new ArrayList<ConduitEntranceController<?>>(); // these are the conduit entrances
	
	public void addSink(ConduitEntranceController<?> s) {
		dataSinks.add(s);
	}

	public void addSource(ConduitExitController<?> s) {
		dataSources.add(s);
	}

	public Identifier getID() {
		return new JadeAgentID(this.getAID());
	}
	
	@Override
	public void takeDown() {
		messageProcessor.pause();
		logger.log(Level.INFO, "waiting for {0} to join", messageProcessor.getClass());
		try {
			messageProcessor.join();
		} catch (java.lang.InterruptedException e) {
			throw new muscle.exception.MUSCLERuntimeException(e);
		}
		messageProcessor = null;
	}

	@Override
	protected void setup() {
		initTransients();
	}

	protected void afterMove() {
		initTransients();
	}

	protected void initTransients() {
		messageProcessor = new IncomingMessageProcessor(this, nonACLQueue);
		nonACLQueue.setQueueConsumer(messageProcessor);
		messageProcessor.start();
	}

	@Override
	protected MessageQueue createMessageQueue() {
		return new SortingMessageQueue(nonACLQueue);
	}

	/**
	maximum buffer size in bytes
	 */
	protected long suggestedMaxBufferSize() {
		return 1042 * 1042 * 800; // 800MB
	}

	/**
	returns our logger
	 */
	public Logger getLogger() {
		return logger;
	}

	/**
	custom implementation of Agent#doMove to deny moving this agent if inappropriate
	 */
	@Override
	public void doMove(Location destination) {
		if (ClassTool.isNative(getClass(), jade.core.Agent.class)) {
			System.out.println(getLocalName() + " is native");
			logger.log(Level.WARNING, "not allowed to move to <{0}>", destination);
			return;
		} else {
			super.doMove(destination);
		}
	}

	/**
	custom implementation of Agent#doClone to deny moving this agent if inappropriate
	 */
	@Override
	public void doClone(Location destination, String newName) {
		if (ClassTool.isNative(getClass(), jade.core.Agent.class)) {
			logger.log(Level.WARNING, "not allowed to clone to <{0}>", destination);
			return;
		} else {
			super.doClone(destination, newName);
		}
	}

	// todo: data serialization in separate thread
	public <E> void sendMessage(Message<E> msg) {
		ObservationMessage<E> dmsg = null;
		if (msg instanceof ObservationMessage) {
			dmsg = (ObservationMessage<E>)msg;
		}
		assert !dmsg.hasByteSequenceContent();

		byte[] rawData = null;
		rawData = new ByteDataConverter<E>().serialize(dmsg.getData());
// 		rawData = MiscTool.gzip(dmsg.getStored());

		dmsg.setByteSequenceContent(rawData);
		dmsg.store(null, null);

		// send data to target agent	
		send(dmsg);
		dmsg.setByteSequenceContent(null);
	}

	// an incoming DataMessage has arrived at this agent
	public void handleDataMessage(ObservationMessage dmsg, long byteCount) {

		// put message to its sink
		// there we process all generic data messages
		// see if this message is intended for one of our other sinks
		for (Iterator<ConduitExitController<?>> sourceIterator = dataSources.iterator(); sourceIterator.hasNext();) {
			ConduitExitController<?> source = sourceIterator.next();

			if (source.id().equals(dmsg.getSinkID())) {

				// only add to buffer counter for generic data messages
				// administrative messages like SignalMessage or ACLMessage are not added
				synchronized (toldToPauseList) {
					bufferSizeCount += byteCount;
				}

				if (bufferSizeCount >= suggestedMaxBufferSize()) {

					if ((bufferSizeCount - byteCount) <= 0) {
						throw new muscle.exception.MUSCLERuntimeException("configuration error: [" + getLocalName() + "] does not accept a data message because its buffer size is too limited");
					}

					// send pause signal to source kernel
					synchronized (toldToPauseList) {
						if (!toldToPauseList.contains(dmsg.getSender())) {
							QueueLimitExceededSignal s = new QueueLimitExceededSignal();
							sendRemoteSignal(s, dmsg.getSender());
							toldToPauseList.add(dmsg.getSender());
						}
					}
				}

				source.put(dmsg);
				return; // it is not possible for a message to feed multiple sources
			}
		}

		logger.log(Level.SEVERE, "no source for <{0}> found, dropping data message", dmsg.getSinkID());
	}

	/**
	wrap a Signal and send it to another agent
	 */
	public void sendRemoteSignal(Signal s, AID dst) {
		DataMessage<Signal> dmsg = new DataMessage<Signal>(Signal.class.toString());
		dmsg.store(s, null);
		dmsg.addReceiver(dst);
		send(dmsg);
	}

	public void handleRemoteSignal(DataMessage<? extends Signal> dmsg) { // or better limit to java.rmi.RemoteException?
		Signal s = dmsg.getData();

		if (s instanceof QueueLimitExceededSignal) {
			// tell all our senders which send to the agent who issued the exception to pause
			pauseSendersForDst(dmsg.getSender());
		} else if (s instanceof QueueWithinLimitSignal) {
			// tell all our senders which send to the agent who issued the exception to continute
			resumeSendersForDst(dmsg.getSender());
		} else {
			throw new muscle.exception.MUSCLERuntimeException("unknown signal <" + s.getClass() + ">");
		}
	}

	@Override
	public void notifySinkWillYield(ObservationMessage dmsg) {
		assert dmsg.getByteCount() != null : "[" + getLocalName() + "] DataMessage#getByteCount must not be <null> here";

		synchronized (toldToPauseList) {
			bufferSizeCount -= dmsg.getByteCount();
		}
		assert bufferSizeCount >= 0 : "[" + getLocalName() + "] bufferSizeCount " + bufferSizeCount;

		// se if we have to tell any source kernels to resume sending data to us
		synchronized (toldToPauseList) {
			if (!toldToPauseList.isEmpty() && bufferSizeCount < suggestedMaxBufferSize()) {
				for (Iterator<AID> iter = toldToPauseList.iterator(); iter.hasNext();) {

					AID sender = iter.next();
					sendRemoteSignal(new QueueWithinLimitSignal(), sender);
					iter.remove();
				}
			}
		}
	}

	private void pauseSendersForDst(AID dst) {
		logger.log(Level.WARNING, "pausing senders for {0}", dst.getLocalName());
		// pause all senders which send to agent dst
		for (Iterator<ConduitEntranceController<?>> iter = dataSinks.iterator(); iter.hasNext();) {
			ConduitEntranceController<?> head = iter.next();

			if (head.dstAgent().equals(dst)) {
				head.pause();
			}
		}
	}

	private void resumeSendersForDst(AID dst) {
		logger.log(Level.INFO, "resuming senders for {0}", dst.getLocalName());
		// resume all senders which send to agent dst
		for (Iterator<ConduitEntranceController<?>> iter = dataSinks.iterator(); iter.hasNext();) {
			ConduitEntranceController<?> head = iter.next();
			if (head.dstAgent().equals(dst)) {
				head.unpause();
			}
		}
	}
}

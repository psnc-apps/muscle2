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


import java.util.logging.Level;

import java.util.logging.Logger;
import jade.core.Location;
import muscle.core.messaging.jade.IncomingMessageProcessor;
import muscle.core.messaging.jade.ObservationMessage;
import javatool.ClassTool;
<<<<<<< HEAD
import muscle.core.ident.JadeAgentID;
import muscle.core.ident.JadeLocation;
import muscle.core.kernel.InstanceController;
import muscle.core.messaging.SinkObserver;
=======
import muscle.core.ident.Identifier;
import muscle.core.ident.JadeAgentID;
import muscle.core.kernel.InstanceController;
import muscle.core.messaging.Message;
import muscle.core.messaging.RemoteDataSinkTail;
import muscle.core.messaging.SinkObserver;
import muscle.core.messaging.jade.SortingMessageQueue;
import muscle.core.messaging.serialization.ByteDataConverter;
import muscle.utilities.ObservableLinkedBlockingQueue;
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0

/**
JADE agent which filters incoming data messages and passes them to multiple message sinks
@author Jan Hegewald
 */
<<<<<<< HEAD
public abstract class MultiDataAgent extends jade.core.Agent implements SinkObserver<ObservationMessage<?>>, InstanceController {
	protected transient IncomingMessageProcessor messageProcessor;
	private static final transient Logger logger = Logger.getLogger(MultiDataAgent.class.getName());

	@Override
	public JadeAgentID getIdentifier() {
		return new JadeAgentID(this.getLocalName(), this.getAID(), new JadeLocation(here()));
	}
=======
public abstract class MultiDataAgent extends jade.core.Agent implements SinkObserver<DataMessage<?>>, InstanceController {
	private transient IncomingMessageProcessor messageProcessor;
	private static final transient Logger logger = AgentLogger.getLogger(MultiDataAgent.class.getName());
	private volatile long bufferSizeCount = 0;
	private ObservableLinkedBlockingQueue<DataMessage<?>> nonACLQueue = new ObservableLinkedBlockingQueue<DataMessage<?>>();
	private final List<AID> toldToPauseList = Collections.synchronizedList(new LinkedList<AID>());

	private List<RemoteDataSinkTail<DataMessage<?>>> dataSources = new ArrayList<RemoteDataSinkTail<DataMessage<?>>>(); // these are the conduit exits
	private List<RemoteDataSinkHead<DataMessage<?>>> dataSinks = new ArrayList<RemoteDataSinkHead<DataMessage<?>>>(); // these are the conduit entrances
	
	public void addSink(RemoteDataSinkHead<DataMessage<?>> s) {
		dataSinks.add(s);
	}

	public void addSource(RemoteDataSinkTail<DataMessage<?>> s) {
		dataSources.add(s);
	}

	public Identifier getID() {
		return new JadeAgentID(this.getAID());
	}
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0
	
	@Override
	public void takeDown() {
		messageProcessor = null;
	}

	@Override
	protected void setup() {
		initTransients();
	}

	@Override
	protected void afterMove() {
		initTransients();
	}

	protected void initTransients() {
		messageProcessor = new IncomingMessageProcessor(this);
		this.addBehaviour(messageProcessor);
	}

	@Override
	public void notifySinkWillYield(ObservationMessage msg) {}

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
<<<<<<< HEAD
=======

	// todo: data serialization in separate thread
	public <E extends java.io.Serializable> void sendMessage(Message<E> msg) {
		DataMessage<E> dmsg = null;
		if (msg instanceof DataMessage) {
			dmsg = (DataMessage<E>)msg;
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
	public void handleDataMessage(DataMessage dmsg, long byteCount) {

		// put message to its sink
		// there we process all generic data messages
		// see if this message is intended for one of our other sinks
		for (Iterator<RemoteDataSinkTail<DataMessage<?>>> sourceIterator = dataSources.iterator(); sourceIterator.hasNext();) {
			RemoteDataSinkTail<DataMessage<?>> source = sourceIterator.next();

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
		try {
			dmsg.setContentObject(s);
		} catch (java.io.IOException e) {
			throw new muscle.exception.MUSCLERuntimeException(e);
		}
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
	public void notifySinkWillYield(DataMessage dmsg) {
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
		for (Iterator<RemoteDataSinkHead<DataMessage<?>>> iter = dataSinks.iterator(); iter.hasNext();) {
			RemoteDataSinkHead<DataMessage<?>> head = iter.next();

			if (head.dstAgent().equals(dst)) {
				head.pause();
			}
		}
	}

	private void resumeSendersForDst(AID dst) {
		logger.log(Level.INFO, "resuming senders for {0}", dst.getLocalName());
		// resume all senders which send to agent dst
		for (Iterator<RemoteDataSinkHead<DataMessage<?>>> iter = dataSinks.iterator(); iter.hasNext();) {
			RemoteDataSinkHead<?> head = iter.next();
			if (head.dstAgent().equals(dst)) {
				head.resume();
			}
		}
	}
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0
}

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
import jade.core.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import javatool.ClassTool;
import muscle.core.messaging.RemoteDataSinkHead;
import muscle.core.messaging.RemoteDataSinkTail;
import muscle.core.messaging.SinkObserver;
import muscle.core.messaging.jade.DataMessage;
import muscle.core.messaging.jade.IncomingMessageProcessor;
import muscle.core.messaging.jade.SortingMessageQueue;
import muscle.core.messaging.signal.QueueLimitExceededSignal;
import muscle.core.messaging.signal.QueueWithinLimitSignal;
import muscle.core.messaging.signal.Signal;
import muscle.logging.AgentLogger;
import utilities.MiscTool;


/**
JADE agent which filters incomming data messages and passes them to multiple message sinks
@author Jan Hegewald
*/
public abstract class MultiDataAgent extends jade.core.Agent implements SinkObserver<DataMessage<?>> {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private transient IncomingMessageProcessor messageProcessor;
	private transient Logger logger;
	private List<RemoteDataSinkTail<DataMessage<?>>> dataSources = new ArrayList<RemoteDataSinkTail<DataMessage<?>>>(); // these are the conduit exits
   private List<RemoteDataSinkHead<DataMessage<?>>> dataSinks = new ArrayList<RemoteDataSinkHead<DataMessage<?>>>(); // these are the conduit entrances
	private volatile long bufferSizeCount = 0;
	private ConcurrentLinkedQueue<DataMessage> nonACLQueue = new ConcurrentLinkedQueue<DataMessage>();
	private List<AID> toldToPauseList = Collections.synchronizedList(new LinkedList<AID>());

	//
	public void addSink(RemoteDataSinkHead<DataMessage<?>> s) {

		this.dataSinks.add(s);
	}


	//
	public void addSource(RemoteDataSinkTail<DataMessage<?>> s) {

		this.dataSources.add(s);
	}


	//
   @Override
	public void takeDown() {

		this.messageProcessor.pause();
		this.logger.info("waiting for "+this.messageProcessor.getClass()+" to join");
		try {
			this.messageProcessor.join();
		}
		catch(java.lang.InterruptedException e) {
			throw new muscle.exception.MUSCLERuntimeException(e);
		}
		this.messageProcessor = null;
	}


	//
   @Override
	protected void setup() {

		this.initTransients();
	}


	//
	@Override
	protected void afterMove() {

		this.initTransients();
	}


	//
	protected void initTransients() {

		this.logger = AgentLogger.getLogger(this);
		this.messageProcessor = new IncomingMessageProcessor(this, this.nonACLQueue);
		this.messageProcessor.start();
	}


	//
   @Override
	protected jade.core.MessageQueue createMessageQueue() {

		return new SortingMessageQueue(this.nonACLQueue);
	}


	/**
	maximum buffer size in bytes
	*/
	protected long suggestedMaxBufferSize() {

		return 1042*1042 *800; // 800MB
	}


	/**
	returns our logger
	*/
	public Logger getLogger() {

		return this.logger;
	}


	/**
	custom implementation of Agent#doMove to deny moving this agent if inappropriate
	*/
   @Override
	public void doMove(Location destination) {

		if( ClassTool.isNative(this.getClass(), jade.core.Agent.class) ) {
System.out.println(this.getLocalName()+" is native");
			this.logger.warning("not allowed to move to <"+destination+">");
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

		if( ClassTool.isNative(this.getClass(), jade.core.Agent.class) ) {
			this.logger.warning("not allowed to clone to <"+destination+">");
			return;
		} else {
			super.doClone(destination, newName);
		}
	}


	// todo: data serialization in separate thread
	public void sendDataMessage(DataMessage<? extends java.io.Serializable> dmsg) {

		assert !dmsg.hasByteSequenceContent();

		byte[] rawData = null;
 		rawData = MiscTool.serialize(dmsg.getStored());
// 		rawData = MiscTool.gzip(dmsg.getStored());

 		dmsg.setByteSequenceContent(rawData);
 		dmsg.store(null, null);

		// send data to target agent
		this.send(dmsg);
 		dmsg.setByteSequenceContent(null);
	}


	// an incomming DataMessage has arrived at this agent
	public void handleDataMessage(DataMessage dmsg) {

		// deserialize message content and store it in the message object
		long byteCount = 0;
		if(dmsg.hasByteSequenceContent()) {

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
			throw new muscle.exception.MUSCLERuntimeException("["+this.getLocalName()+"] can not handle empty DataMessage");
		}

		// sort the message to its internal receiver
		if( dmsg.getSinkID().equals(Signal.class.toString()) ) {
			this.handleRemoteSignal(dmsg);
		}
		else {
			// put message to its sink
			// there we process all generic data messages
			// see if this message is intended for one of our other sinks
			for (RemoteDataSinkTail<DataMessage<?>> source : this.dataSources) {
				if( source.id().equals(dmsg.getSinkID()) ) {

					// only add to buffer counter for generic data messages
					// administrative messages like SignalMessage or ACLMessage are not added
					synchronized(this.toldToPauseList) {
						this.bufferSizeCount += byteCount;
					}

					if(this.bufferSizeCount >= this.suggestedMaxBufferSize()) {

						if( (this.bufferSizeCount-byteCount) <= 0 ) {
							throw new muscle.exception.MUSCLERuntimeException("configuration error: ["+this.getLocalName()+"] does not accept a data message because its buffer size is too limited");
						}

						// send pause signal to source kernel
						synchronized(this.toldToPauseList) {
							if(!this.toldToPauseList.contains(dmsg.getSender())) {
								QueueLimitExceededSignal s = new QueueLimitExceededSignal();
								this.sendRemoteSignal(s, dmsg.getSender());
								this.toldToPauseList.add(dmsg.getSender());
							}
						}
					}

					source.put(dmsg);
					return; // it is not possible for a message to feed multiple sources
				}
			}

			this.logger.severe("no source for <"+dmsg.getSinkID()+"> found, dropping data message");
		}
	}


//	// a general incomming message has arrived at this agent
//	public void receiveMessage(ACLMessage msg) {
//
//		if(msg instanceof DataMessage) {
//
//			handleDataMessage((DataMessage)msg);
//		}
//		else if(msg instanceof ACLMessage) {
//			// handle ACLMessage
//			getLogger().info("dropping acl message <"+msg.getClass()+"> "+msg);
//		}
//		else {
//			// handle other messages
//			getLogger().info("dropping unknown message <"+msg.getClass()+">");
//		}
//
//	}


	/**
	wrap a Signal and send it to another agent
	*/
	public void sendRemoteSignal(Signal s, AID dst) {

		DataMessage<Signal> dmsg = new DataMessage(Signal.class.toString());
		try {
			dmsg.setContentObject(s);
		} catch(java.io.IOException e) {
			throw new muscle.exception.MUSCLERuntimeException(e);
		}
		dmsg.addReceiver(dst);
		this.send(dmsg);
	}


	//
	public void handleRemoteSignal(DataMessage<? extends Signal> dmsg) { // or better limit to java.rmi.RemoteException?

		Signal s = dmsg.getStored();

		if(s instanceof QueueLimitExceededSignal) {
			// tell all our senders which send to the agent who issued the exception to pause
			this.pauseSendersForDst(dmsg.getSender());
		}
		else if(s instanceof QueueWithinLimitSignal) {
			// tell all our senders which send to the agent who issued the exception to continute
			this.resumeSendersForDst(dmsg.getSender());
		} else {
			throw new muscle.exception.MUSCLERuntimeException("unknown signal <"+s.getClass()+">");
		}
	}


	//
   public void notifySinkWillYield(DataMessage dmsg) {

		assert dmsg.getByteCount() != null : "["+this.getLocalName()+"] DataMessage#getByteCount must not be <null> here";

      synchronized(this.toldToPauseList) {
			this.bufferSizeCount -= dmsg.getByteCount();
		}
		assert this.bufferSizeCount >= 0 : "["+this.getLocalName()+"] bufferSizeCount "+this.bufferSizeCount;

		// se if we have to tell any source kernels to resume sending data to us
		synchronized(this.toldToPauseList) {
			if(!this.toldToPauseList.isEmpty() && this.bufferSizeCount < this.suggestedMaxBufferSize()) {
				for(Iterator<AID> iter = this.toldToPauseList.iterator(); iter.hasNext();) {

					AID sender = iter.next();
					this.sendRemoteSignal(new QueueWithinLimitSignal(), sender);
					iter.remove();
				}
			}
		}
	}


	//
	private void pauseSendersForDst(AID dst) {

		this.logger.warning("pausing senders for "+dst.getLocalName());
		// pause all senders which send to agent dst
		for (RemoteDataSinkHead<DataMessage<?>> head : this.dataSinks) {
			if( head.dstAgent().equals(dst) ) {
				head.pause();
			}
		}
	}


	//
	private void resumeSendersForDst(AID dst) {

		this.logger.info("resuming senders for "+dst.getLocalName());
		// resume all senders which send to agent dst
		for(Iterator<RemoteDataSinkHead<DataMessage<?>>> iter = this.dataSinks.iterator(); iter.hasNext();) {
			RemoteDataSinkHead<?> head = iter.next();
			if( head.dstAgent().equals(dst) ) {
				head.resume();
			}
		}
	}


//	//
//	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
//
//		// do default deserialization
//		in.defaultReadObject();
//
//		// init transient fields
//		logger = AgentLogger.getLogger(this);
//		messageProcessor = new IncomingMessageProcessor(this, nonACLQueue);
//	}
}

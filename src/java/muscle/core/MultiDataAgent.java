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
import jade.core.MessageQueue;
import muscle.core.messaging.jade.IncomingMessageProcessor;
import muscle.core.messaging.jade.ObservationMessage;
import java.util.List;
import java.util.ArrayList;
import javatool.ClassTool;
import muscle.core.ident.JadeAgentID;
import muscle.core.kernel.InstanceController;
import muscle.core.messaging.SinkObserver;
import muscle.core.messaging.jade.DataMessage;
import muscle.core.messaging.jade.SortingMessageQueue;
import muscle.utilities.ObservableLinkedBlockingQueue;

/**
JADE agent which filters incoming data messages and passes them to multiple message sinks
@author Jan Hegewald
 */
public abstract class MultiDataAgent extends jade.core.Agent implements SinkObserver<ObservationMessage<?>>, InstanceController {
	private transient IncomingMessageProcessor messageProcessor;
	private static final transient Logger logger = Logger.getLogger(MultiDataAgent.class.getName());
	private ObservableLinkedBlockingQueue<DataMessage<?>> nonACLQueue = new ObservableLinkedBlockingQueue<DataMessage<?>>();
	
	private List<ConduitExitController<?>> dataSources = new ArrayList<ConduitExitController<?>>(); // these are the conduit exits
	private List<ConduitEntranceController<?>> dataSinks = new ArrayList<ConduitEntranceController<?>>(); // these are the conduit entrances
	
	public void addSink(ConduitEntranceController<?> s) {
		dataSinks.add(s);
	}

	public void addSource(ConduitExitController<?> s) {
		dataSources.add(s);
	}

	@Override
	public JadeAgentID getIdentifier() {
		return new JadeAgentID(this.getAID());
	}
	
	@Override
	public void takeDown() {
		messageProcessor.dispose();
		for (ConduitExitController source : dataSources) {
			source.dispose();
		}
		for (ConduitEntranceController sink : dataSinks) {
			sink.dispose();
		}
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

	@Override
	protected void afterMove() {
		initTransients();
	}

	protected void initTransients() {
		messageProcessor = new IncomingMessageProcessor(nonACLQueue);
		nonACLQueue.setQueueConsumer(messageProcessor);
		messageProcessor.start();
	}

	@Override
	protected MessageQueue createMessageQueue() {
		return new SortingMessageQueue(nonACLQueue);
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
}

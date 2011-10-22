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
package muscle.core.conduit;

import java.util.logging.Level;
import jade.core.AID;
import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.Serializable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import java.util.logging.Logger;
import utilities.MiscTool;
import muscle.Constant;
import muscle.behaviour.MoveBehaviour;
import muscle.core.ConduitEntranceController;
import muscle.core.DataTemplate;
import muscle.exception.MUSCLERuntimeException;
import muscle.core.conduit.filter.FilterTail;
import muscle.core.messaging.jade.ObservationMessage;
import utilities.FastArrayList;

/**
unidirectional pipe
the writable end of a conduit is its "entrance" (AKA drain or sink)
the readable end of a conduit is its "exit" (AKA source)
@author Jan Hegewald
 */
public class BasicConduit extends muscle.core.MultiDataAgent {
	private final static Logger logger = Logger.getLogger(BasicConduit.class.getName());
	protected AID entranceAgent;
	protected String entranceName;
	private DataTemplate entranceDataTemplate;
	protected AID exitAgent;
	protected String exitName;
	private DataTemplate exitDataTemplate;
	private List<String> optionalArgs;
	
	//
	@Override
	public void takeDown() {
		if (getCurQueueSize() > 0) {
			logger.log(Level.CONFIG, "there are <{0}> unprocessed messages", getCurQueueSize());
		}
		logger.info("bye");
	}

	public List<String> getOptionalArgs() {
		return optionalArgs;
	}

	public DataTemplate getEntranceDataTemplate() {
		return entranceDataTemplate;
	}

	public DataTemplate getExitDataTemplate() {
		return exitDataTemplate;
	}

	@Override
	protected void setup() {
		super.setup();
		beforeMoveSetup();
	}

	// read args
	private void beforeMoveSetup() {
		System.out.println(getLocalName() + " beforeMoveSetup");
		// configure conduit from given args
		Object[] rawArgs = getArguments();

		if (rawArgs.length == 0) {
			logger.severe("got no args to configure from -> terminating");
			doDelete();
			return;
		} else if (rawArgs.length > 1) {
			logger.log(Level.WARNING, "skipping {0} unknown args -> terminating", (rawArgs.length - 1));
		}

		if (!(rawArgs[0] instanceof ConduitArgs)) {
			logger.log(Level.SEVERE, "got invalid args to configure from <{0}> -> terminating", rawArgs[0].getClass().getName());
			doDelete();
			return;
		}

		// read args passed to the agent
		ConduitArgs args = (ConduitArgs) rawArgs[0];
		entranceAgent = args.getEntranceAgent();
		entranceName = args.getEntranceName();
		entranceDataTemplate = args.getEntranceDataTemplate();
		exitAgent = args.getExitAgent();
		exitName = args.getExitName();
		exitDataTemplate = args.getExitDataTemplate();
		Location targetLocation = args.getTargetLocation();

		if (targetLocation == null) {
			targetLocation = targetLocation();
		}

		optionalArgs = args.getOptionalArgs();
		if (optionalArgs == null) {
			optionalArgs = new FastArrayList<String>(0);
		}

		// sanity check: only proceed if mandatory args are successfully set
		if (MiscTool.anyNull(entranceAgent, entranceName, entranceDataTemplate, exitAgent, exitName, exitDataTemplate, targetLocation, optionalArgs)) {
			logger.severe("can not configure conduit from given args -> terminating");
			doDelete();
			return;
		}

		// move to target container
		addBehaviour(new MoveBehaviour(targetLocation, this) {

			@Override
			public void callback(Agent ownerAgent) {
				afterMoveSetup();
			}
		});
	}

	private void afterMoveSetup() {
		// connect to agent which hosts the entrance
		attach();

		constructMessagePassingMechanism();

		DetachListener detachListener = new DetachListener(8000); // add listener with low priority
		addBehaviour(detachListener);

		logger.log(Level.INFO, "conduit <{0}> is up -- entrance <{1}:{2}> -> exit <{3}:{4}>", new Object[]{getClass(), entranceAgent.getName(), entranceName, exitAgent.getName(), exitName});
	}

	protected void constructMessagePassingMechanism() {
		// init filter chain
		FilterTail<ObservationMessage> filters = new DataSenderFilterTail(this);

		MessageReceiverBehaviour receiver = new MessageReceiverBehaviour(filters, this);
		addBehaviour(receiver);
	}

	// determine (initial) host container
	private Location targetLocation() {

		final long t0 = System.currentTimeMillis();
		logger.finer("looking for target location ...");

		final Timer watcher = new Timer();
		TimerTask watcherTask = new TimerTask() {

			@Override
			public void run() {
				long t1 = System.currentTimeMillis();
				logger.log(Level.WARNING, "looking for target location already takes <{0}> ms, maybe there is an error with the WhereIsAgentHelper agent?", (t1 - t0));
				watcher.cancel();
			}
		};
		long timeout = 1000;
		watcher.schedule(watcherTask, timeout);

		Location location = muscle.utilities.agent.WhereIsAgentHelper.whereIsAgent(adjacentAgent(), this);
		watcher.cancel();
		long t1 = System.currentTimeMillis();
		logger.log(Level.FINER, "looking for target location took <{0}> ms", (t1 - t0));

		return location;
	}

	/**
	connect so source kernel (e.g. a remote sender)
	 */
	protected void attach() {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol(Constant.Protocol.PORTAL_ATTACH);
		msg.addUserDefinedParameter("entrance", entranceName);
		msg.addUserDefinedParameter("exit", exitName);
		msg.addReceiver(entranceAgent);
		msg.setContent(exitName); // sink id
		msg.setSender(this.getAID());
		send(msg);
	}

	class DetachListener extends TickerBehaviour {

		private MessageTemplate detachTemplate;
		private boolean entranceAvailable = true;

		DetachListener(long timeout) {
			super(BasicConduit.this, timeout);
			detachTemplate = MessageTemplate.MatchProtocol(Constant.Protocol.PORTAL_DETACH);
		}

		@Override
		protected void onTick() {
			logger.fine("listening for detach");
			ACLMessage msg = receive(detachTemplate);
			if (msg != null) {
				Class<?> portalClass = null;
				try {
					portalClass = (Class<?>) msg.getContentObject();
				} catch (jade.lang.acl.UnreadableException e) {
					throw new MUSCLERuntimeException(e);
				}

				if (ConduitEntranceController.class.isAssignableFrom(portalClass)) {
					entranceAvailable = false;
					logger.info("entrance detached");
				} else {
					throw new MUSCLERuntimeException("can not detach unknown portal: <" + portalClass.getName() + ">");
				}

				if (entranceAvailable == false /*&& exitAgent == null*/) {
					// process remaining messages and terminate
					myAgent.addBehaviour(new TearDownBehaviour());
					removeBehaviour(this);
				}
			} else {
				block();
			}
		}
	}

	/**
	this behaviour will be activated as soon as our entrance has been detached
	will watch the message queue and tear down the conduit after all remaining messages have been processed
	 */
	private class TearDownBehaviour extends SimpleBehaviour {

		public TearDownBehaviour() {
			super(BasicConduit.this);
		}

		@Override
		public void action() {
			if (myAgent.getCurQueueSize() == 0) {
				myAgent.doDelete();
			}
		}

		@Override
		public boolean done() {
			return false;
		}
	}

	//
//	public enum ResourceStrategy {
//		
//		STATIC_LOW_CPU // try to reduce CPU load but do not move between containers after initial setup
//		, STATIC_LOW_NETWORK // try to reduce network traffic but do not move between containers after initial setup
//		, DYNAMIC_LOW_CPU // try to reduce CPU load and may move to another container during runtime if this helps
//		, DYNAMIC_LOW_NETWORK // try to reduce network traffic and may move to another container during runtime if this helps
//	}
	// try to reduce network traffic
	public AID adjacentAgent() {
//		if(resourceStrategy == ResourceStrategy.STATIC_LOW_NETWORK) {
		// determine prefered host container (usually where the entrance or exit lives)
		if (exitDataTemplate.getQuantity() < 0 || entranceDataTemplate.getQuantity() < 0) {
			return entranceAgent; // default to entranceAgent if quantity is not specified
		} else if (exitDataTemplate.getQuantity() < entranceDataTemplate.getQuantity()) {
			// move to the container which hosts the exit
			return exitAgent;
		} else {
			// move to the container which hosts the entrance
			return entranceAgent;
		}
	}

	public <E> void sendData(E data) {
		if (data instanceof ACLMessage) {
			this.send((ACLMessage)data);
		}
		else {
			throw new IllegalArgumentException("Basic Jade Conduit can only send ACLMessage directly.");
		}
	}
}

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

import jade.core.AID;
import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import muscle.Constant;
import muscle.behaviour.MoveBehaviour;
import muscle.core.ConduitEntrance;
import muscle.core.DataTemplate;
import muscle.core.conduit.filter.Filter;
import muscle.core.conduit.filter.FilterChain;
import muscle.core.messaging.BasicRemoteDataSinkHead;
import muscle.core.messaging.BufferingRemoteDataSinkTail;
import muscle.core.messaging.RemoteDataSinkHead;
import muscle.core.messaging.RemoteDataSinkTail;
import muscle.core.messaging.SinkObserver;
import muscle.core.messaging.jade.DataMessage;
import muscle.exception.MUSCLERuntimeException;
import utilities.MiscTool;


/**
unidirectional pipe
the writable end of a conduit is its "entrance" (AKA drain or sink)
the readable end of a conduit is its "exit" (AKA source)
@author Jan Hegewald
*/
public class BasicConduit extends muscle.core.MultiDataAgent {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	AID entranceAgent;
	String entranceName;
	private DataTemplate entranceDataTemplate;
	AID exitAgent;
	String exitName;
	private DataTemplate exitDataTemplate;
	private ArrayList<Object> optionalArgs;
	private MessageTemplate receiveTemplate;
//	private MessageReceiverBehaviour receiver; // feeds the filter chain

	private ResourceStrategy resourceStrategy;

	private OutputStreamWriter traceReceiveWriter;
	private OutputStreamWriter traceSendWriter;


	//
   @Override
	public void takeDown() {

		if(this.traceReceiveWriter != null) {
			try {
				this.traceReceiveWriter.close();
			} catch (IOException e) {
				throw new MUSCLERuntimeException(e);
			}
		}
		if(this.traceSendWriter != null) {
			try {
				this.traceSendWriter.close();
			} catch (IOException e) {
				throw new MUSCLERuntimeException(e);
			}
		}

		if(this.getCurQueueSize() > 0) {
			this.getLogger().config("there are <"+this.getCurQueueSize()+"> unprocessed messages");
		}
		this.getLogger().info("bye");
	}


	//
	public ArrayList<Object> getOptionalArgs() {

		return this.optionalArgs;
	}


	//
	public DataTemplate getEntranceDataTemplate() {

		return this.entranceDataTemplate;
	}


	//
	public DataTemplate getExitDataTemplate() {

		return this.exitDataTemplate;
	}


//	//
//   @Override
//	public void handleRemoteSignal(DataMessage<? extends Signal> dmsg) { // or better limit to java.rmi.RemoteException?
//
//		// here we forward any signal to our entrance agent
//		dmsg.clearAllReceiver();
//      dmsg.setSender(getAID());
//      send(dmsg);
//	}


	//
   @Override
	protected void setup() {
		super.setup();
		this.beforeMoveSetup();
	}


	// read args
	private void beforeMoveSetup() {
System.out.println(this.getLocalName()+" beforeMoveSetup");
		// configure conduit from given args
		Object[] rawArgs = this.getArguments();

		if(rawArgs.length == 0) {
			this.getLogger().severe("got no args to configure from -> terminating");
			this.doDelete();
			return;
		}
		else if(rawArgs.length > 1) {
			this.getLogger().warning("skipping "+(rawArgs.length-1)+" unknown args -> terminating");
		}

		if(! (rawArgs[0] instanceof ConduitArgs)) {
			this.getLogger().severe("got invalid args to configure from <"+rawArgs[0].getClass().getName()+"> -> terminating");
			this.doDelete();
			return;
		}

		// read args passed to the agent
		ConduitArgs args = (ConduitArgs)rawArgs[0];
		this.entranceAgent = args.getEntranceAgent();
		this.entranceName = args.getEntranceName();
		this.entranceDataTemplate = args.getEntranceDataTemplate();
		this.exitAgent = args.getExitAgent();
		this.exitName = args.getExitName();
		this.exitDataTemplate = args.getExitDataTemplate();
		Location targetLocation = args.getTargetLocation();

		Class<? extends ResourceStrategy> strategyClass = args.getStrategyClass();
		// if strategyClass is a member class, we can not call newInstance() on the class object
		// (this is only possible for static member classes)
		// instead, we fetch the defaulf member class constructor which takes a reference to the enclosing class as argument
		//	note that strategyClass.getConstructor(this.getClass()); does not work for classes which derive from the original enclosing class
		// so only strategyClass.getConstructor(muscle.core.conduit.Conduit); would work here
		// below is a workaround to this: we fetch the default constructor and test if our current instance (this) is an instance of the required type
		if(strategyClass.isMemberClass()) {

			// get the default constructor for a member class of type ResourceStrategy
			Constructor<? extends ResourceStrategy> constructor = null;
			constructor = (Constructor<? extends ResourceStrategy>)strategyClass.getConstructors()[0];
			if(constructor == null || constructor.getParameterTypes().length != 1) {
				if( constructor.getParameterTypes()[0].getClass().isInstance(this) ) {
					throw new MUSCLERuntimeException("can not create ResourceStrategy instance for class <"+strategyClass.getName()+">");
				}
			}

			// instantiate member class of type ResourceStrategy
			try {
				this.resourceStrategy = constructor.newInstance(this);
			} catch (InstantiationException e) {
				throw new MUSCLERuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new MUSCLERuntimeException(e);
			} catch (java.lang.reflect.InvocationTargetException e) {
				throw new MUSCLERuntimeException(e);
			}
		}
		else {
			// instantiate non-member class of type ResourceStrategy
			try {
				this.resourceStrategy = strategyClass.newInstance();
			} catch (InstantiationException e) {
				throw new MUSCLERuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new MUSCLERuntimeException(e);
			}
		}

		if(targetLocation == null) {
			targetLocation = this.targetLocation();
		}

		this.optionalArgs = args.getOptionalArgs();
		if(this.optionalArgs == null) {
			this.optionalArgs = new ArrayList<Object>();
		}

		// sanity check: only proceed if mandatory args are successfully set
		int index = MiscTool.indexOfNull(this.entranceAgent, this.entranceName, this.entranceDataTemplate, this.exitAgent, this.exitName, this.exitDataTemplate, this.resourceStrategy, targetLocation, this.optionalArgs);
		if( index > -1 ) {
			String raws = new String();
			for (Object o : rawArgs) {
				raws += o.toString() + ",";
			}
			this.getLogger().severe("can not configure conduit from given args, arg " + index + " is null -> terminating\nArguments were raws" + raws);
			this.doDelete();
			return;
		}

		// prepare template for incomming data messages
		this.receiveTemplate = MessageTemplate.MatchProtocol(this.entranceName+":"+Constant.Protocol.DATA_TRANSFER);

		// move to target container
		this.addBehaviour(new MoveBehaviour(targetLocation, this) {
         /**
			 *
			 */
			private static final long serialVersionUID = 1L;

		@Override
			public void callback(Agent ownerAgent) {
				BasicConduit.this.afterMoveSetup();
			}
		});
	}


	//
	private void afterMoveSetup() {

		// connect to agent which hosts the entrance
		this.attach();

		this.constructMessagePassingMechanism();

		DetachListener detachListener = new DetachListener(8000); // add listener with low priority
		this.addBehaviour(detachListener);

		this.getLogger().info("conduit <"+this.getClass()+"> is up -- entrance <" + this.entranceAgent.getName()+":"+this.entranceName + "> -> exit <" + this.exitAgent.getName()+":"+this.exitName+">");
	}


	//
	protected void constructMessagePassingMechanism() {

		// we do not use any manipulating filters here,
		// so the out template must be identical with the in template
		try {
			if( !DataTemplate.match(this.getEntranceDataTemplate(), this.getExitDataTemplate()) ) {
				throw new muscle.exception.DataTemplateMismatchException(this.getEntranceDataTemplate().toString()+" vs. "+this.getExitDataTemplate().toString());
			}
		}
		catch (muscle.exception.DataTemplateMismatchException e) {
			throw new MUSCLERuntimeException(e);
		}

		// init filter chain
		FilterChain fc = new FilterChain();
      Filter filters = fc.buildFilterChain(new DataSenderFilter());

		MessageReceiverBehaviour receiver = new MessageReceiverBehaviour(filters);
		this.addBehaviour(receiver);
	}


	// determine (initial) host container
	private Location targetLocation() {

		final long t0 = System.currentTimeMillis();
		this.getLogger().finer("looking for target location ...");

		final Timer watcher = new Timer();
		TimerTask watcherTask = new TimerTask() {
         @Override
			public void run() {
				long t1 = System.currentTimeMillis();
				BasicConduit.this.getLogger().warning("looking for target location already takes <"+(t1-t0)+"> ms, maybe there is an error with the WhereIsAgentHelper agent?");
				watcher.cancel();
			}
		};
		long timeout = 1000;
		watcher.schedule(watcherTask, timeout);

		Location location = muscle.utilities.agent.WhereIsAgentHelper.whereIsAgent(this.resourceStrategy.adjacentAgent(), this);
		watcher.cancel();
		long t1 = System.currentTimeMillis();
		this.getLogger().finer("looking for target location took <"+(t1-t0)+"> ms");

		return location;
	}


	//
//	private AID targetAgent() {
//
//		assert resourceStrategy != null;
//
//		if(resourceStrategy == ResourceStrategy.STATIC_LOW_NETWORK) {
//			// determine prefered host container (usually where the entrance or exit lives)
//			if(exitDataTemplate.getQuantity() < entranceDataTemplate.getQuantity()) {
//				// move to the container which hosts the exit
//				return exitAgent;
//			}
//			else {
//				// move to the container which hosts the entrance
//				return entranceAgent;
//			}
//		}
//		else {
//			throw new MUSCLERuntimeException("can not switch to <"+resourceStrategy+"> -- not implemented?");
//		}
//	}


	/**
	connect so source kernel (e.g. a remote sender)
	*/
	protected void attach() {

		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol(Constant.Protocol.PORTAL_ATTACH);
		msg.addUserDefinedParameter("entrance", this.entranceName);
		msg.addUserDefinedParameter("exit", this.exitName);
		msg.addReceiver(this.entranceAgent);
		msg.setContent(this.exitName); // sink id
		msg.setSender(this.getAID());
		this.send(msg);
	}


	/**
	this behaviour is the connection to the (remote) ConduitEntrance and receives data messages from there
	*/
	class MessageReceiverBehaviour<T> extends CyclicBehaviour implements RemoteDataSinkTail<DataMessage<?>> {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private Filter headFilter;
		private RemoteDataSinkTail<DataMessage<?>> receiver;

		public MessageReceiverBehaviour(Filter newHeadFilter) {
			super(BasicConduit.this);

			this.headFilter = newHeadFilter;
			this.receiver = new BufferingRemoteDataSinkTail<DataMessage<?>>(BasicConduit.this.exitName);
         this.receiver.addObserver(BasicConduit.this);
			BasicConduit.this.addSource(this.receiver);
		}

		// receive from entrance
      @Override
		public void action() {

         DataMessage<?> dmsg = null;
         for(int i = 0; i < 1000 && ((dmsg = this.poll()) == null); i++) {
            try {
               Thread.sleep(1);
            } catch (InterruptedException e) {
              // throw new RuntimeException(e);
            }
         }



//			ACLMessage msg = receiveFromEntrance();

			if( dmsg == null )
				{}//block();
			else {
				// feed the first headFilter
				this.headFilter.put(dmsg);
			}
		}

      public void put(DataMessage<?> d) {
			this.receiver.put(d);
      }

      public DataMessage<?> take() {
			// we use a custom poll instead of take.
			// super.take would call notifySinkWillYield AND also poll,
			// leading in notifySinkWillYield being called twice
			throw new java.lang.UnsupportedOperationException("can not take from "+this.getClass());
      }

      public DataMessage<?> poll() {

//		synchronized(sinkObserver) {
			DataMessage<?> val = this.receiver.poll();

			if(val != null) {
				BasicConduit.this.notifySinkWillYield(val);
			}

			return val;
//		}
	}

      public String id() {
			return this.receiver.id();
      }

      public void addObserver(SinkObserver<DataMessage<?>> o) {
			this.receiver.addObserver(o);
      }



		// receive data from entrance
//		private ACLMessage receiveFromEntrance() {
//
//			getLogger().finest("waiting for a message ..." + " (current msg queue:" + getCurQueueSize() + ")");
//
//			//
//			ACLMessage msg = blockingReceive(receiveTemplate, 1000);
//			// if the conduit should also focus on other behaviours, use a non-blocking receive here:
//			//ACLMessage msg = receive(receiveTemplate);
//
//			if(msg != null)
//			{
//				getLogger().finest("got something (current msg queue:" + getCurQueueSize() + ")");
//
//				AID sender = msg.getSender();
//				if( !sender.equals(entranceAgent) ) {
//					getLogger().severe("wrong sender -- expected <"+entranceAgent.getName()+"> got <"+sender.getName()+">");
//					throw new MUSCLERuntimeException("pipeline error in conduit <"+getName()+">");
//				}
//			}
//
//			return msg;
//		}
	}


	/**
	this behaviour is the connection to the (remote) ConduitExit and sends data messages to it
	*/
	class DataSenderFilter implements Filter<DataMessage> {

		private RemoteDataSinkHead<DataMessage<?>> sink;
		private boolean shouldPause = false;

		//
		public DataSenderFilter() {

			this.sink = new BasicRemoteDataSinkHead<DataMessage<?>>(BasicConduit.this.exitName, BasicConduit.this.exitAgent) {

            @Override
            public void put(DataMessage dmsg) {

					while( DataSenderFilter.this.shouldPause ) {
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
					}

					BasicConduit.this.sendDataMessage(dmsg);
				}

            public void pause() {
					DataSenderFilter.this.shouldPause = true;
				}

            public void resume() {
					DataSenderFilter.this.shouldPause = false;
				}
         };

			BasicConduit.this.addSink(this.sink);
		}

		// handle result data, i.e. send data to exit
		public void put(DataMessage dmsg) {

			dmsg.clearAllReceiver();
			dmsg.addReceiver(BasicConduit.this.exitAgent);
			dmsg.setSender(BasicConduit.this.getAID());

			this.sink.put(dmsg);
		}

	public DataTemplate getInTemplate() {
		// TODO Auto-generated method stub
		return null;
	}

	}


	//
	class DetachListener extends TickerBehaviour {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private MessageTemplate detachTemplate;
		private boolean entranceAvailable = true;

		//
		DetachListener(long timeout) {
			super(BasicConduit.this, timeout);
			this.detachTemplate = MessageTemplate.MatchProtocol(Constant.Protocol.PORTAL_DETACH);
		}


		//
      @Override
		protected void onTick() {
			BasicConduit.this.getLogger().fine("listening for detach");
			ACLMessage msg = BasicConduit.this.receive(this.detachTemplate);
			if( msg != null ) {
				Class<?> portalClass = null;
				try {
					portalClass = (Class<?>)msg.getContentObject();
				} catch (jade.lang.acl.UnreadableException e) {
					throw new MUSCLERuntimeException(e);
				}

				if( ConduitEntrance.class.isAssignableFrom(portalClass) ) {
					this.entranceAvailable = false;
					BasicConduit.this.getLogger().info("entrance detached");
				}
				else {
					throw new MUSCLERuntimeException("can not detach unknown portal: <"+portalClass.getName()+">");
				}

				if( this.entranceAvailable == false /*&& exitAgent == null*/ ) {
					// process remaining messages and terminate
					this.myAgent.addBehaviour(new TearDownBehaviour());
					BasicConduit.this.removeBehaviour(this);
				}
			}
			else {
				this.block();
			}
		}
	}


	/**
	this behaviour will be activated as soon as our entrance has been detached
	will watch the message queue and tear down the conduit after all remaining messages have been processed
	*/
	private class TearDownBehaviour extends SimpleBehaviour {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		public TearDownBehaviour() {
			super(BasicConduit.this);
		}


      @Override
		public void action() {

			if( this.myAgent.getCurQueueSize() == 0 ) {
//				if(receiver != null) {
//					removeBehaviour(receiver);
//					receiver = null;
//				}
				this.myAgent.doDelete();
//				myAgent.removeBehaviour(this);
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
	public class LowBandwidthStrategy implements ResourceStrategy, Serializable {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		public AID adjacentAgent() {

//			if(resourceStrategy == ResourceStrategy.STATIC_LOW_NETWORK) {
				// determine prefered host container (usually where the entrance or exit lives)
				if( BasicConduit.this.exitDataTemplate.getQuantity() < 0 || BasicConduit.this.entranceDataTemplate.getQuantity() < 0 ) {
					return BasicConduit.this.entranceAgent; // default to entranceAgent if quantity is not specified
				} else if(BasicConduit.this.exitDataTemplate.getQuantity() < BasicConduit.this.entranceDataTemplate.getQuantity()) {
					// move to the container which hosts the exit
					return BasicConduit.this.exitAgent;
				}
				else {
					// move to the container which hosts the entrance
					return BasicConduit.this.entranceAgent;
				}
//			}
//			else {
//				throw new MUSCLERuntimeException("can not switch to <"+resourceStrategy+"> -- not implemented?");
//			}
		}

	}


	// do not move or try to be clever
	public class DullStrategy implements ResourceStrategy, Serializable {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		public AID adjacentAgent() {

			return BasicConduit.this.getAID();
		}

	}
}

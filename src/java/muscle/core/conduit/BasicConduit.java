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

import muscle.core.conduit.filter.FilterChain;
import jade.core.AID;
import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

import utilities.MiscTool;
import muscle.Constant;
import muscle.behaviour.MoveBehaviour;
import muscle.core.ConduitEntrance;
import muscle.core.CxADescription;
import muscle.core.DataTemplate;
import muscle.utilities.RemoteOutputStream;
import muscle.exception.MUSCLERuntimeException;
import muscle.core.conduit.filter.Filter;
import muscle.core.messaging.BasicRemoteDataSinkHead;
import muscle.core.messaging.BufferingRemoteDataSinkTail;
import muscle.core.messaging.RemoteDataSinkHead;
import muscle.core.messaging.RemoteDataSinkTail;
import muscle.core.messaging.SinkObserver;
import muscle.core.messaging.jade.DataMessage;


/**
unidirectional pipe
the writable end of a conduit is its "entrance" (AKA drain or sink)
the readable end of a conduit is its "exit" (AKA source)
@author Jan Hegewald
*/
public class BasicConduit extends muscle.core.MultiDataAgent {
	
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
	public void takeDown() {		
		
		if(traceReceiveWriter != null) {
			try {
				traceReceiveWriter.close();
			} catch (IOException e) {
				throw new MUSCLERuntimeException(e);
			}
		}
		if(traceSendWriter != null) {
			try {
				traceSendWriter.close();
			} catch (IOException e) {
				throw new MUSCLERuntimeException(e);
			}
		}
		
		if(getCurQueueSize() > 0)
			getLogger().config("there are <"+getCurQueueSize()+"> unprocessed messages");
		getLogger().info("bye");
	}


	//
	public ArrayList<Object> getOptionalArgs() {
	
		return optionalArgs;
	}
	
	
	//
	public DataTemplate getEntranceDataTemplate() {
	
		return entranceDataTemplate;
	}


	//
	public DataTemplate getExitDataTemplate() {
	
		return exitDataTemplate;
	}


//	//
//	public void handleRemoteSignal(DataMessage<? extends Signal> dmsg) { // or better limit to java.rmi.RemoteException?
//		
//		// here we forward any signal to our entrance agent
//		dmsg.clearAllReceiver();
//      dmsg.setSender(getAID());
//      send(dmsg);
//	}
	
	
	//
	protected void setup() {
		super.setup();
		beforeMoveSetup();
	}
	
	
	// read args
	private void beforeMoveSetup() {
System.out.println(getLocalName()+" beforeMoveSetup");
		// configure conduit from given args
		Object[] rawArgs = getArguments();
		
		if(rawArgs.length == 0) {
			getLogger().severe("got no args to configure from -> terminating");
			doDelete();
			return;
		}
		else if(rawArgs.length > 1) {
			getLogger().warning("skipping "+(rawArgs.length-1)+" unknown args -> terminating");
		}
		
		if(! (rawArgs[0] instanceof ConduitArgs)) {
			getLogger().severe("got invalid args to configure from <"+rawArgs[0].getClass().getName()+"> -> terminating");
			doDelete();		
			return;		
		}

		// read args passed to the agent
		ConduitArgs args = (ConduitArgs)rawArgs[0];
		entranceAgent = args.getEntranceAgent();
		entranceName = args.getEntranceName();
		entranceDataTemplate = args.getEntranceDataTemplate();
		exitAgent = args.getExitAgent();
		exitName = args.getExitName();
		exitDataTemplate = args.getExitDataTemplate();
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
				resourceStrategy = constructor.newInstance(this);
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
				resourceStrategy = strategyClass.newInstance();
			} catch (InstantiationException e) {
				throw new MUSCLERuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new MUSCLERuntimeException(e);
			}		
		}
				
		if(targetLocation == null)
			targetLocation = targetLocation();

		optionalArgs = args.getOptionalArgs();
		if(optionalArgs == null)
			optionalArgs = new ArrayList<Object>();
		
		// sanity check: only proceed if mandatory args are successfully set
		if( MiscTool.anyNull(entranceAgent, entranceName, entranceDataTemplate, exitAgent, exitName, exitDataTemplate, resourceStrategy, targetLocation, optionalArgs) ) {
			getLogger().severe("can not configure conduit from given args -> terminating");
			doDelete();
			return;
		}
					
		// prepare template for incomming data messages
		receiveTemplate = MessageTemplate.MatchProtocol(entranceName+":"+Constant.Protocol.DATA_TRANSFER);
		
		// move to target container
		addBehaviour(new MoveBehaviour(targetLocation, this) {
			public void callback(Agent ownerAgent) {
				afterMoveSetup();
			}
		});
	}


	//
	private void afterMoveSetup() {

		// connect to agent which hosts the entrance
		attach();
		
		constructMessagePassingMechanism();
				
		DetachListener detachListener = new DetachListener(8000); // add listener with low priority
		addBehaviour(detachListener);	

		getLogger().info("conduit <"+getClass()+"> is up -- entrance <" + entranceAgent.getName()+":"+entranceName + "> -> exit <" + exitAgent.getName()+":"+exitName+">");
	}


	//
	protected void constructMessagePassingMechanism() {

		// we do not use any manipulating filters here,
		// so the out template must be identical with the in template
		try {
			if( !DataTemplate.match(getEntranceDataTemplate(), getExitDataTemplate()) ) {
				throw new muscle.exception.DataTemplateMismatchException(getEntranceDataTemplate().toString()+" vs. "+getExitDataTemplate().toString());
			}
		}
		catch (muscle.exception.DataTemplateMismatchException e) {
			throw new MUSCLERuntimeException(e);
		}

		// init filter chain
		FilterChain fc = new FilterChain();
      Filter filters = fc.buildFilterChain(new DataSenderFilter());
		
		MessageReceiverBehaviour receiver = new MessageReceiverBehaviour(filters);
		addBehaviour(receiver);
	}
	

	// determine (initial) host container
	private Location targetLocation() {

		final long t0 = System.currentTimeMillis();
		getLogger().finer("looking for target location ...");
		
		final Timer watcher = new Timer();
		TimerTask watcherTask = new TimerTask() {
			public void run() {
				long t1 = System.currentTimeMillis();
				getLogger().warning("looking for target location already takes <"+(t1-t0)+"> ms, maybe there is an error with the WhereIsAgentHelper agent?");
				watcher.cancel();
			}
		};
		long timeout = 1000;
		watcher.schedule(watcherTask, timeout);
		
		Location location = muscle.utilities.agent.WhereIsAgentHelper.whereIsAgent(resourceStrategy.adjacentAgent(), this);
		watcher.cancel();
		long t1 = System.currentTimeMillis();
		getLogger().finer("looking for target location took <"+(t1-t0)+"> ms");

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


	//
	private RemoteOutputStream newTraceReceiveStream() {
		
		return new RemoteOutputStream(this, CxADescription.ONLY.getSharedLocation(), getName()+"_Entrance_"+entranceName+"--f"+entranceDataTemplate.getScale().getDt()+".txt", 1024);
	}


	//
	private RemoteOutputStream newTraceSendStream() {
		
		return new RemoteOutputStream(this, CxADescription.ONLY.getSharedLocation(), getName()+"_Exit_"+exitName+"--f"+exitDataTemplate.getScale().getDt()+".txt", 1024);
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
	
	
	/**
	this behaviour is the connection to the (remote) ConduitEntrance and receives data messages from there
	*/
	class MessageReceiverBehaviour<T> extends CyclicBehaviour implements RemoteDataSinkTail<DataMessage<?>> {
	
		private Filter headFilter;
		private RemoteDataSinkTail<DataMessage<?>> receiver;
		
		public MessageReceiverBehaviour(Filter newHeadFilter) {
			super(BasicConduit.this);
			
			headFilter = newHeadFilter;
			receiver = new BufferingRemoteDataSinkTail<DataMessage<?>>(exitName);
         receiver.addObserver(BasicConduit.this);
			addSource(receiver);
		}

		// receive from entrance
		public void action() {

         DataMessage<?> dmsg = null;
         for(int i = 0; i < 1000 && ((dmsg = poll()) == null); i++) {
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
				headFilter.put(dmsg);
			}
		}

      public void put(DataMessage<?> d) {
			receiver.put(d);
      }

      public DataMessage<?> take() {
			// we use a custom poll instead of take.
			// super.take would call notifySinkWillYield AND also poll,
			// leading in notifySinkWillYield being called twice 
			throw new java.lang.UnsupportedOperationException("can not take from "+getClass());
      }

      public DataMessage<?> poll() {
		
//		synchronized(sinkObserver) {
			DataMessage<?> val = receiver.poll();
			
			if(val != null)
				BasicConduit.this.notifySinkWillYield((DataMessage<?>)val);
		
			return val;
//		}
	}

      public String id() {
			return receiver.id();
      }

      public void addObserver(SinkObserver<DataMessage<?>> o) {
			receiver.addObserver(o);
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

			sink = new BasicRemoteDataSinkHead<DataMessage<?>>(exitName, exitAgent) {

            public void put(DataMessage dmsg) {

					while( shouldPause ) {
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}			
					}

					sendDataMessage(dmsg);
				}

            public void pause() {					
					shouldPause = true;
				}

            public void resume() {
					shouldPause = false;
				}
         };
			
			addSink(sink);
		}
		
		// handle result data, i.e. send data to exit
		public void put(DataMessage dmsg) {

			dmsg.clearAllReceiver();
			dmsg.addReceiver(exitAgent);
			dmsg.setSender(getAID());
			
			sink.put(dmsg);
		}

	}


	//
	class DetachListener extends TickerBehaviour {

		private MessageTemplate detachTemplate;
		private boolean entranceAvailable = true;
		
		//
		DetachListener(long timeout) {
			super(BasicConduit.this, timeout);
			detachTemplate = MessageTemplate.MatchProtocol(Constant.Protocol.PORTAL_DETACH);
		}
		
		
		//
		protected void onTick() {		
			getLogger().fine("listening for detach");
			ACLMessage msg = receive(detachTemplate);
			if( msg != null ) {
				Class<?> portalClass = null;
				try {
					portalClass = (Class<?>)msg.getContentObject();
				} catch (jade.lang.acl.UnreadableException e) {
					throw new MUSCLERuntimeException(e);
				}
						
				if( ConduitEntrance.class.isAssignableFrom(portalClass) ) {
					entranceAvailable = false;
					getLogger().info("entrance detached");
				}
				else {
					throw new MUSCLERuntimeException("can not detach unknown portal: <"+portalClass.getName()+">");
				}
								
				if( entranceAvailable == false /*&& exitAgent == null*/ ) {				
					// process remaining messages and terminate
					myAgent.addBehaviour(new TearDownBehaviour());
					removeBehaviour(this);
				}
			}
			else {
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
		

		public void action() {
		
			if( myAgent.getCurQueueSize() == 0 ) {
//				if(receiver != null) {
//					removeBehaviour(receiver);
//					receiver = null;
//				}
				myAgent.doDelete();	
//				myAgent.removeBehaviour(this);
			}
		}
		
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

		public AID adjacentAgent() {
			
//			if(resourceStrategy == ResourceStrategy.STATIC_LOW_NETWORK) {
				// determine prefered host container (usually where the entrance or exit lives)
				if( exitDataTemplate.getQuantity() < 0 || entranceDataTemplate.getQuantity() < 0 )
					return entranceAgent; // default to entranceAgent if quantity is not specified
				else if(exitDataTemplate.getQuantity() < entranceDataTemplate.getQuantity()) {
					// move to the container which hosts the exit
					return exitAgent;
				}
				else {		
					// move to the container which hosts the entrance
					return entranceAgent;
				}
//			}
//			else {
//				throw new MUSCLERuntimeException("can not switch to <"+resourceStrategy+"> -- not implemented?");
//			}
		}

	}


	// do not move or try to be clever
	public class DullStrategy implements ResourceStrategy, Serializable {

		public AID adjacentAgent() {
			
			return getAID();
		}

	}
}

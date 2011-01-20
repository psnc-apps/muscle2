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
import jade.core.Agent;
import jade.core.Location;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;
import jade.wrapper.AgentController;
import jadetool.DFServiceTool;
import jadetool.DFServiceTool.RegisterSingletonAgentException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import muscle.Constant;
import muscle.core.ConnectionScheme.Pipeline;
import muscle.core.conduit.BasicConduit;
import muscle.core.conduit.ConduitArgs;
import muscle.exception.MUSCLERuntimeException;
import muscle.exception.SpawnAgentException;
import muscle.logging.AgentLogger;

import com.thoughtworks.xstream.XStream;


/**
listens for kernel announcements and spawns conduits according to the connection scheme
@author Jan Hegewald
*/
public class Plumber extends Agent {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public static final int MANDATORY_CONDUIT_ARG_COUNT = 7;

	private LinkedList<EntranceDescription> postponedEntrances = new LinkedList<EntranceDescription>();
	private LinkedList<ExitDescription> postponedExits = new LinkedList<ExitDescription>();

	private static int conduitCounter = 0;
	private transient AgentLogger logger;

	private transient XStream xstream = new XStream();

	private ConnectionScheme cs;

	//
	@Override
	protected void setup() {

		this.logger = AgentLogger.getLogger(this);

		if( DFServiceTool.hasSingletonAgent(this, Plumber.class.getName()) ) {
			this.logger.severe("can not start because there are already other Plumber(s) available for this platform");
			this.doDelete();
			return; // otherwise the setup would continue
		}

		// register self as singleton
		try {
			DFServiceTool.registerSingletonAgent(this, Plumber.class.getName());
		}
		catch(RegisterSingletonAgentException e) {
			this.logger.severe("can not start because there are already other Plumber(s) available for this platform");
			// bail out here
			throw new MUSCLERuntimeException(e);
		}

		this.cs = this.initConnectionScheme();
		this.logger.info(this.cs.toString());

		this.logger.info("Plumber of kind <"+this.getClass()+"> is up");

		this.addEntranceListener();
		this.addExitListener();


// experimental support for remote gui
//if( CxADescription.ONLY.getBooleanProperty(CxADescription.Key.GUI) ) {
//	addBehaviour(new PlumberVisualisationServerBehaviour(this, MessageTemplate.MatchProtocol("visualisation data"), this));
////	initRemoteComponentHead();
//	addBehaviour(new VisualisationServerBehaviour(this, muscle.gui.remote.SharedConnectionSchemePutter.class, getConnectionSchemeRoot()));
//}
	}


	//
	public ConnectionScheme getConnectionScheme() {

		return this.cs;
	}


	//
//	private void initRemoteComponentHead()	{
//
//		AID guiID = null;
//		logger.info("searching for a remote GUI gateway ...");
//		try {
//			guiID = DFServiceTool.agentForService(this, true, RemoteComponentTailAgent.class.getName(), null);
//		} catch (FIPAException e) {
//			e.printStackTrace();
//			logger.severe("search with DF is not succeeded because of " + e.getMessage());
//			doDelete();
//		}
//		logger.info("found a remote GUI gateway at <"+guiID.getName()+">");
//
//
//		Agent2RemoteComponentHead remoteConnectionSchemeJUNGPanel = new Agent2RemoteComponentHead(coast.gui.remote.SharedConnectionSchemePutter.class, guiID, this);
//		remoteConnectionSchemeJUNGPanel.post( getConnectionSchemeRoot() );
//	}


	//
	private void spawnConduit(EntranceDescription entrance, ConduitDescription conduit, ExitDescription exit) throws SpawnAgentException {

		assert entrance.hasConduit(conduit);
		assert conduit.hasExit(exit);

		AID entranceAgent = entrance.getControllerID();

		AID exitAgent = exit.getControllerID();


		ArrayList<Object> optionalArgs = new ArrayList<Object>(conduit.getArgs().length);
		// fill optional args
		for(int i = 0; i < conduit.getArgs().length; i++) {
			optionalArgs.add(conduit.getArgs()[i]);
		}

		Location targetLocation = this.locationForConduit(entrance, conduit, exit);

		ConduitArgs conduitArgs = new ConduitArgs(
												entranceAgent, entrance.getID(), entrance.getDataTemplate()
												, exitAgent, exit.getID(), exit.getDataTemplate()
												, BasicConduit.LowBandwidthStrategy.class, targetLocation
												, optionalArgs);
		Object[] args = new Object[1];
		args[0] = conduitArgs;


		conduitCounter ++;
		AgentController conduitController;
		String agentName = "conduit#"+conduitCounter+"["+entranceAgent.getLocalName()+":"+entrance.getID()+">-"+conduit.getClassName()+"("+conduit.getID()+")->"+exitAgent.getLocalName()+":"+exit.getID()+"]";
		this.logger.info("spawning conduit:"+agentName);
		try {
			conduitController = this.getContainerController().createNewAgent(agentName, conduit.getClassName(), args);
		} catch (jade.wrapper.StaleProxyException e) {
			this.logger.severe("can not createNewAgent agent: "+agentName);
			throw new SpawnAgentException("getContainerController().createNewAgent failed -- "+e.getMessage(), e.getCause());
		}
		try {
			AID conduitAID = new AID(conduitController.getName(), AID.ISGUID);
			conduit.markAvailable(conduitAID);
		} catch (jade.wrapper.StaleProxyException e) {
			this.logger.severe("can not get name of agent: "+agentName);
			throw new SpawnAgentException("getName() failed -- "+e.getMessage(), e.getCause());
		}
		try {
			conduitController.start();
		} catch (jade.wrapper.StaleProxyException e) {
			this.logger.severe("can not start agent: "+agentName);
			throw new SpawnAgentException("start() failed -- "+e.getMessage(), e.getCause());
		}
	}


	//
	protected Location locationForConduit(EntranceDescription entrance, ConduitDescription conduit, ExitDescription exit) {

//		AID adjacentAgent = entrance.getControllerID();
//System.out.println("location for "+adjacentAgent);
//		Location location = null;
//		try {
//			location = muscle.utilities.agent.WhereIsAgentHelper.whereIsAgent(adjacentAgent, this);
//		} catch (Exception e) {
//			logger.severe("can not determine target location for "+adjacentAgent);
//			throw new MUSCLERuntimeException(e.getMessage(), e.getCause());
//		}
//
//System.out.println("location is "+location);
//
//		return location;
//return here();
		return null;
	}


	/**
	called after new portals heve been registered, see if we can spawn new conduits
	*/
	void portalsChanged() {

		// see if we can feed one of our unconnected exits
		for(Iterator<ExitDescription> exitIterator = this.cs.unconnectedExits().iterator(); exitIterator.hasNext();) {
			ExitDescription exit = exitIterator.next();
			// we can only create this entrance->conduit->exit chain if the exit is
			if( exit.isAvailable() ) {

// TODO test if conduit already exists (if it can feed multiple exits)
// currently only singlefeed is supported

				for(int i = 0; ; i++) {
					ConduitDescription conduit = exit.getConduitDescription(i);
					if( conduit == null) {
						break;
					}
					if( conduit.isAvailable() ) {
						continue; // already spawned
					}

					EntranceDescription entrance = conduit.getEntranceDescription();

					// we can only create this entrance->conduit->exit chain if the entrance is also available
					if( entrance.isAvailable() ) {
						try {
							this.spawnConduit(entrance, conduit, exit);
						} catch (SpawnAgentException e) {
							throw new MUSCLERuntimeException(e);
						}

						exitIterator.remove();
						break;
					}
				}
			}
		}
	}


	/**
	registers a newly activated entrance at the plumber
	*/
	public void addEntrance(String entranceID, DataTemplate dataTemplate, AID controllerID, EntranceDependency[] dependencies) {

		EntranceDescription entrance = this.cs.entranceDescriptionForID(entranceID);
		if(entrance != null) {
			if( entrance.isAvailable() ) {
				this.logger.severe("can not add entrance <"+entranceID+">, an entrance with the same id is already registered");
				return;
			}

			entrance.markAvailable(dataTemplate, controllerID, dependencies);

			try {
				this.testDeadlock(entrance, 0, new LinkedList<EntranceDescription>(), new ArrayList<Integer>());
			} catch (DeadlockException e) {
				throw new MUSCLERuntimeException(e);
			}
			this.logger.fine("no deadlock found for entrance:"+entrance.getID());

			this.portalsChanged();
		}
		// entrance does not exist in connection scheme
		else if( !this.cs.isComplete() ) {
			this.logger.info("postponing entrance <"+entranceID+"> -- it is not part of the current connection scheme");
			entrance = new EntranceDescription(entranceID);
			entrance.markAvailable(dataTemplate, controllerID, dependencies);
			if(this.postponedEntrances.contains(entrance)) {
				this.logger.severe("entrance <"+entranceID+"> -- already exists");
			} else {
				this.postponedEntrances.add(entrance);
			}
		}
		else {
			this.logger.severe("ignoring entrance <"+entranceID+"> -- it is not part of the final connection scheme");
		}
	}


	/**
	registers a newly activated exit at the plumber
	*/
	public void addExit(String exitID, DataTemplate dataTemplate, AID controllerID) {

		ExitDescription exit = this.cs.exitDescriptionForID(exitID);
		if(exit != null) {
			if( exit.isAvailable() ) {
				this.logger.severe("can not add exit <"+exitID+">, an exit with the same id is already registered");
				return;
			}

			exit.markAvailable(dataTemplate, controllerID);

			this.portalsChanged();
		}
		// exit does not exist in connection scheme
		else if( !this.cs.isComplete() ) {
			this.logger.info("postponing exit <"+exitID+"> -- it is not part of the current connection scheme");
			exit = new ExitDescription(exitID);
			exit.markAvailable(dataTemplate, controllerID);
			if(this.postponedExits.contains(exit)) {
				this.logger.severe("exit <"+exitID+"> -- already exists");
			} else {
				this.postponedExits.add(exit);
			}
		}
		else {
			this.logger.severe("ignoring exit <"+exitID+"> -- it is not part of the final connection scheme");
		}
	}


	/**
	unregister all portals associated with this controller agent
	*/
	private void removePortalsForControllerID(AID controllerID) {

		// unset from entrance descriptions
		List<EntranceDescription> entranceDescriptions = this.cs.entranceDescriptionsForControllerID(controllerID);
		for (EntranceDescription entrance : entranceDescriptions) {
			entrance.markUnavailable();
		}

		// unset from exit descriptions
		List<ExitDescription> exitDescriptions = this.cs.exitDescriptionsForControllerID(controllerID);
		for (ExitDescription exit : exitDescriptions) {
			exit.markUnavailable();
		}
	}


// TODO modify deadlock test to take different conduit I/O frequencies into account ?
	/**
		unroll communication chain until we reach<br>
		a) an entrance without dependencies -> no deadlock,<br>
		b) the entrance we started the search from if this produces output in the past -> no deadlock, else we have a deadlock
	*/
	private void testDeadlock(EntranceDescription entrance, int time, LinkedList<EntranceDescription> passedEntrances, ArrayList<Integer> passedTimes) throws DeadlockException {
// disabled because we got an exception with the sandbox for the BF
//		logger.fine("entering deadlock test for "+entrance.getID());
//
//		if(!entrance.isAvailable()) {
//			logger.info("entrance not available:"+entrance.getID()+" skipping deadlock test");
//			return;
//		}
//
//		passedEntrances.add(entrance);
//		passedTimes.add(new Integer(time));
//
//		EntranceDependency[] dependencies = entrance.getDependencies();
//		if(dependencies.length == 0) {
//			// no deadlock
//			return;
//		}
//
//		for(EntranceDependency d : dependencies) {
//
//			logger.fine("testing dependency for "+d.toString()+" @entrance <"+entrance.getID()+">");
//
//			// modify time dependency
//			time += d.getDtOffset();
//			assert(d.getDtOffset() <= 0) : "entrance can not depend on an exit which will be fed in the future";
//
//			// get the exit which belongs to this dependency
//			String exitID = d.getExit().getLocalName();
//			ExitDescription exit = cs.exitDescriptionForID(exitID);
//
//			if(exit == null) {
//				throw new DeadlockException("deadlock for entrance <"+passedEntrances.getFirst().getID()+"> reason: exit <"+exitID+"> does not exist");
//			}
//
//			for(int i = 0; ; i++) {
//				// get the conduit which should feed the exit
//				ConduitDescription conduit =  exit.getConduitDescription(i);
//				if( conduit == null)
//					break;
//
//				// get the entrance which feeds the conduit
//				EntranceDescription remoteEntrance = conduit.getEntranceDescription();
//
//				int index = passedEntrances.indexOf(remoteEntrance);
//
//				if(index > -1) {
//					// posible deadlock here, the communication chain links back to an entrance we already passed in this search
//
//					if(time < passedTimes.get(index)) {
//						// no deadlock
//						return;
//					}
//					else {
//						throw new DeadlockException("deadlock for entrance:<"+passedEntrances.getFirst().getID()+">");
//					}
//				}
//
//				// recursion with the remote entrance
//				testDeadlock(remoteEntrance, time, passedEntrances, passedTimes);
//			}
//		}
	}


	public ConnectionScheme initConnectionScheme() {

		// instantiate our connection scheme class
		Class<? extends ConnectionScheme> csClass = CxADescription.ONLY.getConnectionSchemeClass();
		try {
			return csClass.newInstance();
		}
		catch(java.lang.InstantiationException e) {
			throw new MUSCLERuntimeException(e);
		}
		catch(java.lang.IllegalAccessException e) {
			throw new MUSCLERuntimeException(e);
		}
	}


	/**
	declares a new edge (entrance->conduit->exit) of our graph
	*/
	public void addConnection(String entranceID, String conduitClassName, String conduitID, String[] conduitArgs, String exitID) {

		Pipeline pipeline = this.cs.addConnection(entranceID, conduitClassName, conduitID, conduitArgs, exitID);

		// release entrance/exit if they have been postponed
		int index = this.postponedExits.indexOf(pipeline.exit);
		if(index != -1) {
			ExitDescription e = this.postponedExits.remove(index);
			this.addExit(e.getID(), e.getDataTemplate(), e.getControllerID());
		}
		index = this.postponedEntrances.indexOf(pipeline.entrance);
		if(index != -1) {
			EntranceDescription e = this.postponedEntrances.remove(index);
			this.addEntrance(e.getID(), e.getDataTemplate(), e.getControllerID(), e.getDependencies());
		}
	}


	/**
	create custom SubscriptionInitiator to listen and react to conduit-entrance announcements
	*/
	private void addEntranceListener() {

		SubscriptionInitiator subscriber;

		DFAgentDescription agentDescription = new DFAgentDescription();

		ServiceDescription entranceDescription = new ServiceDescription();
//		entranceDescription.addLanguages("serialized ConduitEntrance");
		entranceDescription.addProtocols(Constant.Protocol.ANNOUNCE_ENTRANCE);
//		entranceDescription.setType("ConduitEntrance");
		agentDescription.addServices(entranceDescription);

		subscriber = new SubscriptionInitiator(this, DFService.createSubscriptionMessage(this, this.getDefaultDF(), agentDescription, new SearchConstraints())) {

			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			protected void handleInform(ACLMessage inform) {
		//System.out.println("Agent "+getLocalName()+": Notification received from DF: msg:\n"+inform.toString()+"\n\n");
		//System.out.println("performative:"+ACLMessage.getPerformative(inform.getPerformative()));
		//System.out.println("replyto:"+inform.getInReplyTo()); // matches the subscription message

				try {

					DFAgentDescription[] results = DFService.decodeNotification(inform.getContent());
		//System.out.println("results:"+results.length);
					for (int i = 0; i < results.length; ++i) {
						DFAgentDescription dfd = results[i];
		//AID provider = dfd.getName();
		//System.out.println("provider:"+provider.getLocalName());
						// The same agent may provide several services
						Iterator serviceIter = dfd.getAllServices(); // does not contain deregistered services of the service provider

						// something deregistered
						if(!serviceIter.hasNext()) {
							//throw new MUSCLERuntimeException("\n--- deregister of portals not supported ---\n");
							Plumber.this.removePortalsForControllerID(dfd.getName());
						}

						while(serviceIter.hasNext()) {
							ServiceDescription sd = (ServiceDescription)serviceIter.next();
							if( sd.getType().equals(Constant.Service.ENTRANCE) ) { // there might be other services as well
								Iterator entranceIter = sd.getAllProperties();
								while(entranceIter.hasNext()) {
									Property content = (Property)entranceIter.next();
									assert content.getName().equals(Constant.Key.ENTRANCE_INFO);
									HashMap<String, String> entranceProperties = (HashMap<String, String>)Plumber.this.xstream.fromXML((String)content.getValue());

									String entranceID = entranceProperties.get("Name");
									DataTemplate dataTemplate = (DataTemplate)Plumber.this.xstream.fromXML(entranceProperties.get("DataTemplate"));
									EntranceDependency[] dependencies = null;
									try {
									 dependencies = (EntranceDependency[])Plumber.this.xstream.fromXML(entranceProperties.get("Dependencies"));
									}
									catch (com.thoughtworks.xstream.converters.ConversionException e) {
										dependencies = new EntranceDependency[0];
										System.err.println("!!!!!!!!!!!!!!! Dependencies xstream failed !!!!!!!!!!!!!!!");
										e.printStackTrace();
//										try {
//											BufferedWriter bw = new BufferedWriter(new FileWriter("/home/joris/Desktop/Dependencies.xml"));
//											bw.write(entranceProperties.get("Dependencies"));
//											bw.close();
//											//e.printStackTrace();
//										}
//										catch (IOException ioe) {
//											ioe.printStackTrace();
//										}
										
									}
									Plumber.this.logger.info("found an entrance: <"+dfd.getName().getLocalName()+":"+entranceID+">");
									Plumber.this.addEntrance(entranceID, dataTemplate, dfd.getName(), dependencies);
								}
							}
						}
					}
				}
				catch (FIPAException e) {
					e.printStackTrace();
				}
			}
		};

		this.addBehaviour(subscriber);
	}


	/**
	create custom SubscriptionInitiator to listen and react to conduit-exit announcements
	*/
	private void addExitListener() {

		SubscriptionInitiator subscriber;

		DFAgentDescription agentDescription = new DFAgentDescription();

		ServiceDescription exitDescription = new ServiceDescription();
//		exitDescription.addLanguages("serialized ConduitExit");
		exitDescription.addProtocols(Constant.Protocol.ANNOUNCE_EXIT);
		exitDescription.setType(Constant.Service.EXIT);
		agentDescription.addServices(exitDescription);

		subscriber = new SubscriptionInitiator(this, DFService.createSubscriptionMessage(this, this.getDefaultDF(), agentDescription, new SearchConstraints())) {

			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			protected void handleInform(ACLMessage inform) {
		//System.out.println("Agent "+getLocalName()+": Notification received from DF: msg:\n"+inform.toString()+"\n\n");
		//System.out.println("performative:"+ACLMessage.getPerformative(inform.getPerformative()));
		//System.out.println("replyto:"+inform.getInReplyTo()); // matches the subscription message

				try {
					DFAgentDescription[] results = DFService.decodeNotification(inform.getContent());
		//System.out.println("results:"+results.length);
					for (int i = 0; i < results.length; ++i) {
						DFAgentDescription dfd = results[i];
		//AID provider = dfd.getName();
		//System.out.println("provider:"+provider.getLocalName());
						// The same agent may provide several services
						Iterator serviceIter = dfd.getAllServices(); // does not contain deregistered services of the service provider

						// something deregistered
						if(!serviceIter.hasNext()) {
							//throw new MUSCLERuntimeException("\n--- deregister of portals not supported ---\n");
							Plumber.this.removePortalsForControllerID(dfd.getName());
						}

						while(serviceIter.hasNext()) {
							ServiceDescription sd = (ServiceDescription)serviceIter.next();
							if( sd.getType().equals(Constant.Service.EXIT) ) {  // there might be other services as well
								Iterator exitIter = sd.getAllProperties();
								while(exitIter.hasNext()) {
									Property content = (Property)exitIter.next();
									assert content.getName().equals(Constant.Key.EXIT_INFO);
									HashMap<String, String> exitProperties = (HashMap<String, String>)Plumber.this.xstream.fromXML((String)content.getValue());

									String exitID = exitProperties.get("Name");
									DataTemplate dataTemplate = (DataTemplate)Plumber.this.xstream.fromXML(exitProperties.get("DataTemplate"));

									Plumber.this.logger.info("found an exit: <"+dfd.getName().getLocalName()+":"+exitID+">");
									Plumber.this.addExit(exitID, dataTemplate, dfd.getName());
								}
							}
						}
					}
				}
				catch (FIPAException e) {
					e.printStackTrace();
				}
			}
		};

		this.addBehaviour(subscriber);
	}


	//
	public static class DeadlockException extends Exception {

			/**
		 *
		 */
		private static final long serialVersionUID = 1L;

			public DeadlockException(String message) {
				super(message);
			}
	}

}

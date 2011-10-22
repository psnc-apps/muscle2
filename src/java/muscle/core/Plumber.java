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

import muscle.Constant;
import muscle.core.conduit.ConduitArgs;
import muscle.core.ConnectionScheme.Pipeline;
import muscle.exception.MUSCLERuntimeException;
import muscle.exception.SpawnAgentException;
import muscle.logging.AgentLogger;

import com.thoughtworks.xstream.XStream;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;
import jade.wrapper.AgentController;

import jadetool.DFServiceTool;
import jadetool.DFServiceTool.RegisterSingletonAgentException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import jade.core.Location;
import java.util.logging.Logger;
import utilities.FastArrayList;


/**
listens for kernel announcements and spawns conduits according to the connection scheme
@author Jan Hegewald
*/
public class Plumber extends Agent {

	public static final int MANDATORY_CONDUIT_ARG_COUNT = 7;

	private LinkedList<EntranceDescription> postponedEntrances = new LinkedList<EntranceDescription>();
	private LinkedList<ExitDescription> postponedExits = new LinkedList<ExitDescription>();
	
	private static int conduitCounter = 0;
	private static final Logger logger = Logger.getLogger(Plumber.class.getName());

	private transient XStream xstream = new XStream();

	private ConnectionScheme cs;
	
	//
	protected void setup() {
		if( DFServiceTool.hasSingletonAgent(this, Plumber.class.getName()) ) {
			logger.severe("can not start because there are already other Plumber(s) available for this platform");
			doDelete();
			return; // otherwise the setup would continue
		}
				
		// register self as singleton
		try {
			DFServiceTool.registerSingletonAgent(this, Plumber.class.getName());
		}
		catch(RegisterSingletonAgentException e) {
			logger.severe("can not start because there are already other Plumber(s) available for this platform");
			// bail out here
			throw new MUSCLERuntimeException(e);
		}

		cs = initConnectionScheme();
		logger.info(cs.toString());

		logger.info("Plumber of kind <"+getClass()+"> is up");

		addEntranceListener();
		addExitListener();
	}

	public ConnectionScheme getConnectionScheme() {
		return cs;
	}

	//
	private void spawnConduit(EntranceDescription entrance, ConduitDescription conduit, ExitDescription exit) throws SpawnAgentException {

		assert entrance.hasConduit(conduit);
		assert conduit.hasExit(exit);
		
		AID entranceAgent = entrance.getControllerID();

		AID exitAgent = exit.getControllerID();
		
		String[] optArgs = conduit.getArgs();
		String[] tmp = new String[optArgs.length];
		System.arraycopy(optArgs, 0, tmp, 0, optArgs.length);
		List<String> optionalArgs = new FastArrayList<String>(tmp);
		
		Location targetLocation = locationForConduit(entrance, conduit, exit);
		
		ConduitArgs conduitArgs = new ConduitArgs(
												entranceAgent, entrance.getID(), entrance.getDataTemplate()
												, exitAgent, exit.getID(), exit.getDataTemplate()
												, targetLocation, optionalArgs);
		Object[] args = new Object[1];
		args[0] = conduitArgs;										
		

		conduitCounter ++;
		AgentController conduitController;
		String agentName = "conduit#"+conduitCounter+"["+entranceAgent.getLocalName()+":"+entrance.getID()+">-"+conduit.getClassName()+"("+conduit.getID()+")->"+exitAgent.getLocalName()+":"+exit.getID()+"]";
		logger.info("spawning conduit:"+agentName);
		try {
			conduitController = getContainerController().createNewAgent(agentName, conduit.getClassName(), args);
		} catch (jade.wrapper.StaleProxyException e) {
			logger.severe("can not createNewAgent agent: "+agentName);
			throw new SpawnAgentException("getContainerController().createNewAgent failed -- "+e.getMessage(), e.getCause());
		}
		try {
			AID conduitAID = new AID(conduitController.getName(), AID.ISGUID);
			conduit.markAvailable(conduitAID);				
		} catch (jade.wrapper.StaleProxyException e) {
			logger.severe("can not get name of agent: "+agentName);
			throw new SpawnAgentException("getName() failed -- "+e.getMessage(), e.getCause());
		}
		try {
			conduitController.start();
		} catch (jade.wrapper.StaleProxyException e) {
			logger.severe("can not start agent: "+agentName);
			throw new SpawnAgentException("start() failed -- "+e.getMessage(), e.getCause());
		}
	}
	
	
	//
	protected Location locationForConduit(EntranceDescription entrance, ConduitDescription conduit, ExitDescription exit) {
		return null;
	}
	

	/**
	called after new portals heve been registered, see if we can spawn new conduits
	*/
	void portalsChanged() {
		// see if we can feed one of our unconnected exits
		for(Iterator<ExitDescription> exitIterator = cs.unconnectedExits().iterator(); exitIterator.hasNext();) {
			ExitDescription exit = exitIterator.next();
			// we can only create this entrance->conduit->exit chain if the exit is
			if( exit.isAvailable() ) {

// TODO test if conduit already exists (if it can feed multiple exits)
// currently only singlefeed is supported

				for(int i = 0; ; i++) {
					ConduitDescription conduit = exit.getConduitDescription(i);
					if( conduit == null)
						break;
					if( conduit.isAvailable() )
						continue; // already spawned
					
					EntranceDescription entrance = conduit.getEntranceDescription();
					
					// we can only create this entrance->conduit->exit chain if the entrance is also available
					if( entrance.isAvailable() ) {
						try {
							spawnConduit(entrance, conduit, exit);
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

		EntranceDescription entrance = cs.entranceDescriptionForID(entranceID);
		if(entrance != null) {			
			if( entrance.isAvailable() ) {
				logger.severe("can not add entrance <"+entranceID+">, an entrance with the same id is already registered");
				return;
			}
			
			entrance.markAvailable(dataTemplate, controllerID, dependencies);
			
			portalsChanged();
		}
		// entrance does not exist in connection scheme
		else if( !cs.isComplete() ) {
			logger.info("postponing entrance <"+entranceID+"> -- it is not part of the current connection scheme");
			entrance = new EntranceDescription(entranceID);
			entrance.markAvailable(dataTemplate, controllerID, dependencies);
			if(postponedEntrances.contains(entrance))
				logger.severe("entrance <"+entranceID+"> -- already exists");
			else
				postponedEntrances.add(entrance);
		}
		else {
			logger.severe("ignoring entrance <"+entranceID+"> -- it is not part of the final connection scheme");
		}
	}

	
	/**
	registers a newly activated exit at the plumber
	*/
	public void addExit(String exitID, DataTemplate dataTemplate, AID controllerID) {

		ExitDescription exit = cs.exitDescriptionForID(exitID);
		if(exit != null) {
			if( exit.isAvailable() ) {
				logger.severe("can not add exit <"+exitID+">, an exit with the same id is already registered");
				return;
			}

			exit.markAvailable(dataTemplate, controllerID);

			portalsChanged();
		}
		// exit does not exist in connection scheme
		else if( !cs.isComplete() ) {
			logger.info("postponing exit <"+exitID+"> -- it is not part of the current connection scheme");
			exit = new ExitDescription(exitID);
			exit.markAvailable(dataTemplate, controllerID);
			if(postponedExits.contains(exit))
				logger.severe("exit <"+exitID+"> -- already exists");
			else
				postponedExits.add(exit);
		}
		else {
			logger.severe("ignoring exit <"+exitID+"> -- it is not part of the final connection scheme");
		}
	}
	
	
	/**
	unregister all portals associated with this controller agent
	*/
	private void removePortalsForControllerID(AID controllerID) {

		// unset from entrance descriptions
		List<EntranceDescription> entranceDescriptions = cs.entranceDescriptionsForControllerID(controllerID);
		for(Iterator<EntranceDescription> iter = entranceDescriptions.iterator(); iter.hasNext();) {
			EntranceDescription entrance = iter.next();
			entrance.markUnavailable();
		}
		
		// unset from exit descriptions
		List<ExitDescription> exitDescriptions = cs.exitDescriptionsForControllerID(controllerID);
		for(Iterator<ExitDescription> iter = exitDescriptions.iterator(); iter.hasNext();) {
			ExitDescription exit = iter.next();
			exit.markUnavailable();
		}
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

		Pipeline pipeline = cs.addConnection(entranceID, conduitClassName, conduitID, conduitArgs, exitID);
		
		// release entrance/exit if they have been postponed
		int index = postponedExits.indexOf(pipeline.exit);
		if(index != -1) {
			ExitDescription e = postponedExits.remove(index);
			addExit(e.getID(), e.getDataTemplate(), e.getControllerID());
		}
		index = postponedEntrances.indexOf(pipeline.entrance);
		if(index != -1) {
			EntranceDescription e = postponedEntrances.remove(index);
			addEntrance(e.getID(), e.getDataTemplate(), e.getControllerID(), e.getDependencies());
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
		
		subscriber = new SubscriptionInitiator(this, DFService.createSubscriptionMessage(this, getDefaultDF(), agentDescription, new SearchConstraints())) {
		
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
							removePortalsForControllerID(dfd.getName());
						}
						
						while(serviceIter.hasNext()) {
							ServiceDescription sd = (ServiceDescription)serviceIter.next();
							if( sd.getType().equals(Constant.Service.ENTRANCE) ) { // there might be other services as well
								Iterator entranceIter = sd.getAllProperties();
								while(entranceIter.hasNext()) {
									Property content = (Property)entranceIter.next();
									assert content.getName().equals(Constant.Key.ENTRANCE_INFO);
									HashMap<String, String> entranceProperties = (HashMap<String, String>)xstream.fromXML((String)content.getValue());
								
									String entranceID = entranceProperties.get("Name");
									DataTemplate dataTemplate = (DataTemplate)xstream.fromXML(entranceProperties.get("DataTemplate"));
									EntranceDependency[] dependencies = (EntranceDependency[])xstream.fromXML(entranceProperties.get("Dependencies"));

									logger.info("found an entrance: <"+dfd.getName().getLocalName()+":"+entranceID+">");
									addEntrance(entranceID, dataTemplate, dfd.getName(), dependencies);																		
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
		
		addBehaviour(subscriber);		
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
		
		subscriber = new SubscriptionInitiator(this, DFService.createSubscriptionMessage(this, getDefaultDF(), agentDescription, new SearchConstraints())) {
		
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
							removePortalsForControllerID(dfd.getName());
						}
						
						while(serviceIter.hasNext()) {
							ServiceDescription sd = (ServiceDescription)serviceIter.next();
							if( sd.getType().equals(Constant.Service.EXIT) ) {  // there might be other services as well
								Iterator exitIter = sd.getAllProperties();
								while(exitIter.hasNext()) {
									Property content = (Property)exitIter.next();
									assert content.getName().equals(Constant.Key.EXIT_INFO);
									HashMap<String, String> exitProperties = (HashMap<String, String>)xstream.fromXML((String)content.getValue());
								
									String exitID = exitProperties.get("Name");
									DataTemplate dataTemplate = (DataTemplate)xstream.fromXML(exitProperties.get("DataTemplate"));

									logger.info("found an exit: <"+dfd.getName().getLocalName()+":"+exitID+">");
									addExit(exitID, dataTemplate, dfd.getName());																	
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
		
		addBehaviour(subscriber);		
	}
}

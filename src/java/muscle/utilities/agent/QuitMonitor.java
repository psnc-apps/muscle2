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

package muscle.utilities.agent;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.core.AID;
import jade.core.Agent;
import jade.core.Location;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.ShutdownPlatform;
import jade.domain.JADEAgentManagement.WhereIsAgentAction;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.ACLCodec.CodecException;
import jade.proto.AchieveREInitiator;

import java.util.HashMap;
import java.util.Iterator;

import muscle.behaviour.WatchAgentBehaviour;

import jade.core.behaviours.DataStore;
import jade.core.behaviours.SimpleBehaviour;
import jadetool.MessageTool;
import muscle.exception.MUSCLERuntimeException;
import muscle.behaviour.KillPlatformBehaviour;
import muscle.behaviour.PrintPlatformAgentsBehaviour;
import muscle.behaviour.PrintLocationAgentsBehaviour;
import java.util.logging.Logger;
import muscle.logging.AgentLogger;
import jade.wrapper.AgentController;
import jadetool.ContainerControllerTool;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.OneShotBehaviour;


/**
this agent monitors a given list of agents and kills the platform if all these agent are no longer available
@author Jan Hegewald
*/
public class QuitMonitor extends jade.core.Agent {
	
	enum AgentState {UNAVAILABLE, ACTIVATED, DONE};
	
	private HashMap<AID, AgentState> agentContainerMap = new HashMap<AID, AgentState>();
	private AgentLogger logger;

	
	/**
	spawns a QuitMonitor agent
	*/
	public static AID spawn(Agent ownerAgent, String[] watchNames) {

		// do not create a simple logger with an agent class name because we might use the agent class name to create an AgentLogger
		Logger simpleLogger = muscle.logging.Logger.getLogger(QuitMonitor.class);
							
		String agentName = javatool.ClassTool.getName(QuitMonitor.class);
		simpleLogger.info("spawning agent: <"+agentName+">");
			
		AgentController controller = ContainerControllerTool.createUniqueNewAgent(ownerAgent.getContainerController(), agentName, javatool.ClassTool.getName(QuitMonitor.class), watchNames);

		// get the AID of the spawned agent
		AID spawnedAID = null;
		try {
			spawnedAID = new AID(controller.getName(), AID.ISGUID);
		} catch (jade.wrapper.StaleProxyException e) {
			throw new MUSCLERuntimeException(e);
		}

		try {
			controller.start();
		} catch (jade.wrapper.StaleProxyException e) {
			throw new MUSCLERuntimeException(e);
		}

		return spawnedAID;
	}
	
	/**
	observes state of other agents and kills platform if those agents have been terminated<br>
	args must be an array of agent names which this agent should watch
	*/
	protected void setup() {

		logger = AgentLogger.getLogger(this);
		boolean parseError = false;
		Object[] args = getArguments();

		if( args != null ) {
			for(int i = 0; i < args.length; i++) {
				AID agentID = new AID((String)args[i], AID.ISLOCALNAME);
				agentContainerMap.put(agentID, AgentState.UNAVAILABLE);
			}
		}
		else
			parseError = true;
		
		if(parseError) {
			throw new MUSCLERuntimeException("can not read arguments");
		}

		
		StringBuilder text = new StringBuilder(javatool.ClassTool.getName(getClass())+"@ container:"+here().getName()+" is up and watching for agents:");
		for(Iterator<AID> iter = agentContainerMap.keySet().iterator(); iter.hasNext();)
			text.append("\n\t"+iter.next().getName());
			
		logger.info(text.toString());
		

		// add AgentWatchers for all our agents
		for(Iterator<AID> iter = agentContainerMap.keySet().iterator(); iter.hasNext();) {
			AID agentID = iter.next();
			addBehaviour(new WatchAgentBehaviour(this, agentID) {
			
				protected void agentCreated(Location location) {
				
					QuitMonitor.this.agentCreated(getTargetID());
				}

				protected void agentDeleted() {
				
					QuitMonitor.this.agentDeleted(getTargetID());
				}
			});
		}
	}
	

	// callback method for WatchAgentBehaviour
	private void agentCreated(AID agentID) {

		if(agentContainerMap.get(agentID) == AgentState.UNAVAILABLE) {
			agentContainerMap.put(agentID, AgentState.ACTIVATED);
		}
		else
			throw new MUSCLERuntimeException("Error: activated agent<"+agentID.getName()+"> twice");
	}


	// callback method for WatchAgentBehaviour
	private void agentDeleted(AID agentID) {

		if(agentContainerMap.containsValue(AgentState.UNAVAILABLE))
			logger.warning("Error: agent<"+agentID.getName()+"> already deleted but other agents are still missing");
		
		if(agentContainerMap.get(agentID) == AgentState.ACTIVATED) {
			agentContainerMap.put(agentID, AgentState.DONE);
		}
		else
			logger.severe("Error: agent<"+agentID.getName()+"> deleted but previous state was <"+agentContainerMap.get(agentID)+">");

		for(Iterator<AgentState> iter = agentContainerMap.values().iterator(); iter.hasNext();) {
			AgentState state = iter.next();

			if(state != AgentState.DONE)
				break;
			
			if(!iter.hasNext())
				tearDown();
		}
	}
	
	
	//
	private void tearDown() {

		SequentialBehaviour tearDownBehaviour = new SequentialBehaviour(this);

		tearDownBehaviour.addSubBehaviour(new PrintPlatformAgentsBehaviour(this));

//		tearDownBehaviour.addSubBehaviour(new OneShotBehaviour(this) {
//			public void action() {
//				try {
//					Thread.sleep(5000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}				
//			}
//		});
		
		tearDownBehaviour.addSubBehaviour(new KillPlatformBehaviour(this));	

		addBehaviour(tearDownBehaviour);
	}

}

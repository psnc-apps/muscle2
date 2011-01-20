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

import jade.core.AID;
import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.SequentialBehaviour;
import jade.wrapper.AgentController;
import jadetool.ContainerControllerTool;

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import muscle.behaviour.KillPlatformBehaviour;
import muscle.behaviour.PrintPlatformAgentsBehaviour;
import muscle.behaviour.WatchAgentBehaviour;
import muscle.exception.MUSCLERuntimeException;
import muscle.logging.AgentLogger;


/**
this agent monitors a given list of agents and kills the platform if all these agent are no longer available
@author Jan Hegewald
*/
public class QuitMonitor extends jade.core.Agent {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;


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
	@Override
	protected void setup() {

		this.logger = AgentLogger.getLogger(this);
		boolean parseError = false;
		Object[] args = this.getArguments();

		if( args != null ) {
			for (Object arg : args) {
				AID agentID = new AID((String)arg, AID.ISLOCALNAME);
				this.agentContainerMap.put(agentID, AgentState.UNAVAILABLE);
			}
		} else {
			parseError = true;
		}

		if(parseError) {
			throw new MUSCLERuntimeException("can not read arguments");
		}


		StringBuilder text = new StringBuilder(javatool.ClassTool.getName(this.getClass())+"@ container:"+this.here().getName()+" is up and watching for agents:");
		for (AID aid : this.agentContainerMap.keySet()) {
			text.append("\n\t"+aid.getName());
		}

		this.logger.info(text.toString());


		// add AgentWatchers for all our agents
		for (AID agentID : this.agentContainerMap.keySet()) {
			this.addBehaviour(new WatchAgentBehaviour(this, agentID) {

				/**
				 *
				 */
				private static final long serialVersionUID = 1L;

				@Override
				protected void agentCreated(Location location) {

					QuitMonitor.this.agentCreated(this.getTargetID());
				}

				@Override
				protected void agentDeleted() {

					QuitMonitor.this.agentDeleted(this.getTargetID());
				}
			});
		}
	}


	// callback method for WatchAgentBehaviour
	private void agentCreated(AID agentID) {

		if(this.agentContainerMap.get(agentID) == AgentState.UNAVAILABLE) {
			this.agentContainerMap.put(agentID, AgentState.ACTIVATED);
		} else {
			throw new MUSCLERuntimeException("Error: activated agent<"+agentID.getName()+"> twice");
		}
	}


	// callback method for WatchAgentBehaviour
	private void agentDeleted(AID agentID) {

		if(this.agentContainerMap.containsValue(AgentState.UNAVAILABLE)) {
			this.logger.warning("Error: agent<"+agentID.getName()+"> already deleted but other agents are still missing");
		}

		if(this.agentContainerMap.get(agentID) == AgentState.ACTIVATED) {
			this.agentContainerMap.put(agentID, AgentState.DONE);
		} else {
			this.logger.severe("Error: agent<"+agentID.getName()+"> deleted but previous state was <"+this.agentContainerMap.get(agentID)+">");
		}

		for(Iterator<AgentState> iter = this.agentContainerMap.values().iterator(); iter.hasNext();) {
			AgentState state = iter.next();

			if(state != AgentState.DONE) {
				break;
			}

			if(!iter.hasNext()) {
				this.tearDown();
			}
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

		this.addBehaviour(tearDownBehaviour);
	}

}

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
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.DataStore;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;

import muscle.behaviour.WhereIsAgentBehaviour;
import muscle.exception.MUSCLERuntimeException;
import muscle.logging.AgentLogger;


/**
helper agent to retrieve the location of an agent
@author Jan Hegewald
*/
public class WhereIsAgentHelper extends DoAgent {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private AID targetAgent;
	private AgentLogger logger;

	//
	public static Location whereIsAgent(AID targetAgent, Agent callerAgent) {

		Location location = null;

		MessageTemplate template = DoAgent.spawn(WhereIsAgentHelper.class, callerAgent, targetAgent);
		ACLMessage msg = callerAgent.blockingReceive(template);
		try {
			DataStore data = (DataStore)msg.getContentObject();
			location = (Location)data.get(WhereIsAgentBehaviour.DATASTORE_KEY);
		}
		catch(jade.lang.acl.UnreadableException e) {
			throw new MUSCLERuntimeException(e);
		}

		return location;
	}


	//
	@Override
	protected void optionalSetup(Object[] args) {

		this.logger = AgentLogger.getLogger(this);

		if(args.length == 0) {
			this.logger.severe("got no args to configure from -> terminating");
			this.doDelete();
			return;
		}

		if(! (args[0] instanceof AID)) {
			this.logger.severe("got invalid args to configure from <"+javatool.ClassTool.getName(args[0].getClass())+"> -> terminating");
			this.doDelete();
			return;
		}

		this.targetAgent = (AID)args[0];
	}


	//
	@Override
	protected ArrayList<Behaviour> getSubBehaviours() {

		ArrayList<Behaviour> behaviours = new ArrayList<Behaviour>();
		behaviours.add(new WhereIsAgentBehaviour(this, this.targetAgent));

		return behaviours;
	}

}
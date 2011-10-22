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


import jade.core.Agent;
import jade.core.AID;
import jade.core.Location;
import java.util.ArrayList;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.DataStore;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.behaviour.WhereIsAgentBehaviour;
import muscle.exception.MUSCLERuntimeException;


/**
helper agent to retrieve the location of an agent
@author Jan Hegewald
*/
public class WhereIsAgentHelper extends DoAgent {

	private final static transient Logger logger = Logger.getLogger(QuitMonitor.class.getName());
	private AID targetAgent;
	
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
	protected void optionalSetup(Object[] args) {
		if(args.length == 0) {
			logger.severe("got no args to configure from -> terminating");
			doDelete();		
			return;
		}

		if(! (args[0] instanceof AID)) {
			logger.log(Level.SEVERE, "got invalid args to configure from <{0}> -> terminating", javatool.ClassTool.getName(args[0].getClass()));
			doDelete();
			return;		
		}

		targetAgent = (AID)args[0];
	}
	
	
	//
	protected ArrayList<Behaviour> getSubBehaviours() {
		
		ArrayList<Behaviour> behaviours = new ArrayList<Behaviour>();
		behaviours.add(new WhereIsAgentBehaviour(this, targetAgent));
		
		return behaviours;
	}
	
}
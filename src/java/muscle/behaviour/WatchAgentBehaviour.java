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

package muscle.behaviour;

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
import jade.domain.JADEAgentManagement.WhereIsAgentAction;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.ACLCodec.CodecException;
import jade.proto.AchieveREInitiator;

import java.util.HashMap;
import java.util.Iterator;

import jade.core.behaviours.DataStore;
import jade.core.behaviours.SimpleBehaviour;




/**
watch an agent (when it activates/terminates)
@author Jan Hegewald
*/
public class WatchAgentBehaviour extends SimpleBehaviour {

	private AID targetID;
	private WhereIsAgentBehaviour containerRequester;
	//enum AgentState {ACTIVE, DELETED};
	private final static int ACTIVE = 1;
	private final static int GONE = 2;
	private int agentState = GONE;
	

	//
	public WatchAgentBehaviour(Agent ownerAgent, AID newTargetID) {
		
		super(ownerAgent);
		targetID = newTargetID;
	}

	public void onStart() {
		
		// add a requester to look for our agent
		containerRequester = new WhereIsAgentBehaviour(myAgent, targetID);
		myAgent.addBehaviour(containerRequester);
	}

	public void action() {
		
		assert containerRequester != null;
		if(containerRequester.done()) {
			Location location = containerRequester.getLocation();
			myAgent.removeBehaviour(containerRequester); // this is probably not necessary
			containerRequester = null;
			
			if(location != null) {
				if( agentState == GONE ) {
					agentState = ACTIVE;
					agentCreated(location);
				}
			}
			else {
				if( agentState == ACTIVE ) {
					agentState = GONE;
					agentDeleted();
				}
			}
			
			// add a new requester to look for our agent
			containerRequester = new WhereIsAgentBehaviour(myAgent, targetID);
			myAgent.addBehaviour(containerRequester);
		}
	}

	public boolean done() {

		return false;
	}

	public AID getTargetID() {
	
		return targetID;
	}
	
	//
	protected void agentCreated(Location location) {
	
		System.out.println("agent:"+targetID.getName()+" created at:"+location.getName());
	}

	//
	protected void agentDeleted() {
	
		System.out.println("agent:"+targetID.getName()+" deleted");		
	}
	
	

}



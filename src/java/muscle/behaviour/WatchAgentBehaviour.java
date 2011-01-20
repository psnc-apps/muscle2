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

import jade.core.AID;
import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.SimpleBehaviour;




/**
watch an agent (when it activates/terminates)
@author Jan Hegewald
*/
public class WatchAgentBehaviour extends SimpleBehaviour {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private AID targetID;
	private WhereIsAgentBehaviour containerRequester;
	//enum AgentState {ACTIVE, DELETED};
	private final static int ACTIVE = 1;
	private final static int GONE = 2;
	private int agentState = GONE;


	//
	public WatchAgentBehaviour(Agent ownerAgent, AID newTargetID) {

		super(ownerAgent);
		this.targetID = newTargetID;
	}

	@Override
	public void onStart() {

		// add a requester to look for our agent
		this.containerRequester = new WhereIsAgentBehaviour(this.myAgent, this.targetID);
		this.myAgent.addBehaviour(this.containerRequester);
	}

	@Override
	public void action() {

		assert this.containerRequester != null;
		if(this.containerRequester.done()) {
			Location location = this.containerRequester.getLocation();
			this.myAgent.removeBehaviour(this.containerRequester); // this is probably not necessary
			this.containerRequester = null;

			if(location != null) {
				if( this.agentState == GONE ) {
					this.agentState = ACTIVE;
					this.agentCreated(location);
				}
			}
			else {
				if( this.agentState == ACTIVE ) {
					this.agentState = GONE;
					this.agentDeleted();
				}
			}

			// add a new requester to look for our agent
			this.containerRequester = new WhereIsAgentBehaviour(this.myAgent, this.targetID);
			this.myAgent.addBehaviour(this.containerRequester);
		}
	}

	@Override
	public boolean done() {

		return false;
	}

	public AID getTargetID() {

		return this.targetID;
	}

	//
	protected void agentCreated(Location location) {

		System.out.println("agent:"+this.targetID.getName()+" created at:"+location.getName());
	}

	//
	protected void agentDeleted() {

		System.out.println("agent:"+this.targetID.getName()+" deleted");
	}



}



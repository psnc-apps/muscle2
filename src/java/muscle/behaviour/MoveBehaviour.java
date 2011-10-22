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

import java.util.logging.Level;
import java.util.logging.Logger;

import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.SimpleBehaviour;


/**
moves an agent
@author Jan Hegewald
*/
abstract public class MoveBehaviour extends SimpleBehaviour {
		
	private Location targetLocation;
	

	//
	public MoveBehaviour(Location newTargetLocation, Agent ownerAgent) {
		super(ownerAgent);
		targetLocation =  newTargetLocation;
	}
	

	//
	public void action() {

		if(myAgent.here().equals(targetLocation)) { // we are (already) at the designated location
			
			// retain a copy to our owner agent since removeBehaviour will set myAgent to null
			Agent myAgentCopy = myAgent;
			myAgent.removeBehaviour(this);
			callback(myAgentCopy);
		}
		else {
			Logger logger = Logger.getLogger(MoveBehaviour.class.getName());
			logger.log(Level.FINE, "moving to location <{0}>", targetLocation.getName());
			myAgent.doMove(targetLocation);
		}
	}


	//
	public boolean done() {

		return false;
	}
	

	//
	abstract public void callback(Agent ownerAgent);
}

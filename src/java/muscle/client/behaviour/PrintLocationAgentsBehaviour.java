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

package muscle.client.behaviour;

import jade.core.AID;
import jade.core.Agent;
import jade.core.Location;
import java.util.Iterator;

/**
prints the results of a LocationAgentsBehaviour to System.out
@author Jan Hegewald
*/
public class PrintLocationAgentsBehaviour extends jade.core.behaviours.SequentialBehaviour {

	private jade.util.leap.List agentIDs;
	Location location;
	
	public PrintLocationAgentsBehaviour(Agent a, Location newLocation) {
		super(a);
		location = newLocation;
	}
	
	public void onStart() {
		
		// add a requester to look for agents in a container
		LocationAgentsBehaviour agentsRequester = new LocationAgentsBehaviour(myAgent, location)
		 {
			public void callback(jade.util.leap.List newAgentIDs) {
				agentIDs = newAgentIDs;
			}
		};
		addSubBehaviour(agentsRequester);
	}

	public int onEnd() {
	
		// print results
		for(Iterator iter = agentIDs.iterator(); iter.hasNext();) {
			AID aid = (AID)iter.next();
			System.out.printf("%25s @ %s @ %s\n", aid.getName(), location.getName(), location.getAddress());
		}
	
		// reset so onStart will be called again
		reset();
		return super.onEnd();
	}

}	

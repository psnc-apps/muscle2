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

import jade.core.Agent;
import jade.core.ContainerID;

import java.util.Iterator;

/**
prints AIDs available in the platform to System.out
@author Jan Hegewald
*/
public class PrintPlatformAgentsBehaviour extends jade.core.behaviours.SequentialBehaviour {
	
	private jade.util.leap.List containerIDs;
	
	public PrintPlatformAgentsBehaviour(Agent a) {
		super(a);
	}
	
	public void onStart() {
		
		// add a requester to look for our containers
		PlatformLocationsBehaviour containerRequester = new PlatformLocationsBehaviour(myAgent)
		 {
			public void callback(jade.util.leap.List newContainerIDs) {
				containerIDs = newContainerIDs;
			}
		};
		addSubBehaviour(containerRequester);
	}

	public int onEnd() {
	
		// print results
		for(Iterator iter = containerIDs.iterator(); iter.hasNext();) {
			ContainerID cid = (ContainerID)iter.next();

			myAgent.addBehaviour(new PrintLocationAgentsBehaviour(myAgent, cid));
		}
	
		// reset so onStart will be called again
		reset();
		return super.onEnd();
	}

}	

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

package jadetool;

import jade.core.AID;
import jade.core.Agent;
import jade.core.Location;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import java.util.UUID;		


/**
additional functionality for jade.wrapper.ContainerController
@author Jan Hegewald
*/
public class ContainerControllerTool {


	//
	static public AgentController createUniqueNewAgent(ContainerController containerController, String baseName, String className, Object[] args) {

		String uniqueName = baseName;

// there seems to be a bug regarding to simultaneous access in controller.createNewAgent
// try a workaround so we do not ever have loop here
uniqueName = baseName+UUID.randomUUID().toString();

		AgentController agentController = null;
		while(true) {
			try {
				agentController = containerController.createNewAgent(uniqueName, className, args);
				break;
			} catch (jade.wrapper.StaleProxyException e) {
				String oldName = uniqueName;
				uniqueName = baseName+UUID.randomUUID().toString();
				System.out.println("ContainerControllerTool can not create agent: <"+oldName+">, trying with other name: <"+uniqueName+">");
			}
		}
 		
// 		// double check if this agent could be created
// 		try {
// 			jade.core.AID spawnedAID = new jade.core.AID(agentController.getName(), jade.core.AID.ISGUID);
// 		} catch (jade.wrapper.StaleProxyException e) {
// 			System.out.println("ContainerControllerTool can not create agent: <"+uniqueName+">");
// 			throw new RuntimeException(e);
// 		}
		
		return agentController;
	}

}
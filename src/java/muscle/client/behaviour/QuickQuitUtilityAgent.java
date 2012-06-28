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

import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
helper agent to immediately quit the agent platform 
@author Jan Hegewald
*/
public class QuickQuitUtilityAgent extends jade.core.Agent {
	private final static transient Logger logger = Logger.getLogger(QuickQuitUtilityAgent.class.getName());
	
	protected void setup() {
		addBehaviour(new KillPlatformBehaviour(this));
	}

	static public void launch(ContainerController jadeContainer) throws muscle.exception.SpawnAgentException {
		Class cls = QuickQuitUtilityAgent.class;
		
		Object[] args = new Object[0];		

		String agentName = cls.getName();
		AgentController controller = null;
		try {
			controller = jadeContainer.createNewAgent(agentName, cls.getName(), args);
		} catch (jade.wrapper.StaleProxyException e) {
			logger.log(Level.SEVERE, "can not createNewAgent agent: {0}", agentName);
			throw new muscle.exception.SpawnAgentException("createNewAgent failed -- "+e.getMessage(), e.getCause());
		}
		try {
			controller.start();
		} catch (jade.wrapper.StaleProxyException e) {
			logger.log(Level.SEVERE, "can not start agent: {0}", agentName);
			throw new muscle.exception.SpawnAgentException("start() failed -- "+e.getMessage(), e.getCause());
		}

	}
}

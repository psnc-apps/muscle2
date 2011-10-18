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

import java.util.ArrayList;
import java.util.List;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import java.util.logging.Logger;


/**
additional functionality for jade.domain.DFService
@author Jan Hegewald
*/
public class DFServiceTool {


	/**
	registers a single agent service description at the default DF
	*/
	static public void register(Agent agent, String serviceType, String serviceName) throws FIPAException {
	
		DFAgentDescription dfd = new DFAgentDescription();    
		dfd.setName(agent.getAID());
	
		ServiceDescription sd = new ServiceDescription();
		sd.setType(serviceType); // this is mandatory
		sd.setName(serviceName); // this is mandatory
	
		dfd.addServices(sd);

		DFService.register(agent, dfd);	
	}


	/**
	does a blocking search at the default DF for an agent service description<br>
	one of serviceType or serviceName may be null which would mean 'any'
	*/
	static public List<AID> agentForService(Agent agent, boolean block, String serviceType, String serviceName) throws FIPAException {

		if(serviceType == null && serviceName == null)
			throw new IllegalArgumentException("<String serviceType> and <String serviceName> can not both be null");
		
		long loopDelay = 500L;
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		if(serviceType != null)
			sd.setType(serviceType);
		if(serviceName != null)
			sd.setName(serviceName);
		dfd.addServices(sd);

		List<AID> serviceAgents = new ArrayList<AID>();
		Logger logger = muscle.logging.Logger.getLogger(DFServiceTool.class);
		while(true) {
			// we are in a while loop, so write something to the log
			logger.finer("searching for service <"+serviceType+"> <"+serviceName+">");

			DFAgentDescription[] results = DFService.search(agent, dfd);
			if( (results != null) && (results.length > 0) ) {
				
				for(DFAgentDescription description : results) {
					//dfd = result[0]; 
					serviceAgents.add( description.getName() );
				}

				break; // we found something
			}

			if( !block ) {
				// do the search only once
				break;
			}

			try {
				Thread.sleep(loopDelay);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}			
		}
		
		return serviceAgents;
	}


	/**
	true if this platform already has the specified singleton agent
	*/
	static public boolean hasSingletonAgent(Agent agent, final String id) {
	
		// search if this singleton id has already registered
		List<AID> otherIDs = null;
		try {
			otherIDs = DFServiceTool.agentForService(agent, false, id, id);
		} catch (FIPAException e) {
			// df search failed
			throw new RuntimeException(e);
		}

		// exit here if singleton already exists
		if(otherIDs.size() > 0) {
			return true;
		}

		return false;
	}


	/**
	this singleton registration does allow to register multiple agents with the same id if they are launched simultaneously from a single command
	*/
	static public void registerSingletonAgent(Agent agent, final String id) throws RegisterSingletonAgentException {
	
		// exit here if singleton already exists
		if( hasSingletonAgent(agent, id) ) {
			throw new RegisterSingletonAgentException("agent with id <"+id+"> is already registered");
		}

		// register with df
		DFAgentDescription dfd = new DFAgentDescription();    
		dfd.setName(agent.getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType(id); // this is mandatory
		sd.setName(id); // this is mandatory
		dfd.addServices(sd);
		
		try {
			DFService.register(agent, dfd);
		} catch (FIPAException e) {
			// registering with df failed
			throw new RuntimeException(e);
		}
	}


	//
	public static class RegisterSingletonAgentException extends Exception {
		public RegisterSingletonAgentException() {
			
		}
		public RegisterSingletonAgentException(String message) {
			super(message);
		}
		public RegisterSingletonAgentException(Throwable cause) {
			super(cause);
		}
		public RegisterSingletonAgentException(String message, Throwable cause) {
			super(message, cause);
		}
	}		
}

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

import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.content.onto.basic.Result;
import jade.core.AID;
import jade.core.Agent;
import jade.core.Location;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jadetool.MessageTool;
import muscle.exception.MUSCLERuntimeException;



/**
requests Location for a given agent
@author Jan Hegewald
*/
public class WhereIsAgentBehaviour extends AchieveREInitiator {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	public static Class<?> DATASTORE_KEY = WhereIsAgentBehaviour.class;
	private Location location;
	private AID targetID;


	//
	public WhereIsAgentBehaviour(Agent ownerAgent, AID newTargetID) {
		super(ownerAgent, MessageTool.createWhereIsAgentRequest(ownerAgent, newTargetID));
		this.targetID = newTargetID;
	}


	//
	public AID getTargetID() {

		return this.targetID;
	}


	//
	public Location getLocation() {

		return this.location;
	}


	//
	@Override
	protected void handleInform(ACLMessage inform) {
		Result result = null;
		try {

			result = (Result) this.myAgent.getContentManager().extractContent(inform);
		} catch (UngroundedException e) {
			throw new RuntimeException(e);
		} catch (OntologyException e) {
			throw new RuntimeException(e);
		} catch (jade.content.lang.Codec.CodecException e) {
			throw new RuntimeException(e);
		}

		this.location = (Location)result.getValue();
		// also alow to share our results via a DataStore
		if(!this.getDataStore().containsKey(WhereIsAgentBehaviour.DATASTORE_KEY)) {
			this.getDataStore().put(WhereIsAgentBehaviour.DATASTORE_KEY, this.location);
		}
		else {
			throw new MUSCLERuntimeException("can not insert results to datastore because the key <"+WhereIsAgentBehaviour.DATASTORE_KEY+"> already exists");
		}
	}


}

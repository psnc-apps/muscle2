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
import jade.core.Agent;
import jade.core.Location;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jadetool.MessageTool;
import muscle.exception.JADERuntimeException;


/**
requests AIDs available in a given location
@author Jan Hegewald
*/
public abstract class LocationAgentsBehaviour extends AchieveREInitiator {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private jade.util.leap.List aids; // the leap list is not a generic class )-;

	public LocationAgentsBehaviour(Agent ownerAgent, Location location) {

		super(ownerAgent, MessageTool.createQueryAgentsOnLocationRequest(ownerAgent, location));
	}

	@Override
	protected final void handleInform(ACLMessage inform) {
		Result result = null;
		try {

			result = (Result)this.myAgent.getContentManager().extractContent(inform);
		} catch (UngroundedException e) {
			throw new JADERuntimeException(e);
		} catch (OntologyException e) {
			throw new JADERuntimeException(e);
		} catch (jade.content.lang.Codec.CodecException e) {
			throw new JADERuntimeException(e);
		}

		this.aids = result.getItems();

		// we are done
		this.myAgent.removeBehaviour(this);

		this.callback(this.aids);
	}

	public abstract void callback(jade.util.leap.List agentIDs);
}

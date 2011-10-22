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

package muscle.core.conduit;

import jade.lang.acl.ACLMessage;
import muscle.Constant;
import muscle.core.DataTemplate;
import muscle.exception.MUSCLERuntimeException;


/**
this is a placeholder conduit which does not pass any data. instead it directly connects the sending with the receiving kernel
@author Jan Hegewald
*/
public class VoidConduit extends BasicConduit {
	
	
	//
   @Override
	protected void constructMessagePassingMechanism() {

		// test if entrance and exit templates match, but do not create a filter chain
		// there should be no filter mechanism for this conduit
		
		// we do not use any manipulating filters here,
		// so the out template must be identical with the in template
		try {
			if( !DataTemplate.match(getEntranceDataTemplate(), getExitDataTemplate()) ) {
				throw new muscle.exception.DataTemplateMismatchException(getEntranceDataTemplate().toString()+" vs. "+getExitDataTemplate().toString());
			}
		}
		catch (muscle.exception.DataTemplateMismatchException e) {
			throw new MUSCLERuntimeException(e);
		}
	}
	
	
	/**
	connect so source kernel (e.g. a remote sender)
	*/
   @Override
	protected void attach() {

		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol(Constant.Protocol.PORTAL_ATTACH);
		msg.addUserDefinedParameter("entrance", entranceName);
		msg.addUserDefinedParameter("exit", exitName);
		msg.addReceiver(entranceAgent);
		msg.setContent(exitName); // sink id

		// overwrite us as sender with the exitAgent
		msg.setSender(exitAgent.getAID());

		send(msg);
	}
}

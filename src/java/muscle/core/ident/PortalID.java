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

package muscle.core.ident;

import jade.core.AID;

/**
portal identifier
@author Jan Hegewald
*/
public class PortalID extends JadeAgentID implements Identifier {
	private final JadeAgentID ownerID;
	public PortalID(String newName, JadeAgentID newAgentID) {
		super(newName, newAgentID.getAID());
		this.ownerID = newAgentID;
	}
	
	public PortalID(String newName, AID newAgentID) {
		this(newName, new JadeAgentID(newAgentID));
	}
	
	@Override
	public String getName() {
		return name+"@"+id.getLocalName();
	}
	
	public JadeAgentID getOwnerID() {
		return this.ownerID;
	}
	
	@Override
	public IDType getType() {
		return IDType.port;
	}
	
	@Override
	public Location getLocation() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}

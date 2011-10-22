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

import java.io.Serializable;


/**
portal identifier
@author Jan Hegewald
*/
public class PortalID extends JadeAgentID implements Serializable, Identifier {
	public PortalID(String newName, Identifier newAgentID) {
		super(newName, newAgentID);
	}
	
	@Override
	public String getName() {
		return name+"@"+id.getLocalName();
	}
	
	@Override
	public IDType getType() {
		return IDType.port;
	}

	@Override
	public Location getLocation() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	@Override
	public int compareTo(Identifier t) {
		if (t instanceof PortalID && super.equals(t)) {
			return this.name.compareTo(((PortalID)t).name);
		}
		else {
			return super.compareTo(t);
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (!super.equals(o)) return false;
		return this.name.equals(((PortalID)o).name);
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + super.hashCode();
		hash = 31 * hash + this.name.hashCode();
		return hash;
	}
}

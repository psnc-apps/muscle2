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
package muscle.core;

import java.io.Serializable;
import java.util.List;
import muscle.id.PortalID;

/**
stores info necessary to setup an entrance
@author Joris Borgdorff
 */
public class PortDescription implements Serializable {
	private final PortalID id;
	private final List<String> args;
	private ConduitDescription conduit;
	
	public PortDescription(PortalID newID, List<String> args) {
		this.id = newID;
		this.args = args;
	}

	public PortalID getID() {
		return id;
	}
	
	public ConduitDescription getConduitDescription() {
		return conduit;
	}
	
	public void setConduitDescription(ConduitDescription description) {
		conduit = description;
	}
	
	public boolean equals(Object b) {
		return b != null && getClass().equals(b.getClass()) && id.equals(((PortDescription)b).getID());
	}
	
	public List<String> getArgs() {
		return this.args;
	}
		
	@Override
	public int hashCode() {
		int hash = 5;
		hash = 79 * hash + this.id.hashCode();
		return hash;
	}
}

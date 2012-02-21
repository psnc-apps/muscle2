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
import muscle.core.ident.PortalID;

/**
stores info necessary to setup an entrance
@author Joris Borgdorff
 */
public class PortDescription implements Serializable {
	private PortalID id;
	private ConduitDescription conduit;
	private DataTemplate dataTemplate;
	
	public PortDescription(PortalID newID) {
		this.id = newID;
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

	public void markAvailable(DataTemplate newDataTemplate) {
		dataTemplate = newDataTemplate;
	}

	public DataTemplate getDataTemplate() {
		return dataTemplate;
	}

	public boolean isAvailable() {
		return id.isResolved();
	}

	public void markUnavailable() {
		dataTemplate = null;
		id.unResolve();
	}
	
	public boolean equals(Object b) {
		return b != null && getClass().equals(b.getClass()) && id.equals(((PortDescription)b).getID());
	}
		
	@Override
	public int hashCode() {
		int hash = 5;
		hash = 79 * hash + (this.id != null ? this.id.hashCode() : 0);
		return hash;
	}
}

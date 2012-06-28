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

/**
portal identifier
@author Joris Borgdorff
*/
public class PortalID<E extends Identifier> extends AbstractID implements Identifier {
	protected final E ownerID;
	
	public PortalID(String newName, E newAgentID) {
		super(newName);
		this.ownerID = newAgentID;
	}
	
	public boolean isResolved() {
		return ownerID.isResolved();
	}
	
	@Override
	public String getName() {
		return name+"@"+ownerID.getName();
	}
	
	public String getPortName() {
		return name;
	}
	
	public E getOwnerID() {
		return this.ownerID;
	}
	
	public void unResolve() {
		ownerID.unResolve();
	}
	
	@Override
	public IDType getType() {
		return IDType.port;
	}

	public Location getLocation() {
		return ownerID.getLocation();
	}
	
	public void resolveLike(Identifier id) {
		this.ownerID.resolveLike(id);
	}
}

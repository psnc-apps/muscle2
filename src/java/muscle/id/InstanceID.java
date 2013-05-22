/*
* Copyright 2008, 2009 Complex Automata Simulation Technique (COAST) consortium
* Copyright 2010-2013 Multiscale Applications on European e-Infrastructures (MAPPER) project
*
* GNU Lesser General Public License
* 
* This file is part of MUSCLE (Multiscale Coupling Library and Environment).
* 
* MUSCLE is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* MUSCLE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public License
* along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/
/*
 * 
 */
package muscle.id;

/**
 *
 * @author Joris Borgdorff
 */
public class InstanceID extends AbstractID {
	private static final long serialVersionUID = 1L;
	protected Location loc;
	private boolean canBeResolved;
	private boolean isAvailable;
	
	public InstanceID(String name) {
		this(name, null);
		this.canBeResolved = true;
		this.isAvailable = false;
	}
	
	public InstanceID(String name, Location loc) {
		super(name);
		this.loc = loc;
	}

	public synchronized void resolve(Location loc) {
		if (!this.canBeResolved()) {
			throw new IllegalStateException("Can not resolve unresolvable identifier");
		}
		this.loc = loc;
	}
	
	public IDType getType() {
		return IDType.instance;
	}
	
	public synchronized boolean isResolved() {
		return this.loc != null;
	}
	
	public synchronized void unResolve() {
		this.loc = null;
		this.isAvailable = false;
	}

	public Location getLocation() {
		return this.loc;
	}
	
	public void resolveLike(Identifier id) {
		this.resolve(id.getLocation());
		this.isAvailable = id.isAvailable();
	}

	@Override
	public synchronized boolean canBeResolved() {
		return this.canBeResolved;
	}

	@Override
	public synchronized void willNotBeResolved() {
		this.canBeResolved = false;
		this.unResolve();
	}
	
	public synchronized boolean isAvailable() {
		return this.isAvailable;
	}
	
	public synchronized void setAvailable(boolean available) {
		this.isAvailable = available;
	}
}

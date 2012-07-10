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
package muscle.client.instance;

import java.io.Serializable;
import muscle.core.DataTemplate;
import muscle.core.kernel.InstanceController;
import muscle.core.model.Timestamp;
import muscle.id.Identifiable;
import muscle.id.PortalID;

public abstract class PassivePortal<T extends Serializable> implements Serializable, Identifiable<PortalID> {
	protected final PortalID portalID;
	protected Timestamp customSITime;
	private int usedCount;
	protected final static long WAIT_FOR_ATTACHMENT_MILLIS = 10000l;
	
	PassivePortal(PortalID newPortalID, InstanceController newOwnerAgent, DataTemplate newDataTemplate) {
		portalID = newPortalID;
	
		// set custom time to 0
		customSITime = new Timestamp(0d);
		this.usedCount = 0;
	}

	public String getLocalName() {
		return portalID.getName();
	}

	public PortalID getIdentifier() {
		return portalID;
	}

	/**
	current time of this portal in SI units
	 */
	public synchronized Timestamp getSITime() {
		return customSITime;
	}
	
	public synchronized void setNextTimestamp(Timestamp t) {
		this.customSITime = t;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !obj.getClass().equals(this.getClass())) return false;
		
		return ((PassivePortal) obj).portalID.equals(portalID);
	}

	@Override
	public int hashCode() {
		return portalID.hashCode();
	}

	@Override
	public String toString() {
		return getLocalName() + " used: " + usedCount + " SI time: " + getSITime();
	}

	void increment() {
		usedCount++;
	}
}

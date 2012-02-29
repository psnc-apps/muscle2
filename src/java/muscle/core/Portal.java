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
import muscle.core.ident.Identifiable;
import muscle.core.ident.PortalID;
import muscle.core.kernel.InstanceController;
import muscle.core.messaging.Timestamp;
import muscle.utilities.parallelism.SafeTriggeredThread;

public abstract class Portal<T extends Serializable> extends SafeTriggeredThread implements Serializable, Identifiable {
	public final static int LOOSE = -1;
	protected final PortalID portalID;
	protected Timestamp customSITime;
	private final boolean loose;
	private int usedCount;
	protected final static long WAIT_FOR_ATTACHMENT_MILLIS = 10000l;
	
	Portal(PortalID newPortalID, InstanceController newOwnerAgent, int newRate, DataTemplate newDataTemplate) {
		portalID = newPortalID;
	
		// set custom time to 0
		customSITime = new Timestamp(0d);
		this.loose = newRate == LOOSE;
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
	public Timestamp getSITime() {
		if (loose)
			return new Timestamp(0d);
		else
			return customSITime;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !obj.getClass().equals(this.getClass())) return false;
		
		return ((Portal) obj).portalID.equals(portalID);
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

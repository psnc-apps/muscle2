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

import muscle.core.ident.PortalID;
import java.io.Serializable;
import javax.measure.DecimalMeasure;
import javax.measure.quantity.Duration;
import java.math.BigDecimal;
import javatool.DecimalMeasureTool;
import muscle.core.kernel.InstanceController;

//
public abstract class Portal<T> implements Serializable {
	private PortalID portalID;
	private int usedCount;
	private final DecimalMeasure<Duration> customSITime;
	private DataTemplate dataTemplate;
	protected InstanceController controller;
	
	Portal(PortalID newPortalID, InstanceController newOwnerAgent, int newRate, DataTemplate newDataTemplate) {
		portalID = newPortalID;
		controller = newOwnerAgent;
		
	
		// set custom time to 0
		customSITime = DecimalMeasure.valueOf(new BigDecimal(0), newDataTemplate.getScale().getDt().getUnit());
		dataTemplate = newDataTemplate;
	}

	// remove this in favor of the close method?
	public void detachOwnerAgent() {
	}

	public String getLocalName() {
		return portalID.getName();
	}

	public PortalID getPortalID() {
		return portalID;
	}

	// free our resources and disallow passing of messages
	// TODO: switch to a NULL implementation after close (put current impl in a strategy and duplicate public interface of that strategy in the portal)
	private void close() {
		usedCount = Integer.MAX_VALUE;
	}

	/**
	current time of this portal in SI units
	 */
	public DecimalMeasure<Duration> getSITime() {
		return customSITime;
	}

	public DataTemplate getDataTemplate() {
		return this.dataTemplate;
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

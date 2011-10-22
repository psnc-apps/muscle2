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
	public static final int LOOSE = -1; // if there is no rate accociated with this portal
	transient InstanceController ownerAgent;
	private PortalID portalID;
	private DataTemplate dataTemplate;
	private int usedCount;
	private int rate;
	private DecimalMeasure<Duration> customSITime;
	
	Portal(PortalID newPortalID, InstanceController newOwnerAgent, int newRate, DataTemplate newDataTemplate) {
		portalID = newPortalID;
		ownerAgent = newOwnerAgent;
		rate = newRate;
		dataTemplate = newDataTemplate;

		// set custom time to 0
		customSITime = DecimalMeasure.valueOf(new BigDecimal(0), dataTemplate.getScale().getDt().getUnit());
	}

	/**
	if a portal is deserialized, we need to attach it to the current owner agent
	 */
	public void setOwner(InstanceController newOwnerAgent) {
		ownerAgent = newOwnerAgent;
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

	public DataTemplate getDataTemplate() {
		return dataTemplate;
	}

	/**
	true if this portal does not pass data at a predefined rate (e.g. every iteration of the kernel) 
	 */
	public boolean isLoose() {
		return rate == LOOSE;
	}
	// temporary workaround to be able to use portals only once (their time will increment only once which makes it hard for the RawKernel to tell if it is still in use)
	private boolean oneShot = false;

	public void oneShot() {
		oneShot = true;
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
		if (rate == LOOSE) {
			// return dt*usedCount*rate
			return customSITime;
		} else {
			// return dt*usedCount*rate
			return DecimalMeasureTool.multiply(dataTemplate.getScale().getDt(), new BigDecimal(usedCount * rate));
		}
	}

	public boolean equals(Object obj) {
		if (obj == null || !obj.getClass().equals(this.getClass())) return false;
		
		return ((Portal) obj).getLocalName().equals(getLocalName());
	}

	@Override
	public int hashCode() {
		return getLocalName().hashCode();
	}

	public String toString() {
		return getLocalName() + " used: " + usedCount + " scale: " + dataTemplate.getScale() + " SI time: " + getSITime();
	}

	void increment() {
		usedCount++;
		if (oneShot) {
			close();
		}
	}
}

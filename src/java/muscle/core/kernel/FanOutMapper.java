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

package muscle.core.kernel;

import java.io.Serializable;
import muscle.core.ConduitExit;
import muscle.core.model.Observation;

/**
 * A fan-out mapper.
 * Use the value variable in to send in writeAll().
 * @author Joris Borgdorff
 * @param <T> received data type
 */
public abstract class FanOutMapper<T extends Serializable> extends Mapper {
	protected ConduitExit<T> onlyExit;
	protected Observation<T> value;

	@SuppressWarnings("unchecked")
	@Override
	public void addPortals() {
		super.addPortals();
		if (this.exits.size() != 1) {
			throw new IllegalStateException("A fan-out mapper only allows a single output, instead of " + exits.size());
		}
		onlyExit = (ConduitExit<T>)this.exits.values().iterator().next().getExit();
	}

	@Override
	protected boolean continueComputation() {
		return onlyExit.hasNext();
	}
	
	@Override
	protected boolean readyForContinue() {
		return onlyExit.ready();
	}
	@Override
	protected boolean readyForReceive() {
		return onlyExit.ready();
	}
	
	@Override
	protected final void receiveAll() {
		value = onlyExit.receiveObservation();
	}
}

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

package muscle.core.kernel;

import java.io.Serializable;
import muscle.core.ConduitEntrance;
import muscle.core.model.Observation;

/**
 * A fan-in mapper.
 * Set the value variable in readAll(), this will be sent.
 * @author Joris Borgdorff
 * @param <T> sent data type
 */
public abstract class FanInMapper<T extends Serializable> extends Mapper {
	protected ConduitEntrance<T> onlyEntrance;
	protected Observation<T> value;

	@Override
	@SuppressWarnings("unchecked")
	public void addPortals() {
		super.addPortals();
		if (this.entrances.size() != 1) {
			throw new IllegalStateException("A fan-in mapper only allows a single output, instead of " + entrances.size());
		}
		onlyEntrance = this.entrances.values().iterator().next().getEntrance();
	}

	@Override
	protected final void sendAll() {
		onlyEntrance.send(value);
	}
}

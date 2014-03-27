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

package muscle.core.conduit.terminal;

import java.io.Serializable;
import muscle.core.ConduitEntrance;
import muscle.core.ConduitEntranceController;
import muscle.core.conduit.filter.FilterChain;
import muscle.core.model.Observation;
import muscle.util.logging.ActivityListener;

/**
 * Receives messages.
 * Override to implement useful behavior.
 * @author Joris Borgdorff
 * @param <T> datatype that the Sink accepts
 */
public abstract class Sink<T extends Serializable> extends Terminal implements ConduitEntranceController<T> {
	private ConduitEntrance<T> entrance;
	
	public Sink() {
		entrance = null;
	}
	
	@Override
	public ConduitEntrance<T> getEntrance() {
		return this.entrance;
	}
	@Override
	public void setEntrance(ConduitEntrance<T> entrance) {
		this.entrance = entrance;
		entrance.setActivityLogger(actLogger, getOpposingIdentifier());
	}
	@Override
	public void setActivityLogger(ActivityListener actLogger) {
		this.actLogger = actLogger;
		if (this.entrance != null) this.entrance.setActivityLogger(actLogger, getOpposingIdentifier());
	}
	@Override
	public boolean waitUntilEmpty() {
		return true;
	}
	
	@Override
	public boolean isEmpty() {
		return true;
	}
	
	@Override
	public boolean hasTransmitter() {
		return true;
	}
	
	@Override
	public String toString() {
		return getIdentifier().getPortName() + ">" + getClass().getSimpleName();
	}

	@Override
	public void send(Observation<T> msg) {
		resetTime(msg.getTimestamp());
		if (filters == null) {
			process(msg);
		} else {
			filters.process(msg);
			filters.apply();
		}
	}
	
	
	@Override
	protected final FilterChain createFilterChainObject() {
		return new FilterChain() {
			@Override @SuppressWarnings("unchecked")
			public void queue(Observation subject) {
				Sink.this.process(subject);
			}
		};
	}
	
	protected abstract void process(Observation<T> msg);
}

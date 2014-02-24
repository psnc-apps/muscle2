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
import muscle.core.ConduitExit;
import muscle.core.ConduitExitController;
import muscle.core.model.Observation;
import muscle.util.data.Takeable;
import muscle.util.logging.ActivityListener;

/**
 * Generates data each time receive is called.
 * @author Joris Borgdorff
 */
public abstract class Source<T extends Serializable> extends Terminal implements ConduitExitController<T>, Takeable<Observation<T>> {
	private ConduitExit<T> exit;
	
	@Override
	public Takeable<Observation<T>> getMessageQueue() {
		return this;
	}
	@Override
	public void messageReceived(Observation<T> obs) {
		super.resetTime(obs.getNextTimestamp());
	}
	@Override
	public ConduitExit<T> getExit() {
		return this.exit;
	}
	@Override
	public void setExit(ConduitExit<T> exit) {
		this.exit = exit;
		this.exit.setActivityLogger(actLogger, getOpposingIdentifier());
	}
	
	@Override
	public void setActivityLogger(ActivityListener actLogger) {
		this.actLogger = actLogger;
		if (exit != null) exit.setActivityLogger(actLogger, getOpposingIdentifier());
	}
	
	@Override
	public String toString() {
		return getIdentifier().getPortName() + "<" + getClass().getSimpleName();
	}
}

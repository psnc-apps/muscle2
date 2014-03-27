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
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import muscle.core.ConduitExit;
import muscle.core.ConduitExitController;
import muscle.core.conduit.filter.FilterChain;
import muscle.core.model.Observation;
import muscle.util.data.Takeable;
import muscle.util.logging.ActivityListener;

/**
 * Generates data each time receive is called.
 * @author Joris Borgdorff
 * @param <T> datatype that the Source generates
 */
public abstract class Source<T extends Serializable> extends Terminal implements ConduitExitController<T>, Takeable<Observation<T>> {
	private ConduitExit<T> exit;
	private final Queue<Observation<T>> queue;

	public Source() {
		this.queue = new LinkedList<Observation<T>>();
	}
	
	@Override
	public Takeable<Observation<T>> getMessageQueue() {
		return this;
	}
	@Override
	public void messageReceived(Observation<T> obs) {
		resetTime(obs.getNextTimestamp());
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
	
	@Override
	public final Observation<T> take() throws InterruptedException {
		if (this.filters == null) {
			return generate();
		} else {
			while (queue.isEmpty()) {
				this.filters.process(generate());
				this.filters.apply();
			}
			return queue.remove();
		}
	}
	
	@Override
	protected final void modifyFilterArgs(List<String> args) {
		args.remove("thread");
		args.remove("muscle.core.conduit.filter.ThreadedFilter");
	}
	
	@Override
	protected final FilterChain createFilterChainObject() {
		return new FilterChain() {
			@Override @SuppressWarnings("unchecked")
			public void queue(Observation subject) {
				queue.add(subject);
			}
		};
	}
	
	protected abstract Observation<T> generate() throws InterruptedException;
}

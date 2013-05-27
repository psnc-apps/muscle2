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

import java.util.logging.Logger;
import muscle.core.ConduitEntranceController;
import muscle.core.ConduitExitController;
import muscle.core.Scale;

/**
 * A mapper instance. Newly created mappers should inherit from this class.
 * @author Joris Borgdorff
 */
public abstract class Mapper extends Instance {
	private final static Logger logger = Logger.getLogger(Mapper.class.getName());

	protected enum Step {
		INIT, RECEIVE, SEND, CONTINUE, END;
	}
	protected Step step = Step.INIT;
	
	/**
	 * Executes the standard mapper workflow.
	 * While continue computation: read all, perform mapping and write all.
	 */
	@Override
	protected final void execute() {
		while (!this.steppingFinished()) {
			step();
		}
	}
	
	/**
	 * Read from all conduits. Override to read from conduits, process it, and save the relevant results for sending.
	 */
	protected abstract void receiveAll();

	/**
	 * Write to all conduits. Override to process data, and write to conduits, and do any post-processing.
	 */
	protected abstract void sendAll();

	/**
	 * Whether the mapper should continue computation.
	 * By default, this is determined by checking all conduit exits whether they will receive a next message. If not,
	 * it will return false. If some conduits have a next messages and others do not, it will return false and output a warning,
	 * since those messages are then considered lost.
	 */
	protected boolean continueComputation() {
		int next = 0;
		for (ConduitExitController ec : this.exits.values()) {
			if (ec.getExit().hasNext()) {
				next++;
			}
		}
		if (next == this.exits.size()) {
			return true;
		} else {
			if (next > 0) {
				logger.severe("mapper received messages on too few ports. These messages are lost.");
			}
			return false;
		}
	}
	
	/**
	 * Whether the mapper can call {@see continueComputation()} without blocking.
	 * This implementation checks whether each conduit exit returns true for {@see muscle.core.ConduitExit.ready()}.
	 */
	protected boolean readyForContinue() {
		for (ConduitExitController ec : this.exits.values()) {
			if (!ec.getExit().ready()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Whether the mapper can call {@see sendAll()} without blocking.
	 * This implementation checks whether each conduit entrance has its transmitter set.
	 */
	protected boolean readyForSend() {
		for (ConduitEntranceController ec : entrances.values()) {
			if (!ec.hasTransmitter()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Whether the mapper can call {@see receiveAll()} without blocking.
	 * This implementation checks whether each conduit exit returns true for {@see muscle.core.ConduitExit.ready()}.
	 */
	protected boolean readyForReceive() {
		for (ConduitExitController ec : this.exits.values()) {
			if (!ec.getExit().ready()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Get the scale of the Mapper.
	 * Since normally a mapper does not have a scale, this implementation returns null.
	 */
	@Override
	public Scale getScale() {
		return null;
	}

	/** Initialize the Mapper. */
	protected void init() {}
	
	@SuppressWarnings("fallthrough")
	public void step() {
		switch (step) {
			case INIT:
				this.operationsAllowed = NONE;
				this.init();
				// no break
			case CONTINUE:
				this.operationsAllowed = RECV;
				if (this.continueComputation()) {
					this.step = Step.RECEIVE;
				} else {
					this.step = Step.END;
				}
				break;
			case RECEIVE:
				this.receiveAll();
				this.step = Step.SEND;
				break;
			case SEND:
				this.operationsAllowed = SEND;
				this.sendAll();
				this.step = Step.CONTINUE;
				break;
			default:
				// Do nothing
		}
	}
	
	@Override
	public boolean readyForStep() {
		switch (step) {
			case CONTINUE:
			case INIT:
				return readyForContinue();
			case SEND:
				return readyForSend();
			case RECEIVE:
				return readyForReceive();
			default:
				return true;
		}
	}
	
	@Override
	public boolean steppingFinished() {
		return this.step == Step.END;
	}
	
	/**
	 * Whether the mapper has the step function implemented correctly.
	 * Returns true.
	 */
	@Override
	public boolean canStep() {
		return true;
	}
}

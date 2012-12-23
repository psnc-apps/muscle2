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
	 * Read from all conduits.
	 */
	protected abstract void receiveAll();

	/**
	 * Write to all conduits.
	 */
	protected abstract void sendAll();

	/**
	 * Whether the mapper should continue computation.
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
	
	protected boolean readyForContinue() {
		for (ConduitExitController ec : this.exits.values()) {
			if (!ec.getExit().ready()) {
				return false;
			}
		}
		return true;
	}
	
	protected boolean readyForSend() {
		for (ConduitEntranceController ec : entrances.values()) {
			if (!ec.hasTransmitter()) {
				return false;
			}
		}
		return true;
	}

	protected boolean readyForReceive() {
		for (ConduitExitController ec : this.exits.values()) {
			if (!ec.getExit().ready()) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public Scale getScale() {
		return null;
	}

	/** Initialize the Mapper. */
	protected void init() {}
	
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
	
	public boolean steppingFinished() {
		return this.step == Step.END;
	}
	
	public boolean canStep() {
		return true;
	}
}

/*
 * 
 */

package muscle.core.kernel;

import java.util.logging.Logger;
import muscle.core.ConduitExitController;
import muscle.core.Scale;

/**
 * A mapper instance. Newly created mappers should inherit from this class.
 * @author Joris Borgdorff
 */
public abstract class Mapper extends Instance {
	private final static Logger logger = Logger.getLogger(Mapper.class.getName());
	
	/**
	 * Executes the standard mapper workflow.
	 * While continue computation: read all, perform mapping and write all.
	 */
	@Override
	protected final void execute() {
		this.operationsAllowed = NONE;
		while (continueComputation()) {
			this.operationsAllowed = RECV;
			receiveAll();
			this.operationsAllowed = SEND;
			sendAll();
			this.operationsAllowed = NONE;
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
	
	@Override
	public Scale getScale() {
		return null;
	}
}

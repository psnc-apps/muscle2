/*
 * 
 */

package muscle.core.kernel;

import muscle.core.Scale;

/**
 * A mapper instance. Newly created mappers should inherit from this class.
 * @author Joris Borgdorff
 */
public abstract class Mapper extends Instance {
	@Override
	protected void execute() {
		while (true) {
			this.operationsAllowed = RECV;
			if (!readAll()) break;
			this.operationsAllowed = SEND;
			if (!writeAll()) break;
		}
	}
	
	/**
	 * Read from all conduits.
	 * @return whether the mapper should quit after reading.
	 */
	protected abstract boolean readAll();

	/**
	 * Write to all conduits.
	 * @return whether the mapper should quit after writing.
	 */
	protected abstract boolean writeAll();

	@Override
	public Scale getScale() {
		return null;
	}
}

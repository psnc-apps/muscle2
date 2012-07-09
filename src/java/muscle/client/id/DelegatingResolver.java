package muscle.client.id;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.id.IDType;

/**
 * Adds autoquit functionality to the simple delegating resolver.
 * @author Joris Borgdorff
 */
public class DelegatingResolver extends SimpleDelegatingResolver {
	private final Set<String> stillAlive;
	private final static Logger logger = Logger.getLogger(DelegatingResolver.class.getName());
	
	public DelegatingResolver(IDManipulator newDelegate, Set<String> stillAlive) {
		super(newDelegate);
		this.stillAlive = stillAlive;
		if (this.stillAlive == null) {
			logger.fine("MUSCLE will not autoquit.");
		} else {
			logger.log(Level.INFO, "MUSCLE will autoquit once submodel instances {0} have finished.", stillAlive);
		}
	}
	
	/**
	 * Removes the identifier with given name and type from the resolver. If this
	 * is the last id that was alive, and autoquit is active, it kills the platform.
	 */
	public synchronized void removeIdentifier(String name, IDType type) {
		super.removeIdentifier(name, type);

		// Remove from stillAlive and if succeeded, try to quit MUSCLE
		if (type == IDType.instance && stillAlive != null && this.stillAlive.remove(name)) {
			if (this.stillAlive.isEmpty()) {
				logger.info("All submodel instances have finished; quitting MUSCLE.");
				delegate.deletePlatform();
			}
			else {
				logger.log(Level.INFO, "Waiting on submodel instance(s) {0} to quit MUSCLE.", stillAlive);
			}
		}
	}
}

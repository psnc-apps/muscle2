/*
 * 
 */
package muscle.core;

import muscle.core.ident.Location;
import muscle.core.ident.Identifier;

/**
 *
 * @author Joris Borgdorff
 */
public class JadeLocator {
	protected JadeLocator() {}
	
	public Location getLocation(Identifier id) {
		return id.getLocation();
	}
	
	private static JadeLocator instance = new JadeLocator();
	public static JadeLocator getInstance() {
		return instance;
	}
}

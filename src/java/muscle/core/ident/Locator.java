/*
 * 
 */
package muscle.core.ident;

import muscle.core.ident.Location;
import muscle.core.ident.Identifier;

/**
 *
 * @author jborgdo1
 */
public interface Locator {
	public Location getLocation(Identifier id);
}

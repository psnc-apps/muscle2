/*
 * 
 */
package muscle.core.ident;

import jade.core.Location;

/**
 *
 * @author jborgdo1
 */
public interface Identifier {
	public IdType getType();
	public Location getLocation();
}

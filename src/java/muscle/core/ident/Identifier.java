/*
 * 
 */
package muscle.core.ident;

/**
 * @author jborgdo1
 */
public interface Identifier extends Comparable<Identifier> {
	public String getName();
	public IDType getType();
	public Location getLocation();
}

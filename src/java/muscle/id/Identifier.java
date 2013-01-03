/*
 * 
 */
package muscle.id;

import java.io.Serializable;

/**
 * @author jborgdo1
 */
public interface Identifier extends Comparable<Identifier>, Serializable {
	public String getName();
	public IDType getType();
	public String getFullName();
	public boolean isResolved();
	public boolean isAvailable();
	public void setAvailable(boolean available);
	public boolean canBeResolved();
	public void willNotBeResolved();
	public void unResolve();
	public void resolveLike(Identifier other);
	public Location getLocation();
	public boolean identifies(Identifiable ident);
}

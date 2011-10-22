/*
 * 
 */
package muscle.core.ident;

/**
 *
 * @author Joris Borgdorff
 */
public class InstanceID extends AbstractID {
	protected Location loc;
	
	public InstanceID(String name) {
		this(name, null);
	}
	
	public InstanceID(String name, Location loc) {
		super(name);
		this.loc = loc;
	}

	public synchronized void resolve(Location loc) {
		this.loc = loc;
	}
	
	public IDType getType() {
		return IDType.instance;
	}
	
	public synchronized boolean isResolved() {
		return this.loc != null;
	}
	
	public synchronized void unResolve() {
		this.loc = null;
	}

	public Location getLocation() {
		return this.loc;
	}
}

/*
 * 
 */
package muscle.id;

/**
 *
 * @author Joris Borgdorff
 */
public class InstanceID extends AbstractID {
	private static final long serialVersionUID = 1L;
	protected Location loc;
	private boolean canBeResolved;
	
	public InstanceID(String name) {
		this(name, null);
		this.canBeResolved = true;
	}
	
	public InstanceID(String name, Location loc) {
		super(name);
		this.loc = loc;
	}

	public synchronized void resolve(Location loc) {
		if (!this.canBeResolved()) {
			throw new IllegalStateException("Can not resolve unresolvable identifier");
		}
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
	
	public void resolveLike(Identifier id) {
		this.resolve(id.getLocation());
	}

	@Override
	public synchronized boolean canBeResolved() {
		return this.canBeResolved;
	}

	@Override
	public synchronized void willNotBeResolved() {
		this.canBeResolved = false;
		this.unResolve();
	}
}

/*
 * 
 */
package muscle.core.ident;

import jade.core.AID;

/**
 *
 * @author Joris Borgdorff
 */
public class JadeAgentID extends InstanceID implements JadeIdentifier {
	protected AID id;
	
	public JadeAgentID(String name) {
		this(name, null, null);
	}
	
	public JadeAgentID(String name, AID aid, Location loc) {
		super(name, loc);
		this.id = aid;
	}

	public synchronized void resolve(AID aid, Location loc) {
		this.id = aid;
		super.resolve(loc);
	}
	
	public AID getAID() {
		return id;
	}
	
	public synchronized void unResolve() {
		this.id = null;
		super.unResolve();
	}
	
	public void resolveLike(Identifier id) {
		if (!(id instanceof JadeIdentifier)) {
			throw new IllegalArgumentException("Can only resolve a JadeIdentifier with another JadeIdentifier.");
		}
		this.resolve(((JadeIdentifier)id).getAID(), id.getLocation());
	}
}

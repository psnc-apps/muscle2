/*
 * 
 */
package muscle.core.ident;

import jade.core.AID;

/**
 *
 * @author Joris Borgdorff
 */
<<<<<<< HEAD
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
	
=======
public class JadeAgentID extends AbstractID implements Identifier {
	protected AID id;
	
	public JadeAgentID(AID id) {
		super(id.getName());
		this.id = id;
	}
	
	public JadeAgentID(String name, Identifier id) {
		super(name);
		if (!(id instanceof JadeAgentID)) {
			throw new IllegalArgumentException("Only JadeAgentId is accepted when specifying a name");
		}
		this.id = ((JadeAgentID)id).getAID();
	}

>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0
	public AID getAID() {
		return id;
	}
	
<<<<<<< HEAD
	public synchronized void unResolve() {
		this.id = null;
		super.unResolve();
=======
	public IDType getType() {
		return IDType.instance;
	}

	public Location getLocation() {
		return null;
	}

	public int compareTo(Identifier t) {
		if (t instanceof JadeAgentID) {
			return this.id.compareTo(((JadeAgentID)t).id);
		}
		else {
			return super.compareTo(t);
		}
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0
	}
}

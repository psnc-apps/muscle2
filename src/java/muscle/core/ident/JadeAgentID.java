/*
 * 
 */
package muscle.core.ident;

import jade.core.AID;

/**
 *
 * @author Joris Borgdorff
 */
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

	public AID getAID() {
		return id;
	}
	
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
	}
}

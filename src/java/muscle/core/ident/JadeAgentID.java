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
		this(id.getName(), id);
	}
	
	public JadeAgentID(String name, AID id) {
		super(name);
		this.id = id;
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
}

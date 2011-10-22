/*
 * 
 */
package muscle.core.ident;

import jade.core.AID;

/**
 *
 * @author Joris Borgdorff
 */
public class JadeAgentID extends AbstractID implements JadeIdentifier {
	protected AID id;
	
	public JadeAgentID(String name) {
		this(name, null);
	}
	
	public JadeAgentID(String name, AID aid) {
		super(name);
		this.id = aid;
	}

	public void setAID(AID aid) {
		this.id = aid;
	}
	
	public AID getAID() {
		return id;
	}
	
	public IDType getType() {
		return IDType.instance;
	}
	
	public boolean isResolved() {
		return this.id != null;
	}
}

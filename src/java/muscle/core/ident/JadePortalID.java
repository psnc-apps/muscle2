/*
 * 
 */
package muscle.core.ident;

import jade.core.AID;

/**
 *
 * @author Joris Borgdorff
 */
public class JadePortalID extends PortalID<JadeIdentifier> implements JadeIdentifier {
	public JadePortalID(String name, JadeIdentifier ownerID) {
		super(name, ownerID);
	}
	
	public JadePortalID(String name, String ownerName) {
		this(name, new JadeAgentID(ownerName));
	}

	public JadePortalID(String name, String ownerName, AID aid, Location loc) {
		super(name, new JadeAgentID(ownerName, aid, loc));
	}
	
	public AID getAID() {
		return ownerID.getAID();
	}
	
	public void resolve(AID aid, Location loc) {
		this.ownerID.resolve(aid, loc);
	}
}

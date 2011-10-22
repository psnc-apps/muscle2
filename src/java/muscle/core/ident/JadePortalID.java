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

	public JadePortalID(String name, String ownerName, AID aid) {
		super(name, new JadeAgentID(ownerName, aid));
	}
	
	public AID getAID() {
		return ownerID.getAID();
	}
}

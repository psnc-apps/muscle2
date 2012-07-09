/*
 * 
 */
package muscle.client.id;

import jade.core.AID;
import muscle.id.Identifier;
import muscle.id.Location;
import muscle.id.PortalID;

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
	
	public void resolveLike(Identifier id) {
		if (!(id instanceof JadeIdentifier)) {
			throw new IllegalArgumentException("Can only resolve a JadeIdentifier with another JadeIdentifier.");
		}
		this.resolve(((JadeIdentifier)id).getAID(), id.getLocation());
	}
}

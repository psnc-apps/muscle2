/*
 * 
 */
package muscle.client.id;

import jade.core.AID;
import muscle.id.Identifier;
import muscle.id.Location;

/**
 * @author Joris Borgdorff
 */
public interface JadeIdentifier extends Identifier {
	public AID getAID();
	public void resolve(AID aid, Location loc);
}

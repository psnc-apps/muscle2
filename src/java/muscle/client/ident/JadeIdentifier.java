/*
 * 
 */
package muscle.client.ident;

import jade.core.AID;
import muscle.core.ident.Identifier;
import muscle.core.ident.Location;

/**
 * @author Joris Borgdorff
 */
public interface JadeIdentifier extends Identifier {
	public AID getAID();
	public void resolve(AID aid, Location loc);
}

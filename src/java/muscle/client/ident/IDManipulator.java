/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.client.ident;

import muscle.core.ident.IDType;
import muscle.core.ident.Identifier;
import muscle.core.ident.Location;

/**
 *
 * @author Joris Borgdorff
 */
public interface IDManipulator {
	public boolean propagate(Identifier id);
	public boolean register(Identifier id, Location loc);
	public void search(Identifier id);
	public Identifier create(String name, IDType idType);
	public Location getLocation();
	public boolean delete(Identifier id);
	public void deletePlatform();
}

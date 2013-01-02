/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.client.id;

import muscle.id.IDType;
import muscle.id.Identifier;
import muscle.id.Location;

/**
 *
 * @author Joris Borgdorff
 */
public interface IDManipulator {
	public void propagate(Identifier id);
	public boolean register(Identifier id, Location loc);
	public void search(Identifier id);
	public boolean willActivate(Identifier id);
	public Identifier create(String name, IDType idType);
	public Location getLocation();
	public Location getManagerLocation();
	public void delete(Identifier id);
	public void deletePlatform();
}

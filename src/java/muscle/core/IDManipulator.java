/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.core;

import muscle.core.ident.IDType;
import muscle.core.ident.Identifier;
import muscle.core.ident.Location;

/**
 *
 * @author Joris Borgdorff
 */
public interface IDManipulator {
	public void propagate(Identifier id, Location loc);
	public void search(Identifier id);
	public void resolve(Identifier id) throws InterruptedException ;
	public Identifier create(String name, IDType iDType);
	public Location getLocation();
	public void delete(Identifier id);
	public void deletePlatform();
}

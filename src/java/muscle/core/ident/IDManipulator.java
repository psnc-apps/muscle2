/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.core.ident;

/**
 *
 * @author Joris Borgdorff
 */
public interface IDManipulator {
	public void propagate(Identifier id, Location loc);
	public void search(Identifier id);
	public Identifier create(String name, IDType iDType);
	public Location getLocation();
	public void delete(Identifier id);
	public void deletePlatform();
}

/*
 * 
 */
package muscle.core.ident;

/**
 *
 * @author Joris Borgdorff
 */
public interface Identifiable<T extends Identifier> {
	public T getIdentifier();
}

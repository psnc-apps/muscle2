/*
 * 
 */
package muscle.id;

/**
 * Keeps a resolver.
 * 
 * @author Joris Borgdorff
 */
public interface ResolverFactory {
	/**
	 * Get a resolver.
	 * 
	 * @throws InterruptedException when the resolver was not yet created, and the waiting for it is interrupted.
	 */
	public Resolver getResolver() throws InterruptedException;
}

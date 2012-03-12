/*
 * 
 */
package muscle.core.ident;

/**
 *
 * @author Joris Borgdorff
 */
public interface ResolverFactory {
	public Resolver getResolver() throws InterruptedException;
}

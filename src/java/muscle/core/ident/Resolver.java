package muscle.core.ident;

import muscle.core.kernel.InstanceController;

/**
 *
 * @author jborgdo1
 */
public interface Resolver {
	/** Whether given ID is local to the current execution. */
	public boolean isLocal(Identifier id);
	/** Get an identifier non-blocking, unresolved. */
	public Identifier getIdentifier(String name, IDType type);
	/** removes blocks */
	public void addResolvedIdentifier(Identifier id);
	/** removes blocks */
	public void removeIdentifier(String name, IDType type);
	/** blocking */
	public void resolveIdentifier(Identifier id) throws InterruptedException;
	
	/** At current location */
	public boolean register(InstanceController id);
	/** At current location */
	public void makeAvailable(InstanceController id);
	/** At current location */
	public boolean deregister(InstanceController id);
}

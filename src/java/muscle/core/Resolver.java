package muscle.core;

import muscle.core.ident.IDType;
import muscle.core.ident.Identifier;
import muscle.core.kernel.InstanceController;

/**
 *
 * @author jborgdo1
 */
public interface Resolver {
	/** non-blocking */
	public boolean isLocal(Identifier id);
	/** non-blocking */
	public void search(String name, IDType type);
	/** blocking */
	public Identifier getResolvedIdentifier(String name, IDType type) throws InterruptedException;
	/** non-blocking, unresolved */
	public Identifier getIdentifier(String name, IDType type);
	/** removes blocks */
	public void addIdentifier(Identifier id);
	/** removes blocks */
	public void removeIdentifier(String name, IDType type);
	/** blocking */
	public void resolveIdentifier(Identifier id) throws InterruptedException;
	/** At current location */
	public void register(InstanceController id);
	/** At current location */
	public void deregister(InstanceController id);
}

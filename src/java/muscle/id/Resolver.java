package muscle.id;

import muscle.core.kernel.InstanceController;
import muscle.util.concurrency.Disposable;

/**
 *
 * @author jborgdo1
 */
public interface Resolver extends Disposable {
	/** Whether given ID is local to the current execution. */
	public boolean isLocal(Identifier id);
	/** Get an identifier non-blocking, unresolved. */
	public Identifier getIdentifier(String name, IDType type);
	/** removes blocks */
	public void addResolvedIdentifier(Identifier id);
	/** removes blocks */
	public void canNotResolveIdentifier(Identifier id);
	/** removes blocks */
	public void removeIdentifier(String name, IDType type);
	/** blocking */
	public boolean resolveIdentifier(Identifier id) throws InterruptedException;
	/** non-blocking */
	public boolean identifierMayActivate(Identifier id);
	
	/** At current location */
	public boolean register(InstanceController id);
	/** At current location */
	public void makeAvailable(InstanceController id);
	/** At current location */
	public boolean deregister(InstanceController id);
}

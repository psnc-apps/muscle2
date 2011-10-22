package muscle.core;

import muscle.core.ident.Location;
import muscle.core.ident.Identifier;
import muscle.core.kernel.InstanceController;

/**
 *
 * @author jborgdo1
 */
public interface Locator {
	public Location getLocation(Identifier id);
	public boolean isLocal(Identifier id);
	/** non-blocking */
	public void searchIdentifier(String name);
	/** blocking */
	public Identifier getIdentifier(String name);
	public void register(InstanceController id);
}

package muscle.core;

import muscle.core.ident.IDType;
import muscle.core.ident.Location;
import muscle.core.ident.Identifier;
import muscle.core.kernel.InstanceController;

/**
 *
 * @author jborgdo1
 */
public interface Locator {
	public Location getLocation(Identifier id);
	public Identifier getIdentifier(String name, IDType type);
	public boolean isLocal(Identifier id);
	public void register(InstanceController id);
}

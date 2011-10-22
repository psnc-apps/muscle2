/*
 * 
 */
package muscle.core;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.AMSService;
import jade.domain.FIPAException;
import java.util.Map;
import muscle.core.ident.IDType;
import muscle.core.ident.Location;
import muscle.core.ident.Identifier;
import muscle.core.ident.JadeAgentID;
import muscle.core.ident.JadeLocation;
import muscle.core.kernel.InstanceController;
import utilities.ArrayMap;
import utilities.SafeThread;

/**
 *
 * @author Joris Borgdorff
 */
public class JadeLocator extends Agent implements Locator {
	private final Map<Identifier,Location> locCache;
	private final Map<String,Identifier> idCache;
	private Location here;
	
	protected JadeLocator() {
		locCache = new ArrayMap<Identifier,Location>();
		idCache = new ArrayMap<String,Identifier>();
	}
	
	@Override
	public void setup() {
		this.here = new JadeLocation(here());
		Boot.getInstance().registerLocator(this);
	}
	
	public synchronized Identifier getIdentifier(String name, IDType type) {
		while (!idCache) {
			
		}
	}
	
	public synchronized Location getLocation(Identifier id) {
		while (!locCache.containsKey(id)) {
			if (!(id instanceof JadeAgentID)) {
				throw new IllegalArgumentException("JadeLocator can only find the locations of JadeAgentIDs.");
			}
			JadeAgentID jid = (JadeAgentID)id;
			new LocateAID().start();
		}
		Location loc = locCache.get(id);
		if (loc == null) {
		}
		return loc;
	}
	
	public synchronized boolean isLocal(Identifier id) {
		return this.locCache.containsKey(id);
	}

	public synchronized void register(InstanceController controller) {
		Identifier id = controller.getIdentifier();
		this.locCache.put(id, here);
		this.idCache.put(, null)
	}
	
	private String makeName(String name, IDType type) {
		return name + "#" + type.name();
	}
	
	private String makeName(Identifier id) {
		return this.makeName(id.getName(), id.getType());
	}
}

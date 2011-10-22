package muscle.core;

import java.util.Map;
import java.util.Set;
import muscle.core.ident.IDType;
import muscle.core.ident.Identifier;
import muscle.core.ident.Location;
import muscle.core.kernel.InstanceController;
import utilities.ArrayMap;
import utilities.ArraySet;

/**
 *
 * @author Joris Borgdorff
 */
public class DelegatingResolver implements Resolver {
	private final IDManipulator delegate;
	private final Map<String,Identifier> idCache;
	private Location here;
	private final Set<String> searchingNow;
	private final Set<String> stillAlive;
	private boolean isDone;
	
	public DelegatingResolver(IDManipulator newDelegate, Set<String> stillAlive) {
		delegate = newDelegate;
		idCache = new ArrayMap<String,Identifier>();
		searchingNow = new ArraySet<String>();
		here = delegate.getLocation();
		this.stillAlive = stillAlive;
		this.isDone = false;
	}
	
	public synchronized Identifier getResolvedIdentifier(String name, IDType type) throws InterruptedException {
		if (type == IDType.port) {
			Identifier id = getIdentifier(name, type);
			this.resolveIdentifier(id);
			return id;
		}
		String fullName = name(name, type);
		if (!idCache.containsKey(fullName)) {
			this.search(name, type);
			while (!isDone && !idCache.containsKey(fullName)) {
				wait();
			}
			if (isDone) throw new InterruptedException("Resolver interrupted");
		}
		return idCache.get(fullName);
	}
	
	public Identifier getIdentifier(String name, IDType type) {
		String fullName = name(name, type);
		
		// Try cache first, not synchronized as making a new id is not expensive
		if (idCache.containsKey(fullName)) {
			return idCache.get(fullName);
		}
		
		// Make a new ID
		return delegate.create(name, type);
	}
	
	public void resolveIdentifier(Identifier id) throws InterruptedException  {
		delegate.resolve(id);
	}
	
	public boolean isLocal(Identifier id) {
		if (here == null) {
			return false;
		}
		return here.equals(id.getLocation());
	}

	public void register(InstanceController controller) {
		Identifier id = controller.getIdentifier();
		this.addIdentifier(id);
		
		delegate.propagate(id, here);
	}
	
	public void deregister(InstanceController controller) {
		System.out.println("Deregistering " + controller);
		Identifier id = controller.getIdentifier();
		this.idCache.remove(name(id.getName(), id.getType()));
		if (stillAlive != null) {
			this.stillAlive.remove(id.getName());
			System.out.println("Waiting on " + stillAlive + " to kill platform.");
		
			if (this.stillAlive.isEmpty()) {
				delegate.deletePlatform();
			}
		}
		
		//Creates a blocking wait in JADE, somehow.
		//delegate.delete(id);
	}


	public void search(String name, IDType type) {
		String fullName = name(name, type);
		synchronized(this) {
			if (searchingNow.contains(fullName)) {
				return;
			}
			else {
				searchingNow.add(fullName);
			}
		}
		delegate.search(getIdentifier(name, type));
	}
	
	public synchronized void addIdentifier(Identifier identifier) {
		String fullName = name(identifier.getName(), identifier.getType());
		idCache.put(fullName, identifier);
		searchingNow.remove(fullName);
		notifyAll();
	}
	
	private static String name(String name, IDType type) {
		return name + "#" + type.name();
	}
	
	public synchronized void dispose() {
		this.isDone = true;
		this.notifyAll();
	}
}

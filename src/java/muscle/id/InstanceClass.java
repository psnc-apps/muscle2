/*
 * 
 */

package muscle.id;

/**
 *
 * @author Joris Borgdorff
 */
public class InstanceClass implements Identifiable {
	private final Class<?> clazz;
	private final String name;
	private Identifier id;
	
	public InstanceClass(String name, Class<?> clazz) {
		if (name == null) {
			throw new IllegalArgumentException("Instance name may not be null");
		}
		if (clazz == null) {
			throw new IllegalArgumentException("Instance class may not be null");
		}
		this.name = name;
		this.clazz = clazz;
	}
	
	public void setIdentifier(Identifier id) {
		this.id = id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Class<?> getInstanceClass() {
		return clazz;
	}

	@Override
	public Identifier getIdentifier() {
		return id;
	}
	
	public String toString() {
		return "Description[" + this.name + "]";
	}
}

/*
 * 
 */

package muscle.id;

/**
 *
 * @author Joris Borgdorff
 */
public class InstanceClass {
	private final Class<?> clazz;
	private final String name;
	
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
	
	public String getName() {
		return this.name;
	}
	
	public Class<?> getInstanceClass() {
		return clazz;
	}
}

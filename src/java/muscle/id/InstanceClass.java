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

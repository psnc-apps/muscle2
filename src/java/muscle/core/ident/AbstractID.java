/*
 * 
 */
package muscle.core.ident;

/**
 *
 * @author Joris Borgdorff
 */
public abstract class AbstractID  implements Identifier {
	protected final String name;
	
	public AbstractID(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public int compareTo(Identifier t) {
		if (getType().equals(t.getType())) {
			return this.getName().compareTo(t.getName());
		}
		else {
			return this.getType().compareTo(t.getType());
		}
	}	
}

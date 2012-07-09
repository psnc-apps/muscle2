/*
 * 
 */
package muscle.id;

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

	public final int compareTo(Identifier t) {
		if (getType().equals(t.getType())) {
			return this.getName().compareTo(t.getName());
		}
		else {
			return this.getType().compareTo(t.getType());
		}
	}
	
	public boolean identifies(Identifiable ident) {
		return this.equals(ident.getIdentifier());
	}
	
	public String toString() {
		return getName();
	}
	
	public final boolean equals(Object other) {
		if (other == null || !(other instanceof AbstractID)) return false;
		
		AbstractID aid = (AbstractID)other;
		return this.getName().equals(aid.getName()) && this.getType().equals(aid.getType());
	}
	
	public int hashCode() {
		int hash = 7;
		hash = 47 * hash + (this.name != null ? this.name.hashCode() : 0);
		hash = 47 * hash + (this.getType() != null ? this.getType().hashCode() : 0);
		return hash;
	}
}

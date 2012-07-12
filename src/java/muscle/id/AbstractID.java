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
	private int hashCode = -1;
	
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
		if (other == null || !(other instanceof Identifier)) return false;
		
		Identifier iid = (Identifier)other;
		return hashCode() == iid.hashCode() && this.getName().equals(iid.getName()) && this.getType().equals(iid.getType());
	}
	
	public int hashCode() {
		if (hashCode == -1) {
			hashCode = 7;
			hashCode = 47 * hashCode + (this.getName() != null ? this.getName().hashCode() : 0);
			hashCode = 47 * hashCode + (this.getType() != null ? this.getType().hashCode() : 0);
			if (hashCode == -1) hashCode = -2;
		}
		return hashCode;
	}
}

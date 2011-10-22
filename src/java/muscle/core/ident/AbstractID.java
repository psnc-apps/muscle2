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

<<<<<<< HEAD
	public final int compareTo(Identifier t) {
=======
	public int compareTo(Identifier t) {
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0
		if (getType().equals(t.getType())) {
			return this.getName().compareTo(t.getName());
		}
		else {
			return this.getType().compareTo(t.getType());
		}
<<<<<<< HEAD
	}
	
	public boolean identifies(Identifiable ident) {
		return this.equals(ident.getIdentifier());
	}
	
	public String toString() {
		return getName() + "[" + getType() + "]";
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
=======
	}	
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0
}

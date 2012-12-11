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
	private final String fullname;
	private int hashCode = -1;
	
	public AbstractID(String name) {
		if (this.name == null) {
			throw new NullPointerException("Id name may not be null");
		}
		this.name = name;
		this.fullname = getFullName(name, getType());
		this.hashCode = 47*7+this.fullname.hashCode();
	}

	public String getName() {
		return name;
	}
	
	public String getFullName() {
		return this.fullname;
	}
	
	public static String getFullName(String name, IDType type) {
		return name + "#" + type.name();
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
		return hashCode() == iid.hashCode() && this.fullname.equals(iid.getFullName()) && this.getType().equals(iid.getType());
	}
	
	public int hashCode() {
		return hashCode;
	}
}

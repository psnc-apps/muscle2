/*
 * 
 */
package muscle.core.ident;

/**
 *
 * @author Joris Borgdorff
 */
public class JadeLocation implements jade.core.Location, Location {
	private final jade.core.Location jadeLocation;

	public JadeLocation(jade.core.Location loc) {
		this.jadeLocation = loc;
	}
	
	public String getID() {
		return jadeLocation.getID();
	}

	public String getName() {
		return jadeLocation.getName();
	}

	public String getProtocol() {
		return jadeLocation.getProtocol();
	}

	public String getAddress() {
		return jadeLocation.getAddress();
	}
	
	public boolean equals(Object o) {
		if (o == null || !o.getClass().equals(this.getClass())) return false;
		return getID().equals(((JadeLocation)o).getID());
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 47 * hash + this.getID().hashCode();
		return hash;
	}
}

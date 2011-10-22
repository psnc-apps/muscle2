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
}

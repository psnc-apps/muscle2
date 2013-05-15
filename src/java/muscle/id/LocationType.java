/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.id;

/**
 *
 * @author Joris Borgdorff
 */
public enum LocationType {
	TCP_LOCATION, TCP_DIR_LOCATION;
	
	public int intValue() {
		return ordinal();
	}
	public static LocationType valueOf(int n) {
		switch (n) {
			case 0:
				return TCP_LOCATION;
			default:
				return TCP_DIR_LOCATION;
		}
	}
}

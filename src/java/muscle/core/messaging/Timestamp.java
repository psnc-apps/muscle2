/*
 * 
 */
package muscle.core.messaging;

/**
 *
 * @author Joris Borgdorff
 */
public class Timestamp implements Comparable<Timestamp> {
	private final double t;

	public Timestamp(double t) {
		this.t = t;
	}
	
	public int compareTo(Timestamp ts) {
		return Double.compare(t, ts.t);
	}
	
	public boolean equals(Object o) {
		if (o == null || !o.getClass().equals(this.getClass())) return false;
		return ((Timestamp)o).t == this.t;
	}

	public int hashCode() {
		int hash = 5;
		hash = 67 * hash + (int) (Double.doubleToLongBits(this.t) ^ (Double.doubleToLongBits(this.t) >>> 32));
		return hash;
	}
}

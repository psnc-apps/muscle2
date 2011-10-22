/*
 * 
 */
package muscle.core.messaging;

/**
 *
 * @author Joris Borgdorff
 */
<<<<<<< HEAD
public class Timestamp extends AbstractTime {
	public Timestamp(double t) {
		super(t);
	}
	
	public String toString() {
		return "t=" + this.t + " s";
	}
	
	public Timestamp subtract(Duration other) {
		return new Timestamp(t - other.t);
	}
	public Timestamp add(Duration other) {
		return new Timestamp(t + other.t);
	}
	public Timestamp multiply(double factor) {
		return new Timestamp(t * factor);
	}
	public Timestamp divide(double factor) {
		return new Timestamp(t / factor);
=======
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
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0
	}
}

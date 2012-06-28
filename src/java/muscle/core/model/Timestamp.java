/*
 * 
 */
package muscle.core.model;

/**
 *
 * @author Joris Borgdorff
 */
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
	}
	
	public double doubleValue() {
		return t;
	}
}

/*
 * 
 */
package muscle.core.messaging;

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
	public Timestamp multiply(Duration other) {
		return new Timestamp(t * other.t);
	}
	public Timestamp divide(Duration other) {
		return new Timestamp(t / other.t);
	}
	public Timestamp add(Duration other) {
		return new Timestamp(t + other.t);
	}
}

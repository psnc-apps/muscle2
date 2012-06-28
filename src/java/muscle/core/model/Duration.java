package muscle.core.model;

/**
 *
 * @author Joris Borgdorff
 */
public class Duration extends AbstractTime {
	public Duration(double t) {
		super(t);
	}
	
	public String toString() {
		return "delta=" + this.t + " s";
	}
	
	public Duration multiply(double factor) {
		return new Duration(t * factor);
	}
	public Duration divide(double factor) {
		return new Duration(t / factor);
	}
}

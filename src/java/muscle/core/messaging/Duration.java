package muscle.core.messaging;

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
}

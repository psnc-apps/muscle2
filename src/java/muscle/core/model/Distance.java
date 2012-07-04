package muscle.core.model;

import eu.mapperproject.jmml.util.numerical.SIUnit;
import eu.mapperproject.jmml.util.numerical.ScaleFactor;
import eu.mapperproject.jmml.util.numerical.ScaleFactor.Dimension;

/**
 *
 * @author Joris Borgdorff
 */
public class Distance extends SIUnit {
	public Distance(double t) {
		this(t, Dimension.TIME);
	}

	public Distance(double t, Dimension dim) {
		this(t, ScaleFactor.SI.withDimension(dim));
	}
	
	public Distance(double t, Dimension dim, String dimName) {
		this(t, ScaleFactor.SI.withDimension(dim, dimName));
	}
	
	private Distance(double t, ScaleFactor sc) {
		super(t, sc);
	}
	
	public String toString() {
		return "delta=" + doubleValue() + " s";
	}
	
	public Distance mult(double factor) {
		return new Distance(value * factor, scale);
	}
	public Distance div(double factor) {
		return new Distance(value / factor, scale);
	}
}

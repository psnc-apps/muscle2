/*
 * 
 */
package muscle.core.model;

import eu.mapperproject.jmml.util.numerical.SIUnit;
import eu.mapperproject.jmml.util.numerical.ScaleFactor;
import eu.mapperproject.jmml.util.numerical.ScaleFactor.Dimension;

/**
 *
 * @author Joris Borgdorff
 */
public class Timestamp extends SIUnit {
	public Timestamp(double t) {
		this(t, ScaleFactor.SECOND);
	}
	
	private Timestamp(double t, ScaleFactor sc) {
		super(t, sc);
	}

	private Timestamp(SIUnit unit) {
		super(unit);
	}
	
	public String toString() {
		return "t=" + doubleValue() + " s";
	}
	
	public Timestamp subtract(Distance other) {
		if (other.getDimension() != Dimension.TIME) {
			throw new IllegalArgumentException("May only subtract time from a timestamp");
		}
		return new Timestamp(super.sub(other));
	}
	public Timestamp add(Distance other) {
		if (other.getDimension() != Dimension.TIME) {
			throw new IllegalArgumentException("May only add time to a timestamp");
		}
		return new Timestamp(super.add(other));
	}
	public Timestamp multiply(double factor) {
		return new Timestamp(value * factor, scale);
	}
	public Timestamp divide(double factor) {
		return new Timestamp(value / factor, scale);
	}
}

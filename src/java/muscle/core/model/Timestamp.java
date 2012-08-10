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
	public final static Timestamp ZERO = new Timestamp(0);
	
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
	public static Timestamp max(Timestamp t1, Timestamp t2) {
		return t1.compareTo(t2) > 0 ? t1 : t2;
	}
	public static Timestamp min(Timestamp t1, Timestamp t2) {
		return t1.compareTo(t2) < 0 ? t1 : t2;
	}
	
	public static Timestamp valueOf(String s) {
		SIUnit si = SIUnit.valueOf(s);
		return new Timestamp(si);
	}
}

/*
* Copyright 2008, 2009 Complex Automata Simulation Technique (COAST) consortium
* Copyright 2010-2013 Multiscale Applications on European e-Infrastructures (MAPPER) project
*
* GNU Lesser General Public License
* 
* This file is part of MUSCLE (Multiscale Coupling Library and Environment).
* 
* MUSCLE is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* MUSCLE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public License
* along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/
/*
 * 
 */
package muscle.core.model;

import eu.mapperproject.jmml.util.numerical.SIUnit;
import eu.mapperproject.jmml.util.numerical.ScaleFactor;
import eu.mapperproject.jmml.util.numerical.ScaleFactor.Dimension;

/**
 * A measure of time.
 * 
 * It extends the SIUnit class, which means it is a double at a certain timescale. This way, no precision is lost
 * when referring to picoseconds or peta-years. It also allows it to be parsed as a string. The default scale is
 * seconds.
 * 
 * @author Joris Borgdorff
 */
public class Timestamp extends SIUnit {
	public final static Timestamp ZERO = new Timestamp(0);
	private static final long serialVersionUID = 1L;
	
	public Timestamp(double t) {
		this(t, ScaleFactor.SECOND);
	}
	
	Timestamp(double t, ScaleFactor sc) {
		super(t, sc);
	}

	Timestamp(SIUnit unit) {
		super(unit);
	}
	
	public String toString() {
		return "t=" + doubleValue() + " s";
	}
	
	public Timestamp subtract(Distance other) {
		if (other.getDimension() != Dimension.TIME) {
			throw new IllegalArgumentException("May only subtract time from a timestamp");
		}
		return new Timestamp(sub(other));
	}
	public Distance distance(Timestamp other) {
		if (other.getDimension() != Dimension.TIME) {
			throw new IllegalArgumentException("May only subtract time from a timestamp");
		}
		boolean isLarger = this.compareTo(other) >= 0;
		if (isLarger) {
			return new Distance(sub(other));
		} else {
			return new Distance(other.sub(this));
		}
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
	
	/**
	 * Create a new Timestamp given a String containing a time.
	 * Valid formats include '1 s', '3.1e-4 ms', '1.3 hr', '2 years'
	 */
	public static Timestamp valueOf(String s) {
		return new Timestamp(SIUnit.valueOf(s));
	}
}

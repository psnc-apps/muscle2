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
package muscle.core.model;

import eu.mapperproject.jmml.util.numerical.SIUnit;
import eu.mapperproject.jmml.util.numerical.ScaleFactor;
import eu.mapperproject.jmml.util.numerical.ScaleFactor.Dimension;

/**
 * A measure of distance.
 * 
 * It extends the SIUnit class, which means it is a double at a certain scale. This way, no precision is lost
 * when referring to picoseconds or peta-meters. It also allows it to be parsed as a string. The default scale is
 * seconds.
 * 
 * @author Joris Borgdorff
 */
public class Distance extends SIUnit {
	public final static Distance ZERO = new Distance(0);
	public final static Distance ONE = new Distance(1);
	private static final long serialVersionUID = 1L;
	
	public Distance(double t) {
		this(t, Dimension.TIME);
	}

	public Distance(double t, Dimension dim) {
		this(t, ScaleFactor.SI.withDimension(dim));
	}
	
	public Distance(double t, Dimension dim, String dimName) {
		this(t, ScaleFactor.SI.withDimension(dim, dimName));
	}
	
	Distance(double t, ScaleFactor sc) {
		super(t, sc);
	}
	
	Distance(SIUnit unit) {
		super(unit);
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
	
	/**
	 * Create a new Distance given a String containing a time.
	 * Valid formats include '1 m', '3.1e-4 ms', '1.3 hr', '2 years'
	 */
	public static Distance valueOf(String siunit) {
		return new Distance(SIUnit.valueOf(siunit));
	}
	
	public Distance withDimension(Dimension dim) {
		return new Distance(value, scale.withDimension(dim));
	}
}

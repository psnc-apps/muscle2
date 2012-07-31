/*
Copyright 2008,2009 Complex Automata Simulation Technique (COAST) consortium

GNU Lesser General Public License

This file is part of MUSCLE (Multiscale Coupling Library and Environment).

    MUSCLE is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    MUSCLE is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/

package muscle.core;

import eu.mapperproject.jmml.util.numerical.ScaleFactor.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.measure.DecimalMeasure;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import muscle.core.model.Distance;


/**
represents time and spatial scale according to SSM in SI units
@author Jan Hegewald
*/
public class Scale implements java.io.Serializable {

	private final Distance dt; // time step (must be seconds when used without quantity)
	private final Distance omegaT; // total time
	private final List<Distance> dx; // scale(s) in space
	
	public Scale(Distance newDt, Distance newOmegaT, Distance ... newDx) {
		this(newDt, newOmegaT, new ArrayList<Distance>(Arrays.asList(newDx)));
	}
	
	public Scale(Distance newDt, Distance newOmegaT, List<Distance> newDx) {
		this.dt = newDt.withDimension(Dimension.TIME);
		this.omegaT = newOmegaT.withDimension(Dimension.TIME);
		this.dx = newDx;
	}
	
	public Distance getDt() {
		return this.dt;
	}

	public Distance getOmegaT() {
		return this.omegaT;
	}
	
	public Distance getDx(int index) {
		return dx.get(index);
	}
	
	public int getDimensions() {
		return dx.size();
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 97 * hash + (this.dt != null ? this.dt.hashCode() : 0);
		hash = 97 * hash + (this.dx != null ? this.dx.hashCode() : 0);
		return hash;
	}
	
	public boolean equals(Object obj) {
		if(obj != null && obj.getClass().equals(this.getClass())) {
			Scale other = (Scale)obj;
			
			// test number of dimensions in space
			if(getDimensions() != other.getDimensions()) {
				return false;
			}

			// test scale for every dimension in space
			for(int i = 0; i < getDimensions(); i++) {
				if(!getDx(i).equals(other.getDx(i))) {
					return false;
				}
			}
			
			// test timescale
			return dt.equals(other.dt) && omegaT.equals(other.dt);
		}
		return false;
	}
	
	public String toString() {
		StringBuilder text = new StringBuilder(30);
		text.append("dt:").append(dt);
		for(int i = 0; i < dx.size(); i++) {
			text.append("dx").append(i+1).append(':').append(dx.get(i));
		}
		
		return text.toString();
	}
	
	@Deprecated
	public Scale(DecimalMeasure<javax.measure.quantity.Duration> newDt, DecimalMeasure ... newDx) {
		this(new Distance(newDt.doubleValue(SI.SECOND)), newDx);
	}
	
	@Deprecated
	public Scale(Distance newDt, DecimalMeasure ... newDx) {
		dt = newDt.withDimension(Dimension.TIME);
		this.omegaT = null;
		// we will get a nasty compiler warning if our method signature contains a generic vararg like DecimalMeasure<Length> ... newDx
		// this is probably because there are no generic c-style arrays in java
		// so we check the types for each vararr item to be a DecimalMeasure<Length>
		dx = new ArrayList<Distance>();
		for(DecimalMeasure m : newDx) {
			dx.add(new Distance(m.doubleValue(SI.METER), Dimension.SPACE));
		}
	}

	@Deprecated
	public Scale(DecimalMeasure<javax.measure.quantity.Duration> newDt, List<DecimalMeasure<Length>> newDx) {
		dt = new Distance(newDt.doubleValue(SI.SECOND), Dimension.TIME);
		this.omegaT = null;
		dx = new ArrayList<Distance>();
		for(DecimalMeasure m : newDx) {
			dx.add(new Distance(m.doubleValue(SI.METER), Dimension.SPACE));
		}
	}
}


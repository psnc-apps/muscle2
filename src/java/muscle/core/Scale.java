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

import com.thoughtworks.xstream.XStream;

import javax.measure.DecimalMeasure;
import javax.measure.unit.SI;
import javax.measure.quantity.Length;
import java.util.ArrayList;
import javax.measure.quantity.Duration;


/**
represents time and spatial scale according to SSM in SI units
@author Jan Hegewald
*/
public class Scale implements java.io.Serializable {

	private muscle.core.messaging.Duration dt; // time scale (must be seconds when used without quantity)
	private ArrayList<DecimalMeasure<Length>> dx; // scale(s) in space (must be meter when used without quantity)
	

	//
	public Scale(DecimalMeasure<Duration> newDt, DecimalMeasure ... newDx) {

		if(newDx.length < 1)
			throw new IllegalArgumentException("number of dimensions must be greater 0 <"+newDx.length+">");		

		dt = new muscle.core.messaging.Duration(newDt.doubleValue(SI.SECOND));
		

		// we will get a nasty compiler warning if our method signature contains a generic vararg like DecimalMeasure<Length> ... newDx
		// this is probably because there are no generic c-style arrays in java
		// so we check the types for each vararr item to be a DecimalMeasure<Length>
		dx = new ArrayList<DecimalMeasure<Length>>();
		for(DecimalMeasure m : newDx) {
			dx.add((DecimalMeasure<Length>)m);
		}
	}


	//
	public Scale(DecimalMeasure<Duration> newDt, ArrayList<DecimalMeasure<Length>> newDx) {
		if(newDx.size() < 1)
			throw new IllegalArgumentException("number of dimensions must be greater 0 <"+newDx.size()+">");		

		dt = new muscle.core.messaging.Duration(newDt.doubleValue(SI.SECOND));
		dx = newDx;
	}


	//
	public muscle.core.messaging.Duration getDt() {	
		return dt;
	}
	

	public DecimalMeasure<Length> getDx(int index) {
	
		return dx.get(index);
	}

	public ArrayList<DecimalMeasure<Length>> getAllDx() {
	
		return dx;
	}
	
	public int getDimensions() {
	
		return dx.size();
	}


	/**
	compare two scales, returns true if spatial scale is identical
	*/
	public static boolean match(Scale a, Scale b) {
	
		// test number of dimensions in space
		if(a.getDimensions() != b.getDimensions())
			return false;

		// test scale for every dimension in space
		for(int i = 0; i < a.getDimensions(); i++) {
			if( a.getDx(i).doubleValue(SI.METER) != b.getDx(i).doubleValue(SI.METER) )
				return false;
		}
		
		return true;		
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 97 * hash + (this.dt != null ? this.dt.hashCode() : 0);
		hash = 97 * hash + (this.dx != null ? this.dx.hashCode() : 0);
		return hash;
	}
	
	public boolean equals(Object obj) {
		if(obj instanceof Scale) {
			Scale other = (Scale)obj;

			if(!Scale.match(this, other))
				return false;
			
			// test timescale
			return dt.equals(other.dt);
		}
		return super.equals(obj);
	}
	
	public String toString() {
		StringBuilder text = new StringBuilder(30);
		text.append("dt:").append(dt);
		for(int i = 0; i < dx.size(); i++) {
			text.append("dx").append(i+1).append(':').append(dx.get(i));
		}
		
		return text.toString();
	}
	
	
	//
	public static void main (String args[]) {

//		DecimalMeasure<Duration> dt = Measure.valueOf(2, SI.SECOND);
//		DecimalMeasure<Length> dx1 = Measure.valueOf(2, SI.METER);
		DecimalMeasure<Duration> dt = javax.measure.DecimalMeasure.valueOf(new java.math.BigDecimal(2), SI.SECOND);
		DecimalMeasure<Length> dx1 = javax.measure.DecimalMeasure.valueOf(new java.math.BigDecimal(2), SI.METER);

		Scale scale = new Scale(dt, dx1);
		XStream xstream = new XStream();
		System.out.println(xstream.toXML(scale));


//        Unit<Mass> centigram = SI.CENTI(SI.GRAM);
//		Unit<Length> kilometer = SI.KILO(SI.METER);
//		Unit<Length> meter = SI.METER;
 //     Unit<Power> megawatt = SI.MEGA(SI.WATT);

//		System.out.println(kilometer.getConverterTo(meter).convert(2));
//		System.out.println(kilometer.getStandardUnit());
	}

}


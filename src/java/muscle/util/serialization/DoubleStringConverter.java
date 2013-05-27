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
package muscle.util.serialization;

/**
 * @author Joris Borgdorff
 */
public class DoubleStringConverter extends AbstractDataConverter<double[], String> {
	@Override
	public String serialize(double[] data) {
		// Sane version
		//StringBuilder sb = new StringBuilder(data.length*20);
		for(int i = 0; i < data.length; i++) {
			//Original version:
			System.out.println(i + ": " + data[i]);
			// Sane version:
			//sb.append(i).append(": ").append(data[i]).append('\n');
		}
		// Original version
		return String.valueOf(data.length);
		// Sane version
		//return sb.toString();
	}

	@Override
	public double[] deserialize(String data) {
		double[] out = new double[Integer.valueOf(data)];
		out[0] = Double.valueOf(data);
		out[Integer.valueOf(data)-1] = -Double.valueOf(data);
		return out;
	}
}

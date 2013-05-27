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

package muscle.core.conduit.terminal;

import java.io.IOException;
import java.io.Reader;
import java.util.Scanner;
import muscle.core.model.Observation;

/**
 * A FileSource that reads double arrays from a file.
 * Unless extended and overriding getInfix() and isEmpty(), it reads a double array
 * from a file exactly once, and will return empty afterwards.
 * Specifically, it reads from the file, delimited by the "delimiter" property
 * (default: space), until a non-double is encountered.
 * @author Joris Borgdorff
 */
public class DoubleFileSource extends FileSource<double[]> {
	private int iteration;
	private String delimiter;
	
	@Override
	public void beforeExecute() {
		delimiter = hasProperty("delimiter") ? getProperty("delimiter") : " ";
		iteration = 0;
	}
	
	/** Returns true after one read is called once. */
	@Override
	public boolean isEmpty() {
		return iteration > 0;
	}
	
	@Override
	protected Observation<double[]> read(Reader in) throws IOException {
		Scanner sc = new Scanner(in);
		sc.useDelimiter(delimiter);

		int size = 0;
		int capacity = 100;
		double[] value = new double[capacity];
		while (sc.hasNextDouble()) {
			if (capacity == size) {
				capacity = (capacity*3)/2;
				double[] tmp = new double[capacity];
				System.arraycopy(value, 0, tmp, 0, size);
				value = tmp;
			}
			value[size] = sc.nextDouble();
			size++;
		}
		
		iteration++;
		if (size != capacity) {
			double[] tmp = new double[size];
			System.arraycopy(value, 0, tmp, 0, size);
			value = tmp;
		}
		return new Observation<double[]>(value, getSITime(), getSITime(), true);
	}
}

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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import muscle.core.model.Observation;

/**
 * A FileSink that writes double arrays to a file.
 * It enumerates the files, so each observation i is written to
 * file.i.suffix. The doubles are delimited by spaces by default, unless the property
 * "delimeter" is set to some other value.
 * @author Joris Borgdorff
 */
public class DoubleFileSink extends FileSink<double[]> {
	private String delimiter;
	
	@Override
	public void beforeExecute() {
		delimiter = hasProperty("delimiter") ? getProperty("delimiter") : " ";
	}
	
	/**
	 * Writes the doubles to a file with a delimeter between them.
	 */
	@Override
	protected void write(OutputStream out, Observation<double[]> obs) throws IOException {
		Writer writer = new OutputStreamWriter(out);
		double[] data = obs.getData();
		if (data != null) {
			int sz = data.length - 1;
			for (int i = 0; i < sz; i++) {
				writer.write(String.valueOf(data[i]));
				writer.write(delimiter);
			}
			if (sz >= 0) {
				writer.write(String.valueOf(data[sz]));
			}
		}
	}
}

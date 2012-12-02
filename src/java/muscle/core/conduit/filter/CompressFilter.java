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

package muscle.core.conduit.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DeflaterOutputStream;
import muscle.core.model.Observation;
import muscle.util.serialization.ByteJavaObjectConverter;

/**
 * Compress a message
 */
public class CompressFilter extends AbstractFilter<byte[],byte[]> {
	private final static Logger logger = Logger.getLogger(CompressFilter.class.getName());
	private final static boolean finerIsLoggable = logger.isLoggable(Level.FINER);
	
	protected void apply(Observation<byte[]> subject) {
		byte[] bytes = subject.getData();
		if (bytes.length == 0) {
			if (finerIsLoggable) logger.log(Level.FINER, "Not compressing 0 bytes...");
			put(subject.copyWithNewData(new byte[0]));
			return;
		}
		if (finerIsLoggable) logger.log(Level.FINEST, "Compressing {0} bytes...", bytes.length);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DeflaterOutputStream  deflater = new DeflaterOutputStream(out);
		try {
			deflater.write(bytes, 0, bytes.length);
			deflater.close();
		} catch (IOException ex) {
			Logger.getLogger(CompressFilter.class.getName()).log(Level.SEVERE, "Could not compress data in filter", ex);
			throw new RuntimeException(ex);
		}

		byte[] output = out.toByteArray();
		if (finerIsLoggable) {
			logger.log(Level.FINER, "Compression ratio {0}% (higher is better)", 100 - 100*output.length/bytes.length);
		}
		put(subject.copyWithNewData(output));
  	}
}

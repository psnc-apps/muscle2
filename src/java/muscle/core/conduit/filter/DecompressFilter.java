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
import java.util.zip.InflaterOutputStream;
import muscle.core.model.Observation;
import muscle.util.serialization.ByteJavaObjectConverter;

/**
 * Decompress a message.
 */
public class DecompressFilter extends AbstractFilter<byte[],byte[]> {
	private final static Logger logger = Logger.getLogger(DecompressFilter.class.getName());
	private final static boolean finerIsLoggable = logger.isLoggable(Level.FINER);
	protected void apply(Observation<byte[]> subject) {
		byte[] bytes = subject.getData();
		if (bytes.length == 0) {
			if (finerIsLoggable) logger.log(Level.FINER, "Not decompressing 0 bytes...");
			put(subject.copyWithNewData(new byte[0]));
			return;
		}
		if (finerIsLoggable) logger.log(Level.FINEST, "Decompressing {0} bytes...", bytes.length);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		InflaterOutputStream inflater = new InflaterOutputStream(out);
		try {
			inflater.write(bytes, 0, bytes.length);
			inflater.close();
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "Could not decompress data in filter", ex);
			throw new RuntimeException(ex);
		}
		byte[] output = out.toByteArray();
		if (finerIsLoggable) logger.log(Level.FINER, "Compression ratio was {0}% (higher is better)", 100 - 100*bytes.length/output.length);
		put(subject.copyWithNewData(output));
  	}
}

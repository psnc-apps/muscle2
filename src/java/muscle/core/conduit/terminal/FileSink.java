/*
 * 
 */

package muscle.core.conduit.terminal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.model.Observation;
import muscle.util.MiscTool;

/**
 * A sink that writes all data to file.
 * 
 * It is governed by the CxA options "file", "relative", and "
 * @author Joris Borgdorff
 */
public abstract class FileSink<T extends Serializable> extends Sink<T> {
	/**
	 * Provides an infix to the filename to be written to.
	 * @see Terminal.getLocalFile() for how the filename is determined
	 */
	protected String getInfix() {
		return null;
	}
	
	/**
	 * Writes an observation to file using a given Writer.
	 * Override to write specific datatypes.
	 * @see Terminal.getLocalFile() for how the filename is determined
	 * 
	 * @param out to write to file with. It should not be closed.
	 * @param obs observation to write
	 */
	protected abstract void write(Writer out, Observation<T> obs) throws IOException;
	
	/** Delegates the send operation to the write method. */
	@Override
	public final void send(Observation<T> obs) {
		Writer out = null;
		try {
			File output = getLocalFile(getInfix());
			out = new BufferedWriter(new FileWriter(output));
			write(out, obs);
		} catch (IOException ex) {
			Logger.getLogger(FileSink.class.getName()).log(Level.SEVERE, getLocalName() + " could not write to file", ex);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException ex) {
					Logger.getLogger(FileSink.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
	}
}

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
 *
 * @author Joris Borgdorff
 */
public abstract class FileSink<T extends Serializable> extends Sink<T> {
	
	protected String getInfix() {
		return null;
	}
	
	protected abstract void write(Writer out, Observation<T> obs) throws IOException;
	
	@Override
	public void send(Observation<T> obs) {
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

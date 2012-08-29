package muscle.core.conduit.terminal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.model.Observation;
import muscle.id.PortalID;

/**
 * Reads data from a source.
 * Override receive to use.
 * Use the 'in' variable to read from the specified file, in the CxA file referred to as name:file and name:relative (if relative to the tmp directory)
 * @author Joris Borgdorff
 */
public abstract class FileSource<T extends Serializable> extends Source<T> {
	protected String getInfix() {
		return null;
	}
	
	protected abstract Observation<T> read(Reader in) throws IOException;

	@Override
	public Observation<T> take() {
		Reader in = null;
		try {
			File input = getLocalFile(getInfix());
			in = new BufferedReader(new FileReader(input));
			return read(in);
		} catch (IOException ex) {
			Logger.getLogger(FileSource.class.getName()).log(Level.SEVERE, getLocalName() + " could not read from file", ex);
			throw new IllegalStateException(getLocalName() + " could not read from file", ex);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ex) {
					Logger.getLogger(FileSource.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
	}
}

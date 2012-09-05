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
 * Override read method and optionally getInfix to use.
 * @author Joris Borgdorff
 */
public abstract class FileSource<T extends Serializable> extends Source<T> {
	/**
	 * Provides an infix to the filename to be written to.
	 * @see Terminal.getLocalFile() for how the filename is determined
	 */
	protected String getInfix() {
		return null;
	}
	
	/**
	 * Read an observation from the given reader.
	 * Override to read specific data types or formats.
	 * @see Terminal.getLocalFile() for how the filename is determined
	 * @param in to read file with. Do not close.
	 * @return an Observation with data and the correct timestamp.
	 * @throws IOException if there was an error reading
	 */
	protected abstract Observation<T> read(Reader in) throws IOException;

	@Override
	/** Delegates the take operation to the read method. */
	public final Observation<T> take() {
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

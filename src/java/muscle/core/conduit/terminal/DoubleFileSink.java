/*
 * 
 */

package muscle.core.conduit.terminal;

import java.io.IOException;
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
	protected void write(Writer out, Observation<double[]> obs) throws IOException {
		double[] data = obs.getData();
		if (data != null) {
			int sz = data.length - 1;
			for (int i = 0; i < sz; i++) {
				out.write(String.valueOf(data[i]));
				out.write(delimiter);
			}
			if (sz >= 0) {
				out.write(String.valueOf(data[sz]));
			}
		}
	}
}

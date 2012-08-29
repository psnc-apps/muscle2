/*
 * 
 */

package muscle.core.conduit.terminal;

import java.io.IOException;
import java.io.Writer;
import muscle.core.model.Observation;

/**
 *
 * @author Joris Borgdorff
 */
public class DoubleFileSink extends FileSink<double[]> {
	private int iteration;
	private String delimiter;
	
	@Override
	public void beforeExecute() {
		delimiter = hasProperty("delimiter") ? getProperty("delimiter") : " ";
		iteration = 0;
	}
	
	@Override
	protected String getInfix() {
		return String.valueOf(iteration);
	}
	
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
		iteration++;
	}
}

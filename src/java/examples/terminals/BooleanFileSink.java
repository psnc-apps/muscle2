/*
 * 
 */

package examples.terminals;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import muscle.core.conduit.terminal.FileSink;
import muscle.core.model.Observation;

/**
 *
 * @author Joris Borgdorff
 */
public class BooleanFileSink extends FileSink<boolean[]> {
	int iteration = 0;
	@Override
	protected String getInfix() {
		return String.valueOf(iteration);
	}
	
	@Override
	protected void write(Writer out, Observation<boolean[]> obs) throws IOException {
		boolean[] data = obs.getData();
		System.out.println("Writing data to <"+ getLocalFile(getInfix()) + ">");
		for (boolean x : data) {
			out.write(x ? "1 " : "0 ");
		}
		iteration++;
	}
}

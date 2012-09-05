/*
 * 
 */

package muscle.core.conduit.terminal;

import java.io.IOException;
import java.io.Reader;
import java.util.Scanner;
import muscle.core.model.Observation;

/**
 * A FileSource that reads double arrays from a file.
 * Unless extended and overriding getInfix() and isEmpty(), it reads a double array
 * from a file exactly once, and will return empty afterwards.
 * Specifically, it reads from the file, delimited by the "delimiter" property
 * (default: space), until a non-double is encountered.
 * @author Joris Borgdorff
 */
public class DoubleFileSource extends FileSource<double[]> {
	private int iteration;
	private String delimiter;
	
	@Override
	public void beforeExecute() {
		delimiter = hasProperty("delimiter") ? getProperty("delimiter") : " ";
		iteration = 0;
	}
	
	/** Returns true after one read is called once. */
	@Override
	public boolean isEmpty() {
		return iteration > 0;
	}
	
	@Override
	protected Observation<double[]> read(Reader in) throws IOException {
		Scanner sc = new Scanner(in);
		sc.useDelimiter(delimiter);

		int size = 0;
		int capacity = 100;
		double[] value = new double[capacity];
		while (sc.hasNextDouble()) {
			if (capacity == size) {
				capacity = (capacity*3)/2;
				double[] tmp = new double[capacity];
				System.arraycopy(value, 0, tmp, 0, size);
				value = tmp;
			}
			value[size] = sc.nextDouble();
			size++;
		}
		
		iteration++;
		if (size != capacity) {
			double[] tmp = new double[size];
			System.arraycopy(value, 0, tmp, 0, size);
			value = tmp;
		}
		return new Observation<double[]>(value, getSITime(), getSITime());
	}
}

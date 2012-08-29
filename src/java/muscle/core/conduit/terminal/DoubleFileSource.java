/*
 * 
 */

package muscle.core.conduit.terminal;

import java.io.IOException;
import java.io.Reader;
import java.util.Scanner;
import muscle.core.model.Observation;

/**
 *
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

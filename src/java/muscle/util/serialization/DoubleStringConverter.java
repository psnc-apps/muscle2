/*
 * 
 */
package muscle.util.serialization;

/**
 * @author Joris Borgdorff
 */
public class DoubleStringConverter extends AbstractDataConverter<double[], String> {
	@Override
	public String serialize(double[] data) {
		// Sane version
		//StringBuilder sb = new StringBuilder(data.length*20);
		for(int i = 0; i < data.length; i++) {
			//Original version:
			System.out.println(i + ": " + data[i]);
			// Sane version:
			//sb.append(i).append(": ").append(data[i]).append('\n');
		}
		// Original version
		return String.valueOf(data.length);
		// Sane version
		//return sb.toString();
	}

	@Override
	public double[] deserialize(String data) {
		double[] out = new double[Integer.valueOf(data)];
		out[0] = Double.valueOf(data);
		out[Integer.valueOf(data)-1] = -Double.valueOf(data);
		return out;
	}
}

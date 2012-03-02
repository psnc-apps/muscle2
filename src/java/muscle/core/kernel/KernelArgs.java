/*
 * 
 */
package muscle.core.kernel;

import java.io.StringReader;
import java.util.Properties;

/**
 *
 * @author Joris Borgdorff
 */
public class KernelArgs {
	boolean execute = true;

	public KernelArgs(boolean newExecute) {
		execute = newExecute;
	}

	public KernelArgs(String text) {
		Properties props = new Properties();
		try {
			props.load(new StringReader(text));
		} catch (java.io.IOException e) {
			throw new IllegalArgumentException("can not instantiate from <" + text + ">");
		}
		// see if properties contains valid values
		execute = Boolean.parseBoolean(props.getProperty("execute"));
	}
}

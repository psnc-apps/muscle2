/*
 * 
 */
package muscle.core.kernel;

import java.io.StringReader;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javatool.PropertiesTool;

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
			throw new IllegalArgumentException("can not instantiate from <" + props.toString() + ">");
		}
		// see if properties contains valid values
		Boolean val = null;
		try {
			val = PropertiesTool.getPropertyWithType(props, "execute", Boolean.class);
		} catch (java.lang.reflect.InvocationTargetException e) {
			throw new IllegalArgumentException("can not instantiate from <" + props.toString() + ">");
		}
		if (val != null) {
			execute = val.booleanValue();
		} else {
			throw new IllegalArgumentException("can not instantiate from <" + props.toString() + ">");
		}
	}
	
}

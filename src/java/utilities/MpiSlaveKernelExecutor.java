package utilities;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import muscle.core.Scale;
import muscle.core.kernel.CAController;
import muscle.core.messaging.jade.ObservationMessage;

public class MpiSlaveKernelExecutor {

	private static final long serialVersionUID = 1L;

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.err.println("This app takes one argument only!");
			System.exit(1);
		}

		CAController kernel = (CAController) Class.forName(args[0])
				.getConstructor().newInstance();

		kernel.executeDirectly();

	}

}

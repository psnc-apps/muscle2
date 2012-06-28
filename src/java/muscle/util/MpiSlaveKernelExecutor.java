package muscle.util;

import muscle.core.kernel.CAController;

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

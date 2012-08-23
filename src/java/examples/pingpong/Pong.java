package examples.pingpong;

import muscle.core.ConduitEntrance;
import muscle.core.ConduitExit;
import muscle.core.kernel.CAController;

/**
 * Receives at 'in' exit byte array and writes them to 'out' exit
 */
public class Pong extends CAController {

	private static final long serialVersionUID = 1L;
	private ConduitEntrance<byte[]> entrance;
	private ConduitExit<byte[]> exit;

	@Override
	protected void addPortals() {
		entrance = addEntrance("out", byte[].class);
		exit = addExit("in", byte[].class);

	}

	@Override
	protected void execute() {
		int count = getIntProperty("preparation_steps") + getIntProperty("tests_count")*getIntProperty("same_size_runs")*getIntProperty("steps");
		
		for (int i = 0; i < count; i++) {
			byte[] ba = exit.receive();
			entrance.send(ba);
		}
	}
}

package examples.pingpong;

import muscle.core.ConduitEntrance;
import muscle.core.ConduitExit;
import muscle.core.Scale;
import muscle.core.kernel.CAController;
import muscle.core.model.Distance;

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
		while (!willStop()) {
			byte[] ba = exit.receive();
			entrance.send(ba);
		}
	}

	@Override
	public Scale getScale() {
		Distance delta = new Distance(1);
		return new Scale(delta,delta);
	}
}

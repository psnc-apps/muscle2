package examples.pingpong;

import java.math.BigDecimal;

import javax.measure.DecimalMeasure;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import muscle.core.ConduitEntrance;
import muscle.core.ConduitExit;
import muscle.core.Scale;
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
		entrance = addEntrance("out", 1, byte[].class);
		exit = addExit("in", 1, byte[].class);

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
		DecimalMeasure<Duration> dt = DecimalMeasure.valueOf(new BigDecimal(1),
				SI.SECOND);
		DecimalMeasure<Length> dx = DecimalMeasure.valueOf(new BigDecimal(1),
				SI.METER);
		return new Scale(dt, dx);
	}

}

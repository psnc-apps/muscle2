package examples.simplempi;

import muscle.core.Scale;
import java.math.BigDecimal;
import javax.measure.DecimalMeasure;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;

public class Test extends muscle.core.kernel.CAController {

	static {
		System.loadLibrary("example_simplempi");
	}

	private int time;

	private native void callNative();

	public muscle.core.Scale getScale() {
		DecimalMeasure<Duration> dt = DecimalMeasure.valueOf(new BigDecimal(1), SI.SECOND);
		DecimalMeasure<Length> dx = DecimalMeasure.valueOf(new BigDecimal(1), SI.METER);
		return new Scale(dt,dx);
	}

	public void addPortals() {
	
	}

	protected void execute() {
		callNative();	
	}	

}

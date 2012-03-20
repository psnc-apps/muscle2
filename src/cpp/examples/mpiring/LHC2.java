package examples.mpiring;

import java.math.BigDecimal;

import javax.measure.DecimalMeasure;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import muscle.core.ConduitExit;
import muscle.core.CxADescription;
import muscle.core.Scale;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;

public class LHC2 extends muscle.core.standalone.MPIKernel {

	public muscle.core.Scale getScale() {
		DecimalMeasure<Duration> dt = DecimalMeasure.valueOf(new BigDecimal(1), SI.SECOND);
		DecimalMeasure<Length> dx = DecimalMeasure.valueOf(new BigDecimal(1), SI.METER);
		return new Scale(dt,dx);
	}

	public void addPortals() {
		addExit("pipe", 1, double[].class);
	}

}

package examples.simplempi;

import muscle.core.DataTemplate;
import muscle.core.Scale;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import muscle.core.ConduitEntrance;
import muscle.core.ConduitExit;
import muscle.core.JNIConduitEntrance;
import muscle.core.JNIConduitExit;
import muscle.core.CxADescription;
import muscle.core.PortalID;
import muscle.exception.MUSCLERuntimeException;
import utilities.jni.JNIMethod;
import utilities.MiscTool;
import javatool.ArraysTool;
import utilities.Transmutable;
import utilities.PipeTransmuter;
import java.math.BigDecimal;
import javax.measure.DecimalMeasure;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;

public class Test extends muscle.core.kernel.CAController {

	static {
		System.loadLibrary("simplempi_lib");
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

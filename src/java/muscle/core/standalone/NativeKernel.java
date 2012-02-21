package muscle.core.standalone;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.measure.DecimalMeasure;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import muscle.core.CxADescription;
import muscle.core.JNIConduitEntrance;
import muscle.core.JNIConduitExit;
import muscle.core.Scale;
import muscle.core.kernel.CAController;


public class NativeKernel extends CAController  implements NativeGateway.CallListener {
	
	protected List<JNIConduitEntrance> entrances = new ArrayList<JNIConduitEntrance>();
	protected List<JNIConduitExit> exits = new ArrayList<JNIConduitExit>();


	public int addEntrance(String name, int rate, int type) {
		entrances.add(addJNIEntrance(name, rate, double[].class)); /* TODO select class based on the type */
		return entrances.size() - 1;
	}
	
	public int addExit(String name, int rate, int type) {
		exits.add(addJNIExit(name, rate, double[].class)); /* TODO select class based on the type */
		return exits.size() - 1;
	}
	
	public void send(int entranceId, double data[]) {
		entrances.get(entranceId).send(data);
	}
	
	public double[] receive(int exitId) {
		return (double[])exits.get(exitId).receive();
	}
	
	public String getKernelName() {
		return controller.getLocalName();
	}
	
	public String getProperty(String name) {
		return CxADescription.ONLY.getProperty(name);
	}

	@Override
	protected void addPortals() {
		/* portals are added in the NativeGateway.CallListener calls */
	}

	@Override
	protected void execute() {
		
		try {
			NativeGateway gateway = new NativeGateway(this);
		
			ProcessBuilder pb = new ProcessBuilder(
				CxADescription.ONLY.getProperty(controller.getLocalName() + ":command"));
			
			if (CxADescription.ONLY.containsKey(controller.getLocalName() + ":arg"))
				pb.command().add(CxADescription.ONLY.getProperty(controller.getLocalName() + ":arg"));
		
			pb.environment().put("MUSCLE_GATEWAY", gateway.getContactInformation());
		
			getLogger().info("Spawning standalone kernel: " + pb.command().get(0));
			
			Process child = pb.start();
			
			StreamRipper stdoutR =  new StreamRipper(System.out, child.getInputStream());
			StreamRipper stderrR =  new StreamRipper(System.err, child.getErrorStream());
			
			stdoutR.start();
			stderrR.start();
			
			int exitCode = child.waitFor();
			
			getLogger().info("Command " + pb.toString() + " finished with exit code " + exitCode);

			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}

	@Override
	public Scale getScale() {
		DecimalMeasure<Duration> dt = DecimalMeasure.valueOf(new BigDecimal( CxADescription.ONLY.getDoubleProperty(controller.getLocalName() +  ":timeScale")), SI.SECOND);
		
		
		DecimalMeasure<Length> dx = DecimalMeasure.valueOf(new BigDecimal(1), SI.METER); /* not used anywhere so can be hardcoded by now. TODO provide information about scale in native code */
		
		return new Scale(dt,dx);
	}

}

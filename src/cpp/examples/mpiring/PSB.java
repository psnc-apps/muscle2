package examples.mpiring;

import java.math.BigDecimal;

import javax.measure.DecimalMeasure;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import muscle.core.ConduitEntrance;
import muscle.core.CxADescription;
import muscle.core.Scale;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;

public class PSB extends muscle.core.kernel.CAController {
	
	private ConduitEntrance<double[]> pipeOutput;


  	public interface MPIRING extends Library {
		public void Ring_Init(String ringName, double deltaE, double energyThreshold);
		public double insertProton(double initialEnergy, EnergyCallback energyCallback);
		public boolean  isMasterNode();
		public void accelerateProtons();
		public void Ring_Cleanup();
   	}

	public  interface EnergyCallback extends Callback {
    		void newEnergy(double energy);
  	}
	
	static EnergyCallback energyCallback = new EnergyCallback(){
        	public void newEnergy(double energy) {
             		System.out.println("PSB: Proton energy callback: " + energy);
            		}
		};

	static MPIRING ring;

	static {
		ring = (MPIRING) Native.loadLibrary("mpiring", MPIRING.class);
	}

	public muscle.core.Scale getScale() {
		DecimalMeasure<Duration> dt = DecimalMeasure.valueOf(new BigDecimal(1), SI.SECOND);
		DecimalMeasure<Length> dx = DecimalMeasure.valueOf(new BigDecimal(1), SI.METER);
		return new Scale(dt,dx);
	}

	public void addPortals() {
		pipeOutput = addEntrance("pipe", 1, double[].class);
	}
	
	static double DELTA_E = CxADescription.ONLY.getDoubleProperty("PSB:DeltaEnergy");
	static double INITIAL_E = CxADescription.ONLY.getDoubleProperty("PSB:InitialEnergy");
	static double MAX_E_THRESHOLD = CxADescription.ONLY.getDoubleProperty("PSB:MaxEnergy");
	 

	protected void execute() {
	
		ring.Ring_Init("PSB", DELTA_E, MAX_E_THRESHOLD);

        if (ring.isMasterNode()) {
        	System.err.println("Inserting proton into Proton Synchrotron Booster (PSB). Initial energy: " + INITIAL_E);
        	double newEnergy = ring.insertProton(INITIAL_E, energyCallback);
        	System.err.println("Proton energy after PSB:  " + newEnergy + ". Injecting into LHC.");
        	pipeOutput.send(new double[] { newEnergy });
        }  else
            ring.accelerateProtons();

        ring.Ring_Cleanup();
	}	
}

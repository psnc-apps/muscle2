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

public class LHC extends muscle.core.kernel.CAController {
	
	private ConduitExit<double[]> pipeInput;

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
             		System.out.println("LHC: Proton energy callback: " + energy);
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
		pipeInput = addExit("pipe", 1, double[].class);
	}
	
	static double DELTA_E = CxADescription.ONLY.getDoubleProperty("LHC:DeltaEnergy");
	static double MAX_E_THRESHOLD = CxADescription.ONLY.getDoubleProperty("LHC:MaxEnergy");

	protected void execute() {
	
		ring.Ring_Init("LHC", DELTA_E, MAX_E_THRESHOLD);

        if (ring.isMasterNode()) {
        	double initialEnergy = pipeInput.receive()[0];
        	System.err.println("LHC: Received proton from PSB: " +  initialEnergy);
        	double finalEnergy = ring.insertProton(initialEnergy, energyCallback);
        	System.err.println("LHC: Final energy: " +  finalEnergy);
        } else
            ring.accelerateProtons();

        ring.Ring_Cleanup();
	}	

}

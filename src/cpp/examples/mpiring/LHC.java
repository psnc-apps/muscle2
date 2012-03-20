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
import com.sun.jna.ptr.DoubleByReference;

public class LHC extends muscle.core.kernel.CAController {
	
	private ConduitExit<double[]> pipeInput;

  	public interface MPIRING extends Library {
		public void Ring_Init(String ringName);
		public void Ring_Broadcast_Params(DoubleByReference deltaE, DoubleByReference maxE);
		public double insertProton(double initialEnergy, double energyThreshold, EnergyCallback energyCallback);
		public boolean  isMasterNode();
		public void accelerateProtons( double deltaE, double energyThreshold);
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
	

	protected void execute() {
	
		ring.Ring_Init("LHC");

        if (ring.isMasterNode()) {
        	double deltaEnergy = CxADescription.ONLY.getDoubleProperty("LHC:DeltaEnergy");
        	double maxEnergy = CxADescription.ONLY.getDoubleProperty("LHC:MaxEnergy");
        	
        	ring.Ring_Broadcast_Params(new DoubleByReference(deltaEnergy), new DoubleByReference(maxEnergy));
        	
        	double initialEnergy = pipeInput.receive()[0];
        	System.err.println("LHC: Received proton from PSB: " +  initialEnergy);
        	
        	double finalEnergy = ring.insertProton(initialEnergy, maxEnergy, energyCallback);
        	System.err.println("LHC: Final energy: " +  finalEnergy);
        } else {
        	DoubleByReference deltaEnergy = new DoubleByReference();
        	DoubleByReference maxEnergy = new DoubleByReference();
        	
        	ring.Ring_Broadcast_Params(deltaEnergy, maxEnergy);

            ring.accelerateProtons(deltaEnergy.getValue(), maxEnergy.getValue());
        }

        ring.Ring_Cleanup();
	}	

}

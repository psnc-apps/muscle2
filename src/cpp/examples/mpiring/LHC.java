/*
* Copyright 2008, 2009 Complex Automata Simulation Technique (COAST) consortium
* Copyright 2010-2013 Multiscale Applications on European e-Infrastructures (MAPPER) project
*
* GNU Lesser General Public License
* 
* This file is part of MUSCLE (Multiscale Coupling Library and Environment).
* 
* MUSCLE is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* MUSCLE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public License
* along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/
package examples.mpiring;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.ptr.DoubleByReference;
import muscle.core.ConduitExit;
import muscle.core.CxADescription;

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

	public void addPortals() {
		pipeInput = addExit("pipe", double[].class);
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

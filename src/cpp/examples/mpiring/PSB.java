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
import muscle.core.ConduitEntrance;
import muscle.core.CxADescription;

public class PSB extends muscle.core.kernel.CAController {
	
	private ConduitEntrance<double[]> pipeOutput;


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
             		System.out.println("PSB: Proton energy callback: " + energy);
            		}
		};

	static MPIRING ring;

	static {
		ring = (MPIRING) Native.loadLibrary("mpiring", MPIRING.class);
	}

	public void addPortals() {
		pipeOutput = addEntrance("pipe", double[].class);
	}

	protected void execute() {
	
		ring.Ring_Init("PSB");

        if (ring.isMasterNode()) {
        	double initialEnergy = CxADescription.ONLY.getDoubleProperty("PSB:InitialEnergy");
        	double deltaEnergy = CxADescription.ONLY.getDoubleProperty("PSB:DeltaEnergy");
        	double maxEnergy =  CxADescription.ONLY.getDoubleProperty("PSB:MaxEnergy");
        	
        	ring.Ring_Broadcast_Params(new DoubleByReference(deltaEnergy), new DoubleByReference(maxEnergy));
        	
        	System.err.println("Inserting proton into Proton Synchrotron Booster (PSB). Initial energy: " + initialEnergy);
        	double newEnergy = ring.insertProton(initialEnergy, maxEnergy, energyCallback);
        	System.err.println("Proton energy after PSB:  " + newEnergy + ". Injecting into LHC.");
        	pipeOutput.send(new double[] { newEnergy });
        }  else {
        	DoubleByReference deltaEnergy = new DoubleByReference();
        	DoubleByReference maxEnergy = new DoubleByReference();
        	
        	ring.Ring_Broadcast_Params(deltaEnergy, maxEnergy);

            ring.accelerateProtons(deltaEnergy.getValue(), maxEnergy.getValue());
        }
        ring.Ring_Cleanup();
	}	
}

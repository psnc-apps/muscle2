/*
Copyright 2008,2009 Complex Automata Simulation Technique (COAST) consortium

GNU Lesser General Public License

This file is part of MUSCLE (Multiscale Coupling Library and Environment).

    MUSCLE is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    MUSCLE is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/

package examples.laplace;

import muscle.core.Scale;
import muscle.core.kernel.RawKernel;
import java.math.BigDecimal;
import javax.measure.DecimalMeasure;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import javax.swing.JFrame;
import muscle.core.ConduitExit;
import muscle.core.ConduitEntranceController;
import muscle.core.CxADescription;


/**
heat flow calculation wrapped in a MUSCLE kernel for distributed computation
@author Jan Hegewald
*/
public class KernelEast extends muscle.core.kernel.CAController {

	private ConduitExit<double[]> readerWest;
	private ConduitEntranceController<double[]> writerWest;
	private Temperature t;
	
	
	//
	public muscle.core.Scale getScale() {
		DecimalMeasure<Duration> dt = DecimalMeasure.valueOf(new BigDecimal(1), SI.SECOND);
		DecimalMeasure<Length> dx = DecimalMeasure.valueOf(new BigDecimal(1), SI.METER);
		return new Scale(dt,dx);
	}


	/**
	init the temperature calculator
	*/
	protected void beforeSetup() {

      JFrame frame = new JFrame("east");
      frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		final int nnx = CxADescription.ONLY.getIntProperty("nx");
		final int nny = CxADescription.ONLY.getIntProperty("ny");
		int dx = CxADescription.ONLY.getIntProperty("dx");
		
		t = new Temperature(nnx, nny, dx) {

			protected void initBoundaryConditions() {
				
				north = new BoundaryCondition.NorthBoundary(nnx, nny, data) {
					public double get(int x, int y, int step) {
						return Math.sin((x+nx) * 2 * Math.PI / (nx*2));
					}
				};
				
				east = new BoundaryCondition.EastBoundary(nnx, nny, data);
				
				south = new BoundaryCondition.SouthBoundary(nnx, nny, data) {
					public double get(int x, int y, int step) {
						return Math.cos((x+nx) * 2 * Math.PI / (nx*2) + Math.PI);
					}
				};
				
				west = new GhostBoundaryWest(nnx, nny, data);
				
				initial = new BoundaryCondition.DefaultCondition(nnx, nny, data);

				area = new BoundaryCondition.AreaCondition(nnx, nny, data, north, east, south, west);	
			}
		};
		frame.add(t.getGUI());
		
		frame.setLocation((int)frame.getLocation().getX()+nnx*dx, (int)frame.getLocation().getY());

		frame.pack();
      frame.setVisible(true);
   }


	/**
	announce our conduit connections
	*/
	protected void addPortals() {
	
		writerWest = addEntrance("east", 1, double[].class);
		readerWest = addExit("west", 1, double[].class);
	}
		

	/**
	call run loop of the temperature calculator
	*/
	protected void execute() {

      t.run(CxADescription.ONLY.getIntProperty("max_timesteps"));
	}
	

	/**
	custom boundary condition which is using a ghostnode column to synchronize data with the other kernel
	*/
   public class GhostBoundaryWest implements BoundaryCondition {

      private int lastStep = -1;
		private double[] remoteSlice;
		private double[] localSlice;
		private double[][] data;
		int nx;
		int ny;
		
		public GhostBoundaryWest(int newNx, int newNy, double[][] newData) {

			nx = newNx;
			ny = newNy;
			data = newData;
			
			// init local slice
			localSlice = new double[ny];
			for(int y = 0; y < ny; y++)
				for(int x = 0; x < nx; x++) {
					if(applies(x, y))
						localSlice[y] = data[x][y];
				}				

			// set remote slice to be the same as our local one, just for initialization
			remoteSlice = localSlice;
		}
						
		public boolean applies(int x, int y) {
		
			return x == 0;
		}
		
      // synchronize ghostnode column with their corresponding original data
		// calc new boundary value based on the neighbours
      public double get(int x, int y, int step) {

         if(step > lastStep) { // update ghost nodes
				lastStep = step;
				
				writerWest.send(localSlice);
				remoteSlice = readerWest.receive();
			}

			double val;
			if( (x == nx-1) || (y == 0) || (y == ny-1) )
				val = data[x][y];
			else {
				double n = data[x][y+1];
				double e = data[x+1][y];
				double s = data[x][y-1];
				double w = remoteSlice[y];

				val = (n+e+s+w)/4.0;
			}
			
			localSlice[y] = val;
			return val;			
      }
   }

}

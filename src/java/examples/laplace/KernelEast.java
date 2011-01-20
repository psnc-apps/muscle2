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

import java.math.BigDecimal;

import javax.measure.DecimalMeasure;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import javax.swing.JFrame;

import muscle.core.ConduitEntrance;
import muscle.core.ConduitExit;
import muscle.core.CxADescription;
import muscle.core.Scale;


/**
heat flow calculation wrapped in a MUSCLE kernel for distributed computation
@author Jan Hegewald
*/
public class KernelEast extends muscle.core.kernel.CAController {

	private static final long serialVersionUID = 1L;
	private ConduitExit<double[]> readerWest;
	private ConduitEntrance<double[]> writerWest;
	private Temperature t;


	//
	@Override
	public muscle.core.Scale getScale() {
		DecimalMeasure<Duration> dt = DecimalMeasure.valueOf(new BigDecimal(1), SI.SECOND);
		DecimalMeasure<Length> dx = DecimalMeasure.valueOf(new BigDecimal(1), SI.METER);
		return new Scale(dt,dx);
	}


	/**
	init the temperature calculator
	*/
	@Override
	protected void beforeSetup() {

      JFrame frame = new JFrame("east");
      frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		final int nnx = CxADescription.ONLY.getIntProperty("nx");
		final int nny = CxADescription.ONLY.getIntProperty("ny");
		int dx = CxADescription.ONLY.getIntProperty("dx");

		this.t = new Temperature(nnx, nny, dx) {

			@Override
			protected void initBoundaryConditions() {

				this.north = new BoundaryCondition.NorthBoundary(nnx, nny, this.data) {
					@Override
					public double get(int x, int y, int step) {
						return Math.sin((x+this.nx) * 2 * Math.PI / (this.nx*2));
					}
				};

				this.east = new BoundaryCondition.EastBoundary(nnx, nny, this.data);

				this.south = new BoundaryCondition.SouthBoundary(nnx, nny, this.data) {
					@Override
					public double get(int x, int y, int step) {
						return Math.cos((x+this.nx) * 2 * Math.PI / (this.nx*2) + Math.PI);
					}
				};

				this.west = new GhostBoundaryWest(nnx, nny, this.data);

				this.initial = new BoundaryCondition.DefaultCondition(nnx, nny, this.data);

				this.area = new BoundaryCondition.AreaCondition(nnx, nny, this.data, this.north, this.east, this.south, this.west);
			}
		};
		frame.add(this.t.getGUI());

		frame.setLocation((int)frame.getLocation().getX()+nnx*dx, (int)frame.getLocation().getY());

		frame.pack();
      frame.setVisible(true);
   }


	/**
	announce our conduit connections
	*/
	@Override
	protected void addPortals() {

		this.writerWest = this.addEntrance("east", 1, double[].class);
		this.readerWest = this.addExit("west", 1, double[].class);
	}


	/**
	call run loop of the temperature calculator
	*/
	@Override
	protected void execute() {

      this.t.run(CxADescription.ONLY.getIntProperty("max_timesteps"));
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

			this.nx = newNx;
			this.ny = newNy;
			this.data = newData;

			// init local slice
			this.localSlice = new double[this.ny];
			for(int y = 0; y < this.ny; y++) {
				for(int x = 0; x < this.nx; x++) {
					if(this.applies(x, y)) {
						this.localSlice[y] = this.data[x][y];
					}
				}
			}

			// set remote slice to be the same as our local one, just for initialization
			this.remoteSlice = this.localSlice;
		}

		public boolean applies(int x, int y) {

			return x == 0;
		}

      // synchronize ghostnode column with their corresponding original data
		// calc new boundary value based on the neighbours
      public double get(int x, int y, int step) {

         if(step > this.lastStep) { // update ghost nodes
				this.lastStep = step;

				KernelEast.this.writerWest.send(this.localSlice);
				this.remoteSlice = KernelEast.this.readerWest.receive();
			}

			double val;
			if( (x == this.nx-1) || (y == 0) || (y == this.ny-1) ) {
				val = this.data[x][y];
			} else {
				double n = this.data[x][y+1];
				double e = this.data[x+1][y];
				double s = this.data[x][y-1];
				double w = this.remoteSlice[y];

				val = (n+e+s+w)/4.0;
			}

			this.localSlice[y] = val;
			return val;
      }
   }

}

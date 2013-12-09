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

import examples.laplace.BoundaryCondition.Direction;

/**
 * heat flow calculation wrapped in a MUSCLE kernel for distributed computation
 *
 * @author Jan Hegewald
 */
public class KernelEast extends TemperatureKernel {
	private CommunicationBoundary remoteWest, remoteEast;
	
	/**
	 * init the temperature calculator
	 */
	@Override
	protected void initFunctions() {
		west = remoteWest = new CommunicationBoundary(Direction.West, nx, ny, data);
		if (wrapAround) {
			east = remoteEast = new CommunicationBoundary(Direction.East, nx, ny, data);
		} else {
			east = new BoundaryCondition(Direction.East, nx, ny);
		}

		north = new BoundaryCondition(Direction.North, nx, ny) {
			@Override
			public double get(int x, int y, int step) {
				return Math.sin((x + nx) * 2 * Math.PI / (nx * 2));
			}
		};
		south = new BoundaryCondition(Direction.South, nx, ny) {
			@Override
			public double get(int x, int y, int step) {
				return Math.cos((x + nx) * 2 * Math.PI / (nx * 2) + Math.PI);
			}
		};
		
		initialCondition = new BoundaryCondition(Direction.Any, nx, ny);
		areaFunction = new AreaFunction(nx, ny);
		
		panel.show("East", nx*dx, 0);
	}

	@Override
	protected void intermediateObservation() {
		this.<double[]>out("westBoundary").send(remoteWest.getLocalBoundary());
		if (wrapAround)
			this.<double[]>out("eastBoundary").send(remoteEast.getLocalBoundary());
	}
	
	@Override
	protected void solvingStep() {
		remoteWest.updateData(data, this.<double[]>in("remoteWest").receive());
		if (wrapAround)
			remoteEast.updateData(data, this.<double[]>in("remoteEast").receive());
		super.solvingStep();
	}
}

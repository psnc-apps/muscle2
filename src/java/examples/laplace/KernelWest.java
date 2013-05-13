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
public class KernelWest extends TemperatureKernel {
	private CommunicationBoundary remoteEast, remoteWest;
	
	/**
	 * init the temperature calculator
	 */
	@Override
	protected void initFunctions() {
		east = remoteEast = new CommunicationBoundary(Direction.East, nx, ny, data);
		if (wrapAround)
			west = remoteWest = new CommunicationBoundary(Direction.West, nx, ny, data);
		else
			west = new BoundaryCondition(Direction.West, nx, ny);
		north = new BoundaryCondition(Direction.North, nx, ny) {
					public double get(int x, int y, int step) {
						return Math.sin(x * 2 * Math.PI / (nx * 2));
					}
				};
		south = new BoundaryCondition(Direction.South, nx, ny) {
					public double get(int x, int y, int step) {
						return Math.cos(x * 2 * Math.PI / (nx * 2) + Math.PI);
					}
				};
		
		initialCondition = new BoundaryCondition(Direction.Any, nx, ny);
		areaFunction = new AreaFunction(nx, ny);

		panel.show("West", 0, 0);
	}

	@Override
	protected void intermediateObservation() {
		this.<double[]>out("eastBoundary").send(remoteEast.getLocalBoundary());
		
		if (wrapAround)
			this.<double[]>out("westBoundary").send(remoteWest.getLocalBoundary());
	}
	
	@Override
	protected void solvingStep() {
		remoteEast.updateData(data, this.<double[]>in("remoteEast").receive());

		if (wrapAround)
			remoteWest.updateData(data, this.<double[]>in("remoteWest").receive());
		super.solvingStep();
	}
}

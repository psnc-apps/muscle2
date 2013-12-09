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

package examples.laplace;

import examples.laplace.graphics.GraphicsPanel;
import muscle.core.kernel.Submodel;
import muscle.core.model.Timestamp;

/**
 *
 * @author Joris Borgdorff
 */
public abstract class TemperatureKernel extends Submodel {
	protected int nx;
	protected int ny;
	protected int dx; // dy=dx
	protected boolean wrapAround;

	private long nano, prevNano;
	private int iteration;
	
	protected double[][] data, nextData;
	protected BoundaryCondition north, south, west, east;
	protected BoundaryCondition initialCondition;
	protected AreaFunction areaFunction;
	protected GraphicsPanel panel;
	
	@Override
	protected void beforeSetup() {
		nx = getIntProperty("nx");
		ny = getIntProperty("ny");
		dx = hasProperty("dx") ? getIntProperty("dx") : 1;
		wrapAround = hasProperty("wrapAround") && getBooleanProperty("wrapAround");
		panel = new GraphicsPanel(nx, ny, dx);
	}
	
	protected abstract void initFunctions();
	
	@Override
	protected Timestamp init(Timestamp prevTime) {
		data = new double[nx][ny];
		nextData = new double[nx][ny];

		initFunctions();

		iteration = -1;
		
		// fill with initial conditions
		for (int x = 0; x < nx; x++) {
			for (int y = 0; y < ny; y++) {
				BoundaryCondition bc;
				
				if (north.applies(x, y)) bc = north;
				else if (south.applies(x, y)) bc = south;
				else if (east.applies(x, y)) bc = east;
				else if (west.applies(x, y)) bc = west;
				else bc = initialCondition;

				data[x][y] = bc.get(x, y, iteration);
			}
		}
		panel.paintAndWait(data);
		
		nano = prevNano = System.nanoTime();
		
		return super.init(prevTime);
	}
	
	// iterate	
	@Override
	protected void solvingStep() {
		iteration++;
		areaFunction.updateData(data);
		
		for (int x = 0; x < nx; x++) {
			for (int y = 0; y < ny; y++) {
				BoundaryCondition bc;
				if (north.applies(x, y)) bc = north;
				else if (south.applies(x, y)) bc = south;
				else if (east.applies(x, y)) bc = east;
				else if (west.applies(x, y)) bc = west;
				else bc = areaFunction;
		
				nextData[x][y] = bc.get(x, y, iteration);
			}
		}

		// swap data containers
		double[][] tmpData = data;
		data = nextData;
		nextData = tmpData;

		panel.paintAndWait(data);

		prevNano = System.nanoTime();
		log("step " + (iteration + 1) + " of " + getIntProperty("max_timesteps") + " done at time " + (prevNano - nano) / 1000000000d);		
	}
}

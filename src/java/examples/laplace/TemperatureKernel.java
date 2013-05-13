/*
 * 
 */

package examples.laplace;

import examples.laplace.graphics.GraphicsPanel;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JComponent;
import javax.swing.JFrame;
import muscle.core.kernel.CAController;
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

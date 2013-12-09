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
/*
 * 
 */

package examples.laplace.graphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author Joris Borgdorff
 */
public class GraphicsPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private final int nx, ny, dx;
	private double[][] data = null;
	
	public GraphicsPanel(int nx, int ny, int dx) {
		this.dx = dx;
		this.nx = nx;
		this.ny = ny;
		setPreferredSize(new Dimension(nx * dx, ny * dx));
	}
	
	public void show(String name, int xdiff, int ydiff) {
		JFrame frame = new JFrame(name);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(this);

		frame.setLocation((int) frame.getLocation().getX() + xdiff, (int) frame.getLocation().getY() + ydiff);

		frame.pack();
		frame.setVisible(true);
	}
	
	public synchronized void paintAndWait(double[][] newData) {
		synchronized (this) {
			while (data != null) {
				try {
					wait();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			data = newData;
		}
		repaint();
	}
	
	@Override
	protected void paintComponent(Graphics gg) {
		synchronized (this) {
			if (data == null) return;
		}
		super.paintComponent(gg);
		Graphics2D g = (Graphics2D) gg;
		// fill with initial conditions
		for (int x = 0; x < nx; x++) {
			for (int y = 0; y < ny; y++) {
				g.setColor(getColor(data[x][y]));
				g.fillRect(x * dx, (ny - 1) * dx - 1 * y * dx, dx, dx);
			}
		}
		synchronized (this) {
			data = null;
			notify();
		}
	}
	
	/**
	 * create a colour gradient from blue to red from the input values
	 * -1.0..1.0<br>
	 * -1.0 -0.5 0.0 0.5 1.0 | | | | | r 000::::000::::000<<<<255::::255 g
	 * 000<<<<255::::255::::255>>>>000 b 255::::255>>>>000::::000::::000
	 *
	 * @author Jan Hegewald
	 */
	private static Color getColor(double rawVal) {
		// val should be from -1 to 1
		// first we apply val to our colour range, which is from 0...4*256
		int val = (int) ((rawVal + 1) / 2.0 * 1023.0);
		assert val >= 0 && val < 4*256;
		
		if (val < 256) {
			return new Color(0, val, 255);
		} else if (val < 2 * 256) {
			return new Color(0, 255, 2*256 - 1 - val);
		} else if (val < 3 * 256) {
			return new Color(val - 2 * 256, 255, 0);
		} else {
			return new Color(255, 4 * 256 - 1 - val, 0);
		}
	}
}

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

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;

import examples.laplace.graphics.Utilities;


/**
heat flow calculation in 2D using coloured graphical output
@author Jan Hegewald
*/
public class Temperature {

   private int nx;
   private int ny;
   private int dx = 1; // dy=dx

	double[][] data;

	BoundaryCondition north;
	BoundaryCondition east;
	BoundaryCondition south;
	BoundaryCondition west;
	BoundaryCondition initial;
	BoundaryCondition area;

   private GraphicsPanel panel = new GraphicsPanel();


   //
   public Temperature(int newWidth, int newHeight) {
      this(newWidth, newHeight, 1);
   }

   //
   public Temperature(int newWidth, int newHeight, int newDx) {
      this.nx = newWidth;
      this.ny = newHeight;
      this.dx = newDx;

      this.panel.setPreferredSize(new java.awt.Dimension (this.nx*this.dx, this.ny*this.dx));
   }


	protected void initBoundaryConditions() {

		this.north = new BoundaryCondition.NorthBoundary(this.nx, this.ny, this.data);
		this.east = new BoundaryCondition.EastBoundary(this.nx, this.ny, this.data);
		this.south = new BoundaryCondition.SouthBoundary(this.nx, this.ny, this.data);
		this.west = new BoundaryCondition.WestBoundary(this.nx, this.ny, this.data);

		this.initial = new BoundaryCondition.DefaultCondition(this.nx, this.ny, this.data);

		this.area = new BoundaryCondition.AreaCondition(this.nx, this.ny, this.data, this.north, this.south, this.east, this.west);
	}


   //
   public void run(int max_timesteps) {

      this.data = new double[this.nx][this.ny];

		this.initBoundaryConditions();

      // fill with initial conditions
      for(int y = 0; y < this.ny; y++) {
		for(int x = 0; x < this.nx; x++) {

            if(this.north.applies(x, y)) {
				this.data[x][y] = this.north.get(x, y, -1);
			} else if(this.south.applies(x, y)) {
				this.data[x][y] = this.south.get(x, y, -1);
			} else if(this.east.applies(x, y)) {
				this.data[x][y] = this.east.get(x, y, -1);
			} else if(this.west.applies(x, y)) {
				this.data[x][y] = this.west.get(x, y, -1);
			} else {
				this.data[x][y] = this.initial.get(x, y, -1);
			}
         }
	}
      this.panel.paintAndWait();

      // prepare tmp data
      double[][] tmpData = new double[this.nx][this.ny];
      for(int y = 0; y < this.ny; y++) {
		for(int x = 0; x < this.nx; x++) {
            tmpData[x][y] = this.data[x][y];
         }
	}

      // iterate
      int iterationCount = max_timesteps;

      for(int i = 0; i < iterationCount; i++) {
//         System.out.println("step "+(i+1)+" of "+iterationCount+" ...");

         for(int y = 0; y < this.ny; y++) {
			for(int x = 0; x < this.nx; x++) {

					if(this.north.applies(x, y)) {
						tmpData[x][y] = this.north.get(x, y, i);
					} else if(this.south.applies(x, y)) {
						tmpData[x][y] = this.south.get(x, y, i);
					} else if(this.east.applies(x, y)) {
						tmpData[x][y] = this.east.get(x, y, i);
					} else if(this.west.applies(x, y)) {
						tmpData[x][y] = this.west.get(x, y, i);
					} else {
						tmpData[x][y] = this.area.get(x, y, i);
					}
            }
		}

         // swap data containers
         double[][] ttmpData = this.data;
         this.data = tmpData;
         tmpData = ttmpData;

         this.panel.paintAndWait();
         System.out.println("step "+(i+1)+" of "+iterationCount+" done");
      }
   }


   //
   public JComponent getGUI() {

      return this.panel;
   }


   //
   public class GraphicsPanel extends javax.swing.JPanel {

	private static final long serialVersionUID = 1L;
	private Boolean shouldRepaint;

      public void paintAndWait() {

         this.shouldRepaint = true;
         this.repaint();

         while(this.shouldRepaint) {
            try {
               Thread.sleep(10);
            } catch (InterruptedException e) {
               throw new RuntimeException(e);
            }
         }

      }


      private int paintCount;
      @Override
	synchronized protected void paintComponent(Graphics gg) {

         if(this.shouldRepaint == null) {
			return;
		} else if(this.shouldRepaint) {

            super.paintComponent(gg);

            Graphics2D g = (Graphics2D) gg;

            // fill with initial conditions
            for(int y = 0; y < Temperature.this.ny; y++) {
				for(int x = 0; x < Temperature.this.nx; x++) {

				      g.setColor(Utilities.getColor(Temperature.this.data[x][y]));
				      g.fillRect(x*Temperature.this.dx, (Temperature.this.ny-1)*Temperature.this.dx-1*y*Temperature.this.dx, Temperature.this.dx, Temperature.this.dx);
				   }
			}

            this.paintCount ++;
            this.shouldRepaint = false;
         }
      }
   }


}

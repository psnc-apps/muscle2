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

import examples.laplace.graphics.Utilities;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JComponent;


/**
heat flow calculation in 2D using coloured graphical output
@author Jan Hegewald
*/
public class Temperature {

   private int nx;
   private int ny;
   private int dx = 1; // dy=dx
   private long nano;
   
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
      nx = newWidth;
      ny = newHeight;
      dx = newDx;
      nano = System.nanoTime();

      panel.setPreferredSize(new java.awt.Dimension (nx*dx, ny*dx));
   }
	
	
	protected void initBoundaryConditions() {

		north = new BoundaryCondition.NorthBoundary(nx, ny, data);
		east = new BoundaryCondition.EastBoundary(nx, ny, data);
		south = new BoundaryCondition.SouthBoundary(nx, ny, data);
		west = new BoundaryCondition.WestBoundary(nx, ny, data);

		initial = new BoundaryCondition.DefaultCondition(nx, ny, data);

		area = new BoundaryCondition.AreaCondition(nx, ny, data, north, south, east, west);	
	}


   //
   public void run(int max_timesteps) {
	
      data = new double[nx][ny];

		initBoundaryConditions();

      // fill with initial conditions
      for(int y = 0; y < ny; y++)
         for(int x = 0; x < nx; x++) {

            if(north.applies(x, y))
               data[x][y] = north.get(x, y, -1);
            else if(south.applies(x, y))
               data[x][y] = south.get(x, y, -1);
            else if(east.applies(x, y))
               data[x][y] = east.get(x, y, -1);
            else if(west.applies(x, y))
               data[x][y] = west.get(x, y, -1);
            else
               data[x][y] = initial.get(x, y, -1);
         }
      panel.paintAndWait();

      // prepare tmp data
      double[][] tmpData = new double[nx][ny];
      for(int y = 0; y < ny; y++)
         for(int x = 0; x < nx; x++) {
            tmpData[x][y] = data[x][y];
         }

      // iterate
      int iterationCount = max_timesteps;
      
      for(int i = 0; i < iterationCount; i++) {
//         System.out.println("step "+(i+1)+" of "+iterationCount+" ...");

         for(int y = 0; y < ny; y++)
            for(int x = 0; x < nx; x++) {

					if(north.applies(x, y))
						tmpData[x][y] = north.get(x, y, i);
					else if(south.applies(x, y))
						tmpData[x][y] = south.get(x, y, i);
					else if(east.applies(x, y))
						tmpData[x][y] = east.get(x, y, i);
					else if(west.applies(x, y))
						tmpData[x][y] = west.get(x, y, i);
					else /*if(area.applies(x, y))*/
						tmpData[x][y] = area.get(x, y, i);
            }

         // swap data containers
         double[][] ttmpData = data;
         data = tmpData;
         tmpData = ttmpData;

         panel.paintAndWait();
         System.out.println("step "+(i+1)+" of "+iterationCount+" done at time " + (System.nanoTime() - nano)/1000000000d);
      }
   }


   //
   public JComponent getGUI() {

      return panel;
   }


   //
   public class GraphicsPanel extends javax.swing.JPanel {

      private Boolean shouldRepaint;

      public void paintAndWait() {

         shouldRepaint = true;
         repaint();

         while(shouldRepaint) {
            try {
               Thread.sleep(10);
            } catch (InterruptedException e) {
               throw new RuntimeException(e);
            }
         }
      
      }


      private int paintCount;
      synchronized protected void paintComponent(Graphics gg) {

         if(shouldRepaint == null)
            return;
         else if(shouldRepaint) {

            super.paintComponent(gg);

            Graphics2D g = (Graphics2D) gg;

            // fill with initial conditions
            for(int y = 0; y < ny; y++)
               for(int x = 0; x < nx; x++) {

                  g.setColor(Utilities.getColor(data[x][y]));
                  g.fillRect(x*dx, (ny-1)*dx-1*y*dx, dx, dx);
               }

            paintCount ++;
            shouldRepaint = false;
         }
      }
   }


}

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

package examples.laplace.graphics;

import java.awt.Color;


/**
graphic related utilities
@author Jan Hegewald
*/
public class Utilities {


	/**
   create a colour gradient from blue to red from the input values -1.0..1.0<br>
	 -1.0   -0.5    0.0    0.5    1.0
		 |      |      |      |      |
	r 000::::000::::000<<<<255::::255
	g 000<<<<255::::255::::255>>>>000
	b 255::::255>>>>000::::000::::000
	@author Jan Hegewald
   */
   public static Color getColor(double rawVal) {

      // val should be from -1 to 1
      // first we apply val to our colour range, which is from 0...4*256
      int val = (int)((rawVal+1)/2.0*1023.0);

      int r = 0;
      if(0 <= val && val < 2*256)
         r = 0;
      else if(2*256 <= val && val < 3*256)
         r = val - 2*256;
      else if(3*256 <= val && val < 4*256)
         r = 256-1;

      int g = 0;
      if(0 <= val && val < 256)
         g = val;
      else if(256 <= val && val < 3*256)
         g = 256-1;
      else if(3*256 <= val && val < 4*256)
         g = 4*256-1 -val;

      int b = 0;
      if(0 <= val && val < 256)
         b = 256-1;
      else if(256 <= val && val < 2*256)
         b = 2*256-1-val;
      else if(2*256 <= val && val < 4*256)
         b = 0;

      return new Color(r,g,b);
   }


   /**
	only for testing purposes
	*/
   public static void main(String[] args) {

      System.out.println(getColor(-0.1));
      javax.swing.JFrame f = new javax.swing.JFrame();
      javax.swing.JPanel p = new javax.swing.JPanel() {
         protected void paintComponent(java.awt.Graphics g) {
           for(int i = 0; i < (4*256); i++) {

            g.setColor(Utilities.getColor( 2D*i/1023D -1 ));
//System.out.println(g.getColor());
            g.fillRect(i, 0, 1, 10);
           }

         }
      };
      f.setSize(4*256, 10);
      f.add(p);

      f.setVisible(true);
      f.repaint();
      
   }
}

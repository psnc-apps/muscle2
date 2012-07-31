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

import javax.swing.JFrame;

/**
utility class to run the temperature solver as a stand alone app (without any MUSCLE involved)
@author Jan Hegewald
*/
public class Main {

   public static void main(String[] args) {
      JFrame frame = new JFrame("Laplace");
      frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

 		int nx = 200;
		int ny = 50;
		int steps = 10000;
      
		Temperature t = new Temperature(nx, ny, 2);
		frame.add(t.getGUI());
		
		frame.pack();
      frame.setVisible(true);

      t.run(steps);
   }

}

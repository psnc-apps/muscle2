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

package muscle.gui;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;


/**
gui of the ProgressFilter
@author Jan Hegewald
*/
public class ProgressMeter {

	int stepCount;
	ProgressPanel gui;


	//
	public ProgressMeter(String title) {

		JFrame f = new JFrame(title);
		f.setSize(300, 200);
		gui = new ProgressPanel();
		gui.stepSpinner.setModel(new SpinnerModel());
//		gui.progressBar.setIndeterminate(true); // we do not know the length of our task
// indeterminate does not work paint strings, which would make setStringPainted useless
		gui.progressBar.setStringPainted(true);
		f.getContentPane().add(gui);
		f.pack();
		// disable closing the window
		f.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		f.setVisible(true);

	}


	//
	public void increment() {
	
		stepCount ++;
		gui.progressBar.setString(Integer.toString(stepCount));

		if(shouldPause(stepCount)) {
			gui.pauseButton.setSelected(true);
			while(gui.pauseButton.isSelected()) {
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}


	//
	public boolean shouldPause(int increment) {
		
		if(gui.pauseButton.isSelected())
			return true;
		
		int stepRate = (Integer)gui.stepSpinner.getValue();
		if(stepRate == 0) // do not stop automatically
			return false;
		if(increment % stepRate == 0)
			return true;
			
		return false;
	}


	static private class SpinnerModel extends SpinnerNumberModel {
		
		public SpinnerModel() {
		
			setMinimum(0);
			setValue(1);
		}
	}
}

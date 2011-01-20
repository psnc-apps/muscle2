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
		this.gui = new ProgressPanel();
		this.gui.stepSpinner.setModel(new SpinnerModel());
//		gui.progressBar.setIndeterminate(true); // we do not know the length of our task
// indeterminate does not work paint strings, which would make setStringPainted useless
		this.gui.progressBar.setStringPainted(true);
		f.getContentPane().add(this.gui);
		f.pack();
		// disable closing the window
		f.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		f.setVisible(true);

	}


	//
	public void increment() {

		this.stepCount ++;
		this.gui.progressBar.setString(Integer.toString(this.stepCount));

		if(this.shouldPause(this.stepCount)) {
			this.gui.pauseButton.setSelected(true);
			while(this.gui.pauseButton.isSelected()) {
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

		if(this.gui.pauseButton.isSelected()) {
			return true;
		}

		int stepRate = (Integer)this.gui.stepSpinner.getValue();
		if(stepRate == 0) {
			return false;
		}
		if(increment % stepRate == 0) {
			return true;
		}

		return false;
	}


	static private class SpinnerModel extends SpinnerNumberModel {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		public SpinnerModel() {

			this.setMinimum(0);
			this.setValue(1);
		}
	}
}

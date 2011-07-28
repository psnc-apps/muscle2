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

package utilities;

import java.util.Timer;
import java.util.TimerTask;


/**
performs a delayed task after action has been called, specify delay in ms
@author Jan Hegewald
*/
abstract public class DelayedAction {
	
	private final long DELAY;
	Timer timer;
	
	public DelayedAction(long newDelay) {

		DELAY = newDelay;
	}
	
	abstract public void doAction();
	
	public void clear() {
		
		if( timer != null ) {
			timer.cancel();
			timer = null;
		}
	}
	
	public void action() {
		
		if( timer == null ) {
			timer = new Timer();
			TimerTask timerTask = new TimerTask() {
				public void run() {
					// now we should do the real flush
					DelayedAction.this.doAction();
					DelayedAction.this.clear();
				}
			};
			timer.schedule(timerTask, DELAY);
		}
	}
}

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
package muscle.util;

import java.text.DecimalFormat;

/**
 * An unsynchronized timer, based on a stopwatch.
 * It reports wall-clock time for internal use only. It is not comparable between processors or machines, only
 * within a single execution.
 * @author Joris Borgdorff
 */
public class Timer {
	private long baseTime;
	private long pauseTime;
	private Timer pauseTimer;
	private boolean paused;
	private final static DecimalFormat doubleFormat = new DecimalFormat("#.####");

	public Timer() {
		baseTime = getTime();
		paused = false;
	}

	/**
	 * Reset the timer. The timer will start running again immediately after calling reset.
	 * @return current value of the timer
	 */
	public long reset() {
		long diff = nanosec();
		baseTime += diff + pauseTime;
		pauseTime = 0;
		paused = false;

		return diff;
	}

	/**
	 * Pause the timer. May be called multiple times. Has no effect on a paused timer.
	 */
	public void pause() {
		if (!paused) {
			if (pauseTimer == null) {
				pauseTimer = new Timer();
			} else {
				pauseTimer.reset();
			}
			paused = true;
		}
	}

	/**
	 * Remove the pause from a timer. Has no effect if the timer is running.
	 */
	public void unpause() {
		if (paused) {
			pauseTime += pauseTimer.nanosec();
			paused = false;
		}
	}
	
	/**
	 * Whether the timer is paused.
	 * @return true if paused 
	 */
	public boolean isPaused() {
		return paused;
	}

	/**
	 * Returns a string representation of the current time. This resets the timer.
	 * @return floating point time appended with " s" for second.
	 */
	@Override
	public String toString() {
		return toString(reset()) + " s";
	}

	/**
	 * Returns a string representation of a given number of nanoseconds.
	 * @param diff time in nanoseconds
	 * @return four-decimal time in seconds
	 */
	public static String toString(long diff) {
		return doubleFormat.format((diff / 100000)/10000.d);
	}
	
	/**
	 * Print a message to standard out with the current time. This resets the timer.
	 * @param msg Message to print
	 */
	public void println(String msg) {
		System.out.println(msg + " (" + toString() + ")");
	}

	/** The current time of the timer in milliseconds.
	 * @return time in milliseconds
	 */
	public long millisec() {
		return nanosec() / 1000000;
	}

	/** The current time of the timer in nanoseconds.
	 * @return time in nanoseconds
	 */
	public long nanosec() {
		long newTime = getTime();

		boolean wasPaused = paused;
		if (paused) {
			unpause();
		}
		long diff = newTime - baseTime - pauseTime;
		if (wasPaused) {
			pause();
		}

		return diff;
	}

	/**
	 * Get the starting time of the timer, or the time of the last reset.
	 * @return time of last reset or time of initialization
	 */
	public long getBaseTime() {
		return baseTime;
	}

	/**
	 * Set the starting time to a certain value. If the timer was paused, it will no longer be paused
	 * @param time time in nanoseconds
	 */
	public void setTime(long time) {
		this.unpause();
		this.baseTime = time;
		this.pauseTime = 0;
	}

	/**
	 * Get the current time in nanoseconds. Override for different time standards. Not comparable
	 * between machines or processors.
	 * @return time in nanoseconds
	 */
	protected long getTime() {
		return System.nanoTime();
	}
}

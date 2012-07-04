/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.util;

/**
 *
 * @author Joris Borgdorff
 */
public class SynchronizedTimer extends Timer {
	/**
	 * Reset the timer and returns its current value. The timer will start running again immediately after calling reset.
	 */
	public synchronized long reset() {
		return super.reset();
	}

	/**
	 * Pause the timer. May be called multiple times. Has no effect on a paused timer.
	 */
	public synchronized void pause() {
		super.pause();
	}

	/**
	 * Remove the pause from a timer. Has no effect if the timer is running.
	 */
	public synchronized void unpause() {
		super.unpause();
	}
	
	/**
	 * Whether the timer is paused.
	 */
	public synchronized boolean isPaused() {
		return super.isPaused();
	}

	/** The current time of the timer in nanoseconds. */
	public synchronized long nanosec() {
		return super.nanosec();
	}

	/**
	 * Get the starting time of the timer, or the time of the last reset.
	 */
	public synchronized long getBaseTime() {
		return super.getBaseTime();
	}

	/**
	 * Set the starting time to a certain value. If the timer was paused, it will no longer be paused
	 */
	public synchronized void setTime(long time) {
		super.setTime(time);
	}
}
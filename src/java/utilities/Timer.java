/*
 * 
 */
package utilities;

import java.text.DecimalFormat;

/**
 * An unsynchronized timer, based on a stopwatch.
 * It reports wall-clock time.
 * @author Joris Borgdorff
 */
public class Timer {
	private long baseTime;
	private long pauseTime;
	private Timer pauseTimer;
	private boolean paused;
	private final static double MULTIPLIER = 1000000; // Nanosecond precision
	private final static DecimalFormat doubleFormat = new DecimalFormat("#.#### s");

	public Timer() {
		baseTime = getTime();
		paused = false;
	}

	/**
	 * Reset the timer and returns its current value. The timer will start running again immediately after calling reset.
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
	 */
	public boolean isPaused() {
		return paused;
	}

	/**
	 * Returns a string representation of the current time. This resets the timer.
	 */
	@Override
	public String toString() {
		return toString(reset());
	}

	/**
	 * Returns a string representation of a given number of nanoseconds.
	 */
	public static String toString(long diff) {
		return doubleFormat.format(diff / MULTIPLIER);
	}

	/**
	 * Print a message to standard out with the current time. This resets the timer.
	 * @param msg Message to print
	 */
	public void println(String msg) {
		System.out.println(msg + " (" + toString() + ")");
	}

	/** The current time of the timer in milliseconds. */
	public long millisec() {
		return nanosec() / 1000;
	}

	/** The current time of the timer in nanoseconds. */
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
	 */
	public long getBaseTime() {
		return baseTime;
	}

	/**
	 * Set the starting time to a certain value. If the timer was paused, it will no longer be paused
	 */
	public void setTime(long time) {
		this.unpause();
		this.baseTime = time;
		this.pauseTime = 0;
	}

	/**
	 * Get the current time in nanoseconds. Override for different time standards.
	 */
	protected long getTime() {
		return System.nanoTime();
	}
}

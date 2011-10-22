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

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
helper class to measure cpu/wallclock times for the jvm or single threads
@author Jan Hegewald
 */
public class Timing {
	/**
	thread CPU time in nanoseconds (user+system)
	 */
	public static long getThreadCpuTime() {
		ThreadMXBean tBean = ManagementFactory.getThreadMXBean();
		return tBean.isCurrentThreadCpuTimeSupported() ? tBean.getCurrentThreadCpuTime() : 0L;
	}

	/**
	thread user time in nanoseconds
	 */
	public static long getThreadUserTime() {
		ThreadMXBean tBean = ManagementFactory.getThreadMXBean();
		return tBean.isCurrentThreadCpuTimeSupported() ? tBean.getCurrentThreadUserTime() : 0L;
	}

	/**
	thread system time in nanoseconds
	 */
	public static long getThreadSystemTime() {
		ThreadMXBean tBean = ManagementFactory.getThreadMXBean();
		return tBean.isCurrentThreadCpuTimeSupported() ? (tBean.getCurrentThreadCpuTime() - tBean.getCurrentThreadUserTime()) : 0L;
	}

	/**
	JVM CPU time in nanoseconds
	 */
	public static long getJVMCpuTime() {
		ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
		long cputime = 0L;

		if (threadBean.isThreadCpuTimeSupported()) {
			long[] ids = threadBean.getAllThreadIds();
			for (int i = 0; i < ids.length; i++) {
				cputime += threadBean.getThreadCpuTime(ids[i]);
			}
		}

		return cputime;
	}

	private final Timer threadTimer;
	private final Timer systemTimer;

	public Timing() {
		threadTimer = new ThreadCpuTimer();
		systemTimer = new Timer();
	}

	public void stop() {
		threadTimer.pause();
		systemTimer.pause();
	}

	public boolean isCounting() {
		return !threadTimer.isPaused();
	}

	public String toString() {
		long threadTime = threadTimer.reset();
		long totalTime = systemTimer.reset();
		float percent = 100.0f;
		if (totalTime > 0) {
			percent = threadTime / (float) totalTime * 100.0f;
		}
		return Timer.toString(threadTime) + "/" + Timer.toString(totalTime) + " = " + percent + " %";
	}

	public static void main(String[] args) {

		Timing t = new Timing();
		System.out.println(t);
		for (int i = 0; i < 10000042; i++) {
			String s = i + "foo" + i / 42.42;
		}
		System.out.println(t);
	}
}

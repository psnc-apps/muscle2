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
package muscle.util.concurrency;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import muscle.util.Timer;

/**
 * An unsynchronized timer that measures the CPU time that the current thread is taking.
 * @author Joris Borgdorff
 */
public class ThreadCpuTimer extends Timer {
	private final ThreadMXBean threadInfo;
	
	public ThreadCpuTimer() {
		ThreadMXBean tBean = null;
		tBean = ManagementFactory.getThreadMXBean();
		if (tBean.isThreadCpuTimeSupported()) {
			if (!tBean.isThreadCpuTimeEnabled()) {
				tBean.setThreadCpuTimeEnabled(true);
			}
		}
		else {
			tBean = null;
		}
		threadInfo = tBean;
	}
	
	protected long getTime() {
		if (threadInfo != null) {
			return threadInfo.getCurrentThreadCpuTime();
		}
		else {
			return super.getTime();
		}
	}
}

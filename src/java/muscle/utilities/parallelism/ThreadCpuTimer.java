/*
 * 
 */
package muscle.utilities.parallelism;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import utilities.Timer;

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

package muscle.core.kernel;

import muscle.util.concurrency.NamedRunnable;

/**
 *
 * @author Joris Borgdorff
 */
public interface InstanceControllerListener {
	public void isFinished(NamedRunnable ic);
	public void addInstanceController(NamedRunnable instance, Thread fromThread);
}

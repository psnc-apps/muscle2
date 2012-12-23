/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.client.instance;

import muscle.core.kernel.InstanceControllerListener;
import muscle.util.concurrency.NamedRunnable;

/**
 *
 * @author Joris Borgdorff
 */
public class SingleControllerRunner implements NamedRunnable {
	private final PassiveInstanceController controller;
	private final InstanceControllerListener listener;

	SingleControllerRunner(PassiveInstanceController controller, InstanceControllerListener listener) {
		this.controller = controller;
		this.listener = listener;
	}

	@Override
	public String getName() {
		return controller.getName();
	}

	@Override
	public void run() {
		while (!controller.isDisposed()) {
			controller.step();
		}
	}

	@Override
	public void dispose() {
		controller.dispose();
		listener.isFinished(this);
	}

	@Override
	public boolean isDisposed() {
		return controller.isDisposed();
	}
}

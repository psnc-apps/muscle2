/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.client.instance;

import eu.mapperproject.jmml.util.FastArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.client.communication.PortFactory;
import muscle.core.kernel.InstanceControllerListener;
import muscle.id.InstanceClass;
import muscle.id.ResolverFactory;
import muscle.util.concurrency.NamedRunnable;

/**
 *
 * @author Joris Borgdorff
 */
public class MultiControllerRunner implements NamedRunnable {
	private final String name;
	private final PortFactory portFactory;
	private final ResolverFactory rf;
	private final InstanceControllerListener listener;
	private final List<InstanceClass> instances;
	private final List<PassiveInstanceController> controllers;
	private boolean isDone;
	private static final Logger logger = Logger.getLogger(MultiControllerRunner.class.getName());
	
	public MultiControllerRunner(List<InstanceClass> instances, InstanceControllerListener listener, ResolverFactory rf, PortFactory portFactory) {
		this.name = "MultiController-" + instances.iterator().next();
		this.instances = instances;
		this.listener = listener;
		this.rf = rf;
		this.portFactory = portFactory;
		this.controllers = new FastArrayList<PassiveInstanceController>(instances.size());
		this.isDone = false;
	}
	
	@Override
	public void run() {
		int threaded = 0;
		for (InstanceClass ic : instances) {
			if (this.isDisposed()) {
				return;
			}
			PassiveInstanceController controller = new PassiveInstanceController(this, ic, listener, rf, portFactory);
			if (controller.init()) {
				if (controller.canStep() || threaded == instances.size() - 1) {
					synchronized (controllers) {
						this.controllers.add(controller);
					}
				} else {
					SingleControllerRunner runner = new SingleControllerRunner(controller, listener);
					Thread t = new Thread(runner, runner.getName());
					listener.addInstanceController(runner, t);
					t.start();
				}
			} else {
				dispose();
			}
		}
		int current = 1, active;
		while (current > 0) {
			current = 0; active = 0;
			for (PassiveInstanceController controller : this.controllers) {
				if (this.isDisposed()) {
					return;
				}
				if (!controller.isDisposed()) {
					current++;
					if (controller.readyForStep()) {
						active++;
						controller.step();
					}
				}
			}
			if (active == 0 && current > 0) {
				logger.log(Level.FINE, "No activity... (with {0} submodels in under control)", current);
				try {
					Thread.sleep(50);
				} catch (InterruptedException ex) {
					Logger.getLogger(MultiControllerRunner.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
		dispose();
	}
	
	@Override
	public void dispose() {
		synchronized (this) {
			if (this.isDone) {
				return;
			}
			this.isDone = true;
		}
		synchronized (controllers) {
			for (PassiveInstanceController controller : this.controllers) {
				controller.dispose();
			}
		}
		listener.isFinished(this);
	}

	@Override
	public synchronized boolean isDisposed() {
		return this.isDone;
	}

	@Override
	public String getName() {
		return name;
	}
}

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
package muscle.client.instance;

import eu.mapperproject.jmml.util.FastArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.client.communication.PortFactory;
import muscle.core.ConnectionScheme;
import muscle.core.kernel.InstanceControllerListener;
import muscle.id.InstanceClass;
import muscle.id.Resolver;
import muscle.util.concurrency.NamedRunnable;

/**
 * Runs multiple instance controllers in a single thread.
 * @author Joris Borgdorff
 */
public class MultiControllerRunner implements NamedRunnable {
	private final String name;
	private PortFactory portFactory;
	private Resolver resolver;
	private final InstanceControllerListener listener;
	private List<InstanceClass> instances;
	private final List<PassiveInstanceController> controllers;
	private boolean isDone;
	private static final Logger logger = Logger.getLogger(MultiControllerRunner.class.getName());
	private final static int SLEEP_MS = Integer.valueOf(System.getProperty("muscle.client.sleep_when_idle", "0"));
	private ConnectionScheme connectionScheme;
	
	public MultiControllerRunner(List<InstanceClass> instances, InstanceControllerListener listener, Resolver res, PortFactory portFactory, ConnectionScheme cs) {
		this.name = "MultiController-" + instances.iterator().next();
		this.instances = instances;
		this.listener = listener;
		this.resolver = res;
		this.portFactory = portFactory;
		this.controllers = new FastArrayList<PassiveInstanceController>(instances.size());
		this.isDone = false;
		this.connectionScheme = cs;
	}
	
	private void init() {
		int threaded = 0;
		for (InstanceClass ic : instances) {
			if (this.isDisposed()) {
				return;
			}
			PassiveInstanceController controller = new PassiveInstanceController(this, ic, listener, resolver, portFactory, connectionScheme);
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
					threaded++;
				}
			} else {
				dispose();
			}
		}
		this.connectionScheme = null;
		this.resolver = null;
		this.portFactory = null;
		this.instances = null;
	}
	
	@Override
	public void run() {
		this.init();
		
		final Thread currentThread = Thread.currentThread();
		final int defaultPriority = currentThread.getPriority();
		boolean isLowPriority = false;
		int active = 1;
		while (!this.controllers.isEmpty()) {
			if (active == 0) {
				logger.log(Level.FINE, "No activity... (with {0} submodels in under control)", this.controllers.size());
				currentThread.setPriority(Thread.MIN_PRIORITY);
				isLowPriority = true;
				if (SLEEP_MS == 0) {
					// Nothing is happening, see if another thread can resolve the lock
					Thread.yield();
				} else {
					try {
						// Nothing is happening, wait for a few milliseconds for messages to arrive
						Thread.sleep(SLEEP_MS);
					} catch (InterruptedException ex) {
						//Do nothing
					}
				}
			}
			
			active = 0;
			Iterator<PassiveInstanceController> i = this.controllers.iterator();
			while (i.hasNext()) {
				if (this.isDisposed()) {
					return;
				}
				PassiveInstanceController controller = i.next();
				if (controller.isDisposed()) {
					i.remove();
				} else if (controller.readyForStep()) {
					if (isLowPriority) {
						currentThread.setPriority(defaultPriority);
						isLowPriority = false;
					}
					active++;
					controller.step();
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

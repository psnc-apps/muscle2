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
package muscle.client;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.ContainerController;
import jade.wrapper.PlatformController;
import java.util.LinkedList;
import java.util.List;

/**
encapsulate booting of JADE and tearing down JADE
@author Jan Hegewald
 */
public class JADE {

	private Profile containerProfile;
	boolean isMain = false;
	private ContainerController jadeContainer;
	private String containerName; // jadeContainer.getContainerName() not available in shutdownhook anymore, so er store it before
	private String platformName; // jadeContainer.getPlatformName() not available in shutdownhook anymore, so er store it before
	List<Thread> hooks = new LinkedList<Thread>();

	public JADE(String[] args) {
		// note: it seems like loggers can not be used within shutdown hooks
		hooks.add(new JVMHook());
		Runtime.getRuntime().addShutdownHook(hooks.get(hooks.size() - 1));

		// boot jade without relying on jade.Boot.main
		containerProfile = new ProfileImpl(jade.Boot.parseCmdLineArgs(args));
		containerProfile.setParameter("jade_core_messaging_MessageManager_maxqueuesize","500000000");
		isMain = containerProfile.getBooleanProperty(Profile.MAIN, false);
		jade.core.Runtime jadeRuntime = jade.core.Runtime.instance();
		jadeRuntime.setCloseVM(true);
		jadeRuntime.invokeOnTermination(new Thread() {
			public void run() {
				System.out.println("container " + containerName + "@" + platformName + " has been terminated");
				jadeContainer = null;
			}
		});

		if (containerProfile.getBooleanProperty(Profile.MAIN, true)) {
			jadeContainer = jadeRuntime.createMainContainer(containerProfile);
		} else {
			jadeContainer = jadeRuntime.createAgentContainer(containerProfile);
		}

		try {
			containerName = jadeContainer.getContainerName();
		} catch (jade.wrapper.ControllerException e) {
			e.printStackTrace();
		}
		platformName = jadeContainer.getPlatformName();
		System.out.println("started jade");
	}

	public ContainerController getContainerController() {
		return jadeContainer;
	}

	public List<Thread> getShutdownHooks() {
		return hooks;
	}

	private class JVMHook extends Thread {
		public void run() {
			if (jadeContainer != null) {

				if (isMain) { // kill the platform

					// quit whole platform via AMS/QuickQuitUtilityAgent
					try {
						muscle.client.behaviour.QuickQuitUtilityAgent.launch(jadeContainer);
					} catch (muscle.exception.SpawnAgentException e) {

						// try to kill platform
						PlatformController platformController = null;
						try {
							platformController = jadeContainer.getPlatformController();
						} catch (jade.wrapper.ControllerException ee) {
							throw new RuntimeException(ee);
						}
						try {
							platformController.kill();
						} catch (jade.wrapper.ControllerException ee) {
							ee.printStackTrace();
						}
					}
				} else { // kill this container
					try {
						jadeContainer.kill();
					} catch (jade.wrapper.StaleProxyException e) {
						e.printStackTrace();
					}
				}
				System.out.println("waiting for container " + containerName + "@" + platformName + " to terminate");
				while (jadeContainer != null) {
					try {
						sleep(100);
					} catch (java.lang.InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
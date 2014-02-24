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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.client.instance;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.client.LocalManager;
import muscle.client.communication.PortFactory;
import muscle.core.ConduitEntranceController;
import muscle.core.ConnectionScheme;
import muscle.core.kernel.InstanceControllerListener;
import muscle.exception.MUSCLEConduitExhaustedException;
import muscle.exception.MUSCLEDatatypeException;
import muscle.id.InstanceClass;
import muscle.id.Resolver;
import muscle.util.concurrency.NamedRunnable;
import muscle.util.logging.ActivityListener;
import muscle.util.logging.ActivityProtocol;

/**
 * Runs a submodel in single steps
 * @author Joris Borgdorff
 */
public class PassiveInstanceController extends AbstractInstanceController {
	private final static Logger logger = Logger.getLogger(PassiveInstanceController.class.getName());
	private final NamedRunnable runner;
	private boolean isInitializing;
	
	public PassiveInstanceController(NamedRunnable runner, InstanceClass instanceClass, InstanceControllerListener listener, Resolver res, PortFactory portFactory, ConnectionScheme cs, ActivityListener actLogger) {
		super(instanceClass, listener, res, portFactory, cs, actLogger);
		this.runner = runner;
		this.isInitializing = true;
	}
	
	public boolean init() {
		super.init();
		
		try {
			instance.beforeExecute();
		} catch (Exception ex) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			try {
				pw.close(); sw.close();
			} catch (IOException ex1) {
				Logger.getLogger(ThreadedInstanceController.class.getName()).log(Level.SEVERE, null, ex1);
			}
			logger.log(Level.SEVERE, "{0} was halted due to an error.\n====TRACE====\n{1}==END TRACE==", new Object[]{getName(), sw});
			LocalManager.getInstance().shutdown(9);
		}
		
		if (!register()) {
			logger.log(Level.SEVERE, "Could not register {0}; it may already have been registered. {0} was halted.", getName());
			this.disposeNoDeregister(false);
			return false;
		}
		if (actLogger != null) actLogger.activity(ActivityProtocol.START, id);
		return true;
	}
	
	public void step() throws OutOfMemoryError {
		try {
			if (isInitializing) {
				instance.connectPortals();
				propagate();

				// log info about this controller
				if (logger.isLoggable(Level.INFO)) {
					logger.log(Level.INFO, instance.infoText());
				}

				logger.log(Level.INFO, "{0}: executing", getName());
				beforeExecute();
				this.isInitializing = false;
			} else if (!instance.steppingFinished()) {
				try {
					instance.step();
				} catch (MUSCLEConduitExhaustedException ex) {
					logger.log(Level.SEVERE, getName() + " was prematurely halted, by trying to receive a message from a stopped submodel.", ex);
					LocalManager.getInstance().shutdown(6);
				} catch (MUSCLEDatatypeException ex) {
					logger.log(Level.SEVERE, getName() + " communicated a wrong data type. Check the coupling.", ex);
					LocalManager.getInstance().shutdown(7);
				}
			} else {
				this.afterExecute();
				logger.log(Level.INFO, "{0}: finished", getName());
				dispose();
			}
		} catch (Exception ex) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			try {
				pw.close(); sw.close();
			} catch (IOException ex1) {
				Logger.getLogger(ThreadedInstanceController.class.getName()).log(Level.SEVERE, null, ex1);
			}
			logger.log(Level.SEVERE, "{0} was halted due to an error.\n====TRACE====\n{1}==END TRACE==", new Object[]{getName(), sw});
			LocalManager.getInstance().shutdown(8);
		}
	}
	
	public boolean readyForStep() {
		if (isInitializing) {
			return true;
		} else if (instance.steppingFinished()) {
			if (isExecuting()) {
				for (ConduitEntranceController ec : entrances) {
					if (!ec.isEmpty()) {
						return false;
					}
				}
			}
			return true;
		} else {
			return instance.readyForStep();
		}
	}

	@Override
	public NamedRunnable getRunner() {
		return this.runner;
	}
	
	public boolean canStep() {
		return instance.canStep();
	}
}

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
import muscle.core.kernel.InstanceControllerListener;
import muscle.exception.MUSCLEConduitExhaustedException;
import muscle.exception.MUSCLEDatatypeException;
import muscle.id.InstanceClass;
import muscle.id.ResolverFactory;
import muscle.util.concurrency.NamedRunnable;

/**
 * Runs a submodel in single steps
 * @author Joris Borgdorff
 */
public class PassiveInstanceController extends AbstractInstanceController {
	private final static Logger logger = Logger.getLogger(PassiveInstanceController.class.getName());
	private final NamedRunnable runner;
	private boolean isInitializing;
	
	public PassiveInstanceController(NamedRunnable runner, InstanceClass instanceClass, InstanceControllerListener listener, ResolverFactory rf, PortFactory portFactory) {
		super(instanceClass, listener, rf, portFactory);
		this.runner = runner;
		this.isInitializing = true;
	}
	
	public boolean init() {
		super.init();
		
		instance.beforeExecute();

		if (!register()) {
			logger.log(Level.SEVERE, "Could not register {0}; it may already have been registered. {0} was halted.", getName());
			if (!this.isDisposed()) {
				this.disposeNoDeregister();
			}
			return false;
		}
		return true;
	}
	
	public void step() throws OutOfMemoryError {
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
		} else {
			this.afterExecute();
			logger.log(Level.INFO, "{0}: finished", getName());
			dispose();
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

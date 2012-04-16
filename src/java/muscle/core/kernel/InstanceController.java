/*
 * 
 */
package muscle.core.kernel;

import java.io.Serializable;
import muscle.core.ConduitEntranceController;
import muscle.core.ConduitExitController;
import muscle.core.ident.Identifiable;

/**
 * @author Joris Borgdorff
 */
public interface InstanceController extends Identifiable, Runnable {
	public String getLocalName();
	public <T extends Serializable> void addConduitEntrance(ConduitEntranceController<T> s);
	public <T extends Serializable> void addConduitExit(ConduitExitController<T> s);
	/** Dispose of the instance. If it was centrally registered, it will deregister itself. */
	public void dispose();
	public boolean isExecuting();
}

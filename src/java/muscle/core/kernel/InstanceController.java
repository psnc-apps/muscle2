/*
 * 
 */
package muscle.core.kernel;

import java.io.Serializable;
import muscle.core.ConduitEntranceController;
import muscle.core.ConduitExitController;
import muscle.core.DataTemplate;
import muscle.id.Identifiable;

/**
 * @author Joris Borgdorff
 */
public interface InstanceController extends Identifiable, Runnable {
	public String getLocalName();
	public <T extends Serializable> ConduitEntranceController<T> createConduitEntrance(String portalName, DataTemplate newDataTemplate);
	public <T extends Serializable> ConduitExitController<T> createConduitExit(String portalName, DataTemplate newDataTemplate);
	/** Dispose of the instance. If it was centrally registered, it will deregister itself. */
	public void dispose();
	public boolean isExecuting();
}

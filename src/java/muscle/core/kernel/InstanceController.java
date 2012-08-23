/*
 * 
 */
package muscle.core.kernel;

import java.io.Serializable;
import muscle.core.ConduitEntranceController;
import muscle.core.ConduitExitController;
import muscle.core.DataTemplate;
import muscle.id.Identifiable;
import muscle.util.concurrency.Disposable;

/**
 * @author Joris Borgdorff
 */
public interface InstanceController extends Identifiable, Runnable, Disposable {
	public String getLocalName();
	public <T extends Serializable> ConduitEntranceController<T> createConduitEntrance(boolean threaded, String portalName, DataTemplate newDataTemplate);
	public <T extends Serializable> ConduitExitController<T> createConduitExit(boolean threaded, String portalName, DataTemplate newDataTemplate);
	public boolean isExecuting();
}

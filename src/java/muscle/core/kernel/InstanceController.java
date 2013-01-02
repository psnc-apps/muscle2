/*
 * 
 */
package muscle.core.kernel;

import java.io.Serializable;
import java.util.Map;
import muscle.core.ConduitDescription;
import muscle.core.ConduitEntranceController;
import muscle.core.ConduitExitController;
import muscle.core.DataTemplate;
import muscle.id.Identifiable;
import muscle.util.concurrency.Disposable;
import muscle.util.concurrency.NamedRunnable;

/**
 * @author Joris Borgdorff
 */
public interface InstanceController extends Identifiable, Disposable {
	public NamedRunnable getRunner();
	public String getName();
	public <T extends Serializable> ConduitEntranceController<T> createConduitEntrance(boolean threaded, boolean shared, String portalName, DataTemplate newDataTemplate);
	public <T extends Serializable> ConduitExitController<T> createConduitExit(boolean threaded, String portalName, DataTemplate newDataTemplate);
	public boolean isExecuting();
	public void fatalException(Throwable ex);
	public Map<String, ConduitDescription> getEntranceDescriptions();
	public Map<String, ConduitDescription> getExitDescriptions();
}

/*
 * 
 */
package muscle.core.kernel;

import muscle.core.ConduitEntranceController;
import muscle.core.ConduitExitController;
import muscle.core.ident.Identifiable;

/**
 * @author Joris Borgdorff
 */
public interface InstanceController extends Identifiable {
	public String getLocalName();
	public <T> void addSink(ConduitEntranceController<T> s);
	public <T> void addSource(ConduitExitController<T> s);
}

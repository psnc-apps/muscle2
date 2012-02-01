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
public interface InstanceController extends Identifiable {
	public String getLocalName();
	public <T extends Serializable> void addSink(ConduitEntranceController<T> s);
	public <T extends Serializable> void addSource(ConduitExitController<T> s);
}

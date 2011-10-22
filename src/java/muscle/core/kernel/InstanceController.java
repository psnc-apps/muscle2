/*
 * 
 */
package muscle.core.kernel;

import java.util.logging.Logger;
import muscle.core.ConduitEntranceController;
import muscle.core.ConduitExitController;
import muscle.core.ident.Identifier;

/**
 * @author Joris Borgdorff
 */
public interface InstanceController {
	public String getLocalName();
	public void addSink(ConduitEntranceController<?> s);
	public void addSource(ConduitExitController<?> s);
	public Identifier getID();
	public Logger getLogger();
}

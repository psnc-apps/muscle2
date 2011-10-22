/*
 * 
 */
package muscle.core.kernel;

import java.util.logging.Logger;
import muscle.core.ConduitEntranceController;
import muscle.core.ConduitExitController;
import muscle.core.ident.Identifier;
import muscle.core.messaging.Message;

/**
 * @author Joris Borgdorff
 */
public interface InstanceController {
	public String getLocalName();
	public void addSink(ConduitEntranceController<?> s);
	public void addSource(ConduitExitController<?> s);
	public Identifier getID();
	public Logger getLogger();
	public <E> void sendData(E data);
	public <E> void sendMessage(Message<E> dmsg);
	//public <E> Message<E> receiveMessage();
}

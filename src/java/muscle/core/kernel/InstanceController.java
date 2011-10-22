/*
 * 
 */
package muscle.core.kernel;

import java.io.Serializable;
import java.util.logging.Logger;
import muscle.core.ident.Identifier;
import muscle.core.messaging.Message;
import muscle.core.messaging.RemoteDataSinkHead;
import muscle.core.messaging.RemoteDataSinkTail;
import muscle.core.messaging.jade.DataMessage;

/**
 * @author Joris Borgdorff
 */
public interface InstanceController {
	public String getLocalName();
	public void addSink(RemoteDataSinkHead<DataMessage<?>> s);
	public void addSource(RemoteDataSinkTail<DataMessage<?>> s);
	public Identifier getID();
	public Logger getLogger();
	public <E extends Serializable> void sendData(E data);
	public <E extends Serializable> void sendMessage(Message<E> dmsg);
}

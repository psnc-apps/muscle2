/*
 * 
 */
package muscle.core.kernel;

<<<<<<< HEAD
import muscle.core.ConduitEntranceController;
import muscle.core.ConduitExitController;
import muscle.core.ident.Identifiable;
=======
import java.io.Serializable;
import java.util.logging.Logger;
import muscle.core.ident.Identifier;
import muscle.core.messaging.Message;
import muscle.core.messaging.RemoteDataSinkHead;
import muscle.core.messaging.RemoteDataSinkTail;
import muscle.core.messaging.jade.DataMessage;
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0

/**
 * @author Joris Borgdorff
 */
<<<<<<< HEAD
public interface InstanceController extends Identifiable {
	public String getLocalName();
	public <T> void addSink(ConduitEntranceController<T> s);
	public <T> void addSource(ConduitExitController<T> s);
=======
public interface InstanceController {
	public String getLocalName();
	public void addSink(RemoteDataSinkHead<DataMessage<?>> s);
	public void addSource(RemoteDataSinkTail<DataMessage<?>> s);
	public Identifier getID();
	public Logger getLogger();
	public <E extends Serializable> void sendData(E data);
	public <E extends Serializable> void sendMessage(Message<E> dmsg);
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0
}

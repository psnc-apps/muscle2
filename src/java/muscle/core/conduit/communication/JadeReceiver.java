/*
 * 
 */
package muscle.core.conduit.communication;

import jade.lang.acl.ACLMessage;
import muscle.core.ident.PortalID;
import muscle.core.messaging.Message;
import muscle.core.messaging.jade.ObservationMessage;
import muscle.core.messaging.serialization.DataConverter;
import muscle.core.wrapper.Observation;
import muscle.exception.MUSCLERuntimeException;

/**
 *
 * @author Joris Borgdorff
 */
public class JadeReceiver<T> extends AbstractCommunicatingPoint<Message<T>, ACLMessage> implements Receiver<T, ACLMessage> {
	@Override
	public Message<T> receive() {		
		ObservationMessage<Observation<T>> dmsg = null;
		try {
			dmsg = sinkDelegate.take();
		} catch (InterruptedException ex) {
			throw new MUSCLERuntimeException(ex);
		}
				
		Observation<T> wrapper = dmsg.getData();
		T data = wrapper.getData();
		
		return data;
	}
		
	// deserialize
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		
		// do default deserialization
		in.defaultReadObject();
		
		// init transient fields
		// can not use agent logger here, because exit has no access to owner agent
		logger = muscle.logging.Logger.getLogger(ConduitExit.class);
	}
		throw new UnsupportedOperationException("Not supported yet.");
	}
}

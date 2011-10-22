/*
 * 
 */
package muscle.core.conduit.communication;

import jade.lang.acl.ACLMessage;
import muscle.core.ident.PortalID;
import muscle.core.messaging.Message;
import muscle.core.messaging.serialization.DataConverter;

/**
 *
 * @author Joris Borgdorff
 */
public class JadeReceiver<T> implements Receiver<T, ACLMessage> {
	private DataConverter<Message<T>, ACLMessage> deserializer;
	private PortalID portalID;
	
	@Override
	public void setDeserializer(DataConverter<Message<T>, ACLMessage> deserializer) {
		this.deserializer = deserializer;
	}

	@Override
	public void setTransmittingPort(PortalID id) {
		this.portalID = id;
	}

	@Override
	public Message<T> receive() {
		
		DataMessage<DataWrapper<T>> dmsg = null;
		try {
			dmsg = sinkDelegate.take();
		} catch (InterruptedException ex) {
			throw new MUSCLERuntimeException(ex);
		}
				
		DataWrapper<T> wrapper = dmsg.getData();
		T data = wrapper.getData();
		
		assert getDataTemplate().getDataClass().isInstance(data);

		increment();
		
		return data;
		
		
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

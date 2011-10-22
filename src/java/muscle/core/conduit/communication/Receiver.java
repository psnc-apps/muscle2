/*
 * 
 */
package muscle.core.conduit.communication;

<<<<<<< HEAD
import muscle.core.ident.Identifier;
import muscle.core.ident.PortalID;
=======
import muscle.core.ident.PortalID;
import muscle.core.messaging.Message;
import muscle.core.messaging.serialization.DataConverter;
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0

/**
 *
 * @author jborgdo1
 */
<<<<<<< HEAD
public interface Receiver<E, F, Q extends Identifier, P extends PortalID<Q>> extends CommunicatingPoint<E,F,Q,P> {
	/** Receives a message. Will return null if no more messages can be received */
	public E receive();
=======
public interface Receiver<E, F> {
	public void setTransmittingPort(PortalID id);
	public Message<E> receive();
	public void setDeserializer(DataConverter<Message<E>,F> deserializer);
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0
}

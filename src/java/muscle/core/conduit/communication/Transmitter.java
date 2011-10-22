/*
 * 
 */
package muscle.core.conduit.communication;

<<<<<<< HEAD
import muscle.core.ident.Identifier;
import muscle.core.ident.PortalID;
import muscle.core.messaging.Message;
import muscle.core.messaging.signal.Signal;
import muscle.core.wrapper.Observation;
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
public interface Transmitter<E,F,Q extends Identifier,P extends PortalID<Q>> extends CommunicatingPoint<Observation<E>, F, Q, P> {
	public void signal(Signal signal);
	public void transmit(Observation<E> msg);
=======
public interface Transmitter<E,F> {
	public void setReceivingPort(PortalID id);
	public void transmit(Message<E> message);
	public void setSerializer(DataConverter<E,F> serializer);
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0
}

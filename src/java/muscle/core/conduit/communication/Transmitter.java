/*
 * 
 */
package muscle.core.conduit.communication;

import muscle.core.ident.PortalID;
import muscle.core.messaging.Message;
import muscle.core.messaging.serialization.DataConverter;
import muscle.core.messaging.signal.Signal;

/**
 *
 * @author jborgdo1
 */
public interface Transmitter<E,F> {
	public void setReceivingPort(PortalID id);
	public void signal(Signal signal);
	public void transmit(Message<E> signal);
	public void setSerializer(DataConverter<E,F> serializer);
}

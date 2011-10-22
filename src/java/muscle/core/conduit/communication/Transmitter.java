/*
 * 
 */
package muscle.core.conduit.communication;

import muscle.core.ident.PortalID;
import muscle.core.messaging.Message;
import muscle.core.messaging.serialization.DataConverter;

/**
 *
 * @author jborgdo1
 */
public interface Transmitter<E,F> {
	public void setReceivingPort(PortalID id);
	public void transmit(Message<E> message);
	public void setSerializer(DataConverter<E,F> serializer);
}

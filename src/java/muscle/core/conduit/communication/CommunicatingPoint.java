/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.core.conduit.communication;

import muscle.core.ident.Identifier;
import muscle.core.ident.PortalID;
import muscle.core.messaging.serialization.DataConverter;
import muscle.utilities.parallelism.Disposable;

/**
 *
 * @author Joris Borgdorff
 */
public interface CommunicatingPoint<E,F, Q extends Identifier, P extends PortalID<Q>> extends Disposable {
	public void setComplementaryPort(P id);
	public void setDataConverter(DataConverter<E,F> serializer);
}

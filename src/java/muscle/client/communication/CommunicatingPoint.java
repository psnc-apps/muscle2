/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.client.communication;

import muscle.core.ident.Identifier;
import muscle.core.ident.PortalID;
import muscle.util.serialization.DataConverter;
import muscle.util.concurrency.Disposable;

/**
 *
 * @author Joris Borgdorff
 */
public interface CommunicatingPoint<E,F, Q extends Identifier, P extends PortalID<Q>> extends Disposable {
	public void setComplementaryPort(P id);
	public void setDataConverter(DataConverter<E,F> serializer);
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.core.conduit.communication;

import muscle.core.ident.Identifier;
import muscle.core.ident.PortalID;
import muscle.core.messaging.serialization.DataConverter;

/**
 *
 * @author Joris Borgdorff
 */
public abstract class AbstractCommunicatingPoint<E,F, Q extends Identifier, P extends PortalID<Q>> implements CommunicatingPoint<E,F,Q,P> {
	protected DataConverter<E, F> converter;
	protected P portalID;

	public void setComplementaryPort(P id) {
		this.portalID = id;
	}

	public void setDataConverter(DataConverter<E, F> converter) {
		this.converter = converter;
	}
	
	public void dispose() {}
}

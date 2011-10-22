/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.core.conduit.communication;

import muscle.core.ident.PortalID;
import muscle.core.messaging.serialization.DataConverter;

/**
 *
 * @author Joris Borgdorff
 */
public abstract class AbstractCommunicatingPoint<E,F> implements CommunicatingPoint<E,F> {
	protected DataConverter<E, F> converter;
	protected PortalID portalID;

	public void setComplementaryPort(PortalID id) {
		this.portalID = id;
	}

	public void setDataConverter(DataConverter<E, F> converter) {
		this.converter = converter;
	}	
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.client.communication;

import muscle.id.Identifier;
import muscle.id.PortalID;
import muscle.util.serialization.DataConverter;

/**
 *
 * @author Joris Borgdorff
 */
public abstract class AbstractCommunicatingPoint<E,F, Q extends Identifier, P extends PortalID<Q>> implements CommunicatingPoint<E,F,Q,P> {
	protected DataConverter<E, F> converter;
	protected P portalID;
	private boolean isDone;

	public AbstractCommunicatingPoint() {
		this.converter = null;
		this.portalID = null;
		this.isDone = false;
	}
	
	public void setComplementaryPort(P id) {
		this.portalID = id;
	}

	public void setDataConverter(DataConverter<E, F> converter) {
		this.converter = converter;
	}
	
	public synchronized void dispose() {
		this.isDone = true;
	}
	
	public synchronized boolean isDisposed() {
		return this.isDone;
	}
}

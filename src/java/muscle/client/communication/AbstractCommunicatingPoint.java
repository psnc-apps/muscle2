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
public abstract class AbstractCommunicatingPoint<E,F, Q extends Identifier, P extends PortalID<Q>> implements CommunicatingPoint {
	protected final DataConverter<E, F> converter;
	protected final P portalID;
	private boolean isDone;

	public AbstractCommunicatingPoint(DataConverter<E, F> converter, P portalID) {
		this.converter = converter;
		this.portalID = portalID;
		this.isDone = false;
	}
	
	public synchronized void dispose() {
		this.isDone = true;
	}
	
	public synchronized boolean isDisposed() {
		return this.isDone;
	}
}

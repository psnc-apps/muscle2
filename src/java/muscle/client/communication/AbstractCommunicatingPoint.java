/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.client.communication;

import muscle.id.PortalID;
import muscle.util.serialization.DataConverter;

/**
 *
 * @author Joris Borgdorff
 */
public abstract class AbstractCommunicatingPoint<E,F> implements CommunicatingPoint<E,F> {
	protected DataConverter<E, F> converter;
	protected final PortalID portalID;
	private boolean isDone;

	public AbstractCommunicatingPoint(DataConverter<E, F> converter, PortalID portalID) {
		this.converter = converter;
		this.portalID = portalID;
		this.isDone = false;
	}
	
	public void setDataConverter(DataConverter<E,F> converter) {
		this.converter = converter;
	}
	
	public synchronized void dispose() {
		this.isDone = true;
	}
	
	public synchronized boolean isDisposed() {
		return this.isDone;
	}
}

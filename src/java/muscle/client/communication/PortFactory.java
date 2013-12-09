/*
* Copyright 2008, 2009 Complex Automata Simulation Technique (COAST) consortium
* Copyright 2010-2013 Multiscale Applications on European e-Infrastructures (MAPPER) project
*
* GNU Lesser General Public License
* 
* This file is part of MUSCLE (Multiscale Coupling Library and Environment).
* 
* MUSCLE is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* MUSCLE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public License
* along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/
package muscle.client.communication;

import java.io.Serializable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.client.instance.ConduitEntranceControllerImpl;
import muscle.client.instance.ConduitExitControllerImpl;
import muscle.core.kernel.InstanceController;
import muscle.exception.ExceptionListener;
import muscle.id.Identifier;
import muscle.id.PortalID;
import muscle.id.Resolver;
import muscle.util.concurrency.Disposable;
import muscle.util.concurrency.LimitedThreadPool;
import muscle.util.concurrency.NamedCallable;

/**
 * Assigns Receivers and Transmitters to Portals.
 * 
 * @author Joris Borgdorff
 */
public abstract class PortFactory implements Disposable {
	protected final LimitedThreadPool executor;
	protected final Resolver resolver;
	protected final IncomingMessageProcessor messageProcessor;
	
	/**
	 * Assigns a Receiver to a ConduitExitController in a Thread.
	 * 
	 * By evaluating the Future that is returned, it is possible to determine when this has taken place and what the actual assigned receiver was.
	 * The call is non-blocking, however, the returned Future can be evaluated with a blocking call.
	 */
	public <T extends Serializable> Receiver<T,?> getReceiver(InstanceController ic, ConduitExitControllerImpl<T> localInstance, PortalID otherSide) {
		try {
			return this.<T>getReceiverTask(ic, localInstance, otherSide).call();
		} catch (Exception ex) {
			Logger.getLogger(PortFactory.class.getName()).log(Level.SEVERE, "Could not instantiate receiver", ex);
			return null;
		}
	}
	
	/**
	 * Assigns a Transmitter to a ConduitEntranceController in a Thread.
	 * 
	 * By evaluating the Future that is returned, it is possible to determine when this has taken place and what the actual assigned transmitter was.
	 * The call is non-blocking, however, the returned Future can be evaluated with a blocking call.
	 */
	public <T extends Serializable> Future<Transmitter<T,?>> getTransmitter(InstanceController ic, ConduitEntranceControllerImpl<T> localInstance, PortalID otherSide, boolean shared) {
		return executor.submit(this.<T>getTransmitterTask(ic, localInstance, otherSide, shared));
	}

	/**
	 * Creates a task that will assign a receiver to a ConduitExitController.
	 * 
	 * In this task, the receiver must also be added to the messageProcessor, and the otherSide might have to be resolved.
	 */
	protected abstract <T extends Serializable> NamedCallable<Receiver<T,?>> getReceiverTask(InstanceController ic, ConduitExitControllerImpl<T> localInstance, PortalID otherSide);
	
	/**
	 * Creates a task that will assign a transmitter to a ConduitEntranceController.
	 * 
	 * In this task, the otherSide might have to be resolved.
	 */
	protected abstract <T extends Serializable> NamedCallable<Transmitter<T,?>> getTransmitterTask(InstanceController ic, ConduitEntranceControllerImpl<T> localInstance, PortalID otherSide, boolean shared);
	
	protected PortFactory(Resolver res, ExceptionListener listener, IncomingMessageProcessor msgProcessor) {
		this.executor = new LimitedThreadPool(16, listener);
		this.resolver = res;
		this.messageProcessor = msgProcessor;
	}
	
	public void start() {
		this.executor.start();
	}
	
	/** Resolves a PortalID, if not already done. */
	protected boolean resolvePort(PortalID port) throws InterruptedException {
		if (port.isAvailable()) {
			return true;
		} else {
			resolver.resolveIdentifier(port);
			return port.isAvailable();
		}
	}
	
		/** Resolves a PortalID, if not already done. */
	protected boolean portWillActivate(PortalID port) {
		return resolver.identifierMayActivate(port);
	}
	
	public void removeReceiver(Identifier id) {
		this.messageProcessor.removeReceiver(id);
	}
	
	/** Frees all resources attached to the PortFactory. After this call, getReceiver() and getTransmitter() can not be called anymore. */
	@Override
	public void dispose() {
		this.executor.dispose();
	}
	
	@Override
	public boolean isDisposed() {
		return this.executor.isDisposed();
	}
}

/*
Copyright 2008,2009 Complex Automata Simulation Technique (COAST) consortium

GNU Lesser General Public License

This file is part of MUSCLE (Multiscale Coupling Library and Environment).

MUSCLE is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

MUSCLE is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
 */
package muscle.client.instance;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.client.communication.Transmitter;
import muscle.client.communication.message.DetachConduitSignal;
import muscle.core.ConduitEntrance;
import muscle.core.DataTemplate;
import muscle.core.kernel.InstanceController;
import muscle.core.model.Observation;
import muscle.exception.MUSCLEDatatypeException;
import muscle.id.PortalID;

/**
this is the (remote) head of a conduit,
an entrance sends data to the conduit exit through a transmitter
 */
public class PassiveConduitEntranceController<T extends Serializable> extends PassivePortal<T>  implements ConduitEntranceControllerImpl<T> {// generic T will be the underlying unwrapped data, e.g. double[]
	private ConduitEntrance<T> conduitEntrance;
	private Transmitter<T,?> transmitter;
	private boolean transmitterFound;
	private final static Logger logger = Logger.getLogger(PassiveConduitEntranceController.class.getName());
	private volatile boolean processingMessage;
	private boolean isDone;
	
	public PassiveConduitEntranceController(PortalID newPortalID, InstanceController newOwnerAgent, DataTemplate<T> newDataTemplate) {
		super(newPortalID, newOwnerAgent, newDataTemplate);
		this.transmitter = null;
		this.conduitEntrance = null;
		this.processingMessage = false;
		this.isDone = false;
		this.transmitterFound = false;
	}

	/** Set the transmitter that will be used to transmit messages. Before this
	 * is called, the conduit will not be able to send messages.
	 */
	public synchronized void setTransmitter(Transmitter<T,?> trans) {
		logger.log(Level.FINE, "ConduitEntrance <{0}> is now attached.", portalID);
		this.transmitter = trans;
		this.notifyAll();
	}
	
	/** Set the entrance that will be the interface for the CAController. Before
	 *  this is called, messages are not added. */
	public void setEntrance(ConduitEntrance<T> entrance) {		
		this.conduitEntrance = entrance;
	}
	
	/** Get the entrance that is the interface for the CAController. */
	public ConduitEntrance<T> getEntrance() {
		return this.conduitEntrance;
	}
	
	/** Waits until no more messages have to be sent. */
	public synchronized boolean waitUntilEmpty() throws InterruptedException {
		while (this.processingMessage && !isDisposed()) {
			wait();
		}
		return !this.processingMessage;
	}
	
	/** Waits for a resume call if the thread was paused. Returns the transmitter if the thread is no longer paused and false if the thread should stop. */
	private synchronized boolean waitForTransmitter() throws InterruptedException {
		while (transmitter == null && !isDisposed()) {
			logger.log(Level.FINE, "ConduitEntrance <{0}> is waiting for connection to transmit over.", portalID);
			this.wait(WAIT_FOR_ATTACHMENT_MILLIS);
		}
		this.transmitterFound = (transmitter != null);
		return transmitterFound;
	}

	@Override
	public synchronized void dispose() {
		// if we are connected to a conduit, tell conduit to detach this exit
		if (transmitter != null) {
			try {
				transmitter.signal(new DetachConduitSignal());
			} catch (Exception ex) {
				logger.log(Level.WARNING, "Could not detach {0}", this.portalID);
			}
			transmitter.dispose();
			transmitterFound = false;
		}
		
		this.isDone = true;
	}
	
	/**
	 * Actually send message to transmitter.
	 */
	private void transmit(Observation<T> msg) {
		try {
			if (!transmitterFound && !waitForTransmitter()) {
				logger.log(Level.SEVERE, "ConduitEntrance {0} quit before message could be sent.", portalID);
				return;
			}
		} catch (InterruptedException ex) {
			logger.log(Level.SEVERE, "Could not send observation for ConduitEntrance " + portalID, ex);
			return;
		}

		transmitter.transmit(msg);
		increment();
	}
	
	public String toString() {
		return "out:" + this.getIdentifier();
	}

	@Override
	public void send(Observation<T> msg) {
		synchronized (this) {
			if (this.isDisposed()) {
				throw new IllegalStateException("Can not send message over disposed ConduitEntrance");
			}
			this.processingMessage = true;
		}
		if (!dataClass.isInstance(msg.getData())) {
			throw new MUSCLEDatatypeException("Data type "+ msg.getData().getClass().getSimpleName() + " sent through conduit entrance " + this + " does not match expected data type " + dataClass.getSimpleName());
		}
		
		// Update the willStop timestamp as soon as the message is sent by the Instance, not when it is processed.
		this.resetTime(msg.getNextTimestamp());
		this.transmit(msg);

		synchronized (this) {
			this.processingMessage = false;
			this.notifyAll();
		}
	}

	@Override
	public synchronized boolean isDisposed() {
		return this.isDone;
	}
	
	@Override
	public void start() {
		// Do nothing
	}
}

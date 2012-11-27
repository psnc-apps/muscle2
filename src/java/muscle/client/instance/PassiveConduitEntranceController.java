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

import eu.mapperproject.jmml.util.FastArrayList;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.client.LocalManager;
import muscle.client.communication.Transmitter;
import muscle.client.communication.message.DetachConduitSignal;
import muscle.core.ConduitDescription;
import muscle.core.ConduitEntrance;
import muscle.core.ConnectionScheme;
import muscle.core.DataTemplate;
import muscle.core.conduit.filter.FilterChain;
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
	private final FilterChain filters;

	public PassiveConduitEntranceController(PortalID newPortalID, InstanceController newOwnerAgent, DataTemplate<T> newDataTemplate) {
		super(newPortalID, newOwnerAgent, newDataTemplate);
		this.transmitter = null;
		this.conduitEntrance = null;
		this.processingMessage = false;
		this.isDone = false;
		this.transmitterFound = false;
		this.filters = createFilterChain();
	}
	
	/** Create a filter chain from the given arguments */
	private FilterChain createFilterChain() {
		ConnectionScheme cs = ConnectionScheme.getInstance();
		ConduitDescription cd = cs.entranceDescriptionForPortal(portalID).getConduitDescription();
		List<String> args = cd.getArgs();
		int entranceArgDiv = args.indexOf("");
		if (entranceArgDiv < 1) return null;
		
		List<String> entranceArgs = new FastArrayList<String>(entranceArgDiv);
		for (int i = 0; i < entranceArgDiv; i++) {
			entranceArgs.add(args.get(i));
		}
		
		FilterChain fc = new FilterChain() {
			protected void apply(Observation subject) {
				transmitter.transmit(subject);
			}
		};
		
		fc.init(entranceArgs);
		logger.log(Level.INFO, "The conduit entrance ''{0}'' will use filter(s) {1}.", new Object[] {cd, entranceArgs});
		return fc;
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

		if (this.filters == null) {
			transmitter.transmit(msg);
		} else {
			try {
				this.filters.process(msg);
			} catch (Throwable ex) {
				logger.log(Level.SEVERE, "Could not filter message " + msg + " properly, probably the coupling is not correct.", ex);
				LocalManager.getInstance().shutdown(14);
			}
		}
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
		T data = msg.getData();
		if (data != null && !dataClass.isInstance(data)) {
			throw new MUSCLEDatatypeException("Data type "+ data.getClass().getSimpleName() + " sent through conduit entrance " + this + " does not match expected data type " + dataClass.getSimpleName());
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

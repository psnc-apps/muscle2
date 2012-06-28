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
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.client.communication.Transmitter;
import muscle.client.communication.message.DetachConduitSignal;
import muscle.core.*;
import muscle.core.conduit.filter.FilterChain;
import muscle.core.ident.PortalID;
import muscle.core.kernel.InstanceController;
import muscle.core.model.Observation;
import muscle.core.model.Timestamp;
import muscle.util.data.SingleProducerConsumerBlockingQueue;
import muscle.util.serialization.DataConverter;
import muscle.util.serialization.SerializableDataConverter;

/**
this is the (remote) head of a conduit,
an entrance sends data to the conduit exit through a transmitter
@author Jan Hegewald
 */
public class ConduitEntranceControllerImpl<T extends Serializable> extends Portal<T>  implements ConduitEntranceController<T> {// generic T will be the underlying unwrapped data, e.g. double[]
	private ConduitEntrance<T> conduitEntrance;
	private Transmitter<T,?,?,?> transmitter;
	private final static Logger logger = Logger.getLogger(ConduitEntranceControllerImpl.class.getName());
	private final FilterChain filters;
	private boolean processingMessage;
	private final DataConverter<T,?> serializer;
	private BlockingQueue<Observation<T>> queue;
	
	public ConduitEntranceControllerImpl(PortalID newPortalID, InstanceController newOwnerAgent, DataTemplate newDataTemplate) {
		super(newPortalID, newOwnerAgent, newDataTemplate);
		this.transmitter = null;
		this.conduitEntrance = null;
		this.filters = createFilterChain();
		this.processingMessage = false;
		this.serializer = new SerializableDataConverter<T>();
		this.queue = new SingleProducerConsumerBlockingQueue<Observation<T>>();
	}
	
	/** Create a filter chain from the given arguments */
	private FilterChain createFilterChain() {
		FilterChain fc = new FilterChain() {
			@SuppressWarnings("unchecked")
			protected void apply(Observation subject) {
				try {
					ConduitEntranceControllerImpl.this.send(subject);
				} catch (InterruptedException ex) {
					logger.log(Level.SEVERE, "Could not filter observation for ConduitEntrance {0}: {1}",
							new Object[]{portalID, ex});
				}
			}
		};
		ConnectionScheme cs = ConnectionScheme.getInstance();
		ConduitDescription cd = cs.entranceDescriptionForPortal(portalID).getConduitDescription();
		List<String> args = cd.getArgs();
		fc.init(args);
		if (!args.isEmpty()) {
			logger.log(Level.INFO, "The conduit ''{0}'' will use filter(s) {1}.", new Object[] {cd, args});
		}
		return fc;
	}

	/** Set the transmitter that will be used to transmit messages. Before this
	 * is called, the conduit will not be able to send messages.
	 */
	public synchronized void setTransmitter(Transmitter<T,?,?,?> trans) {
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
	
	/**
	 * Processing messages. When messages are finished processing, the conduitEntrance
	 * takes note of this.
	 */
	@Override
	protected void execute() throws InterruptedException {
		boolean running;
		synchronized (this) {
			running = queue != null && !queue.isEmpty();
		}
		while (running) {
			Observation<T> elem = null;
			synchronized (this) {
				this.processingMessage = true;
				if (queue != null) {
					elem = queue.remove();
				}
			}
			
			if (elem == null) {
				logger.log(Level.WARNING, "Can not send null data through ConduitEntrance {0}", this);
				continue;
			}
			
			this.filters.process(elem);
			synchronized (this) {
				this.processingMessage = false;
				running = queue != null && !queue.isEmpty();
			}
		}
		synchronized (this) {
			this.notifyAll();
		}
	}
	
	/** Waits until no more messages have to be sent. */
	public synchronized boolean waitUntilEmpty() throws InterruptedException {
		while (!isDisposed() && (this.processingMessage || (queue != null && !queue.isEmpty()))) {
			wait();
		}
		return !this.processingMessage && (queue == null || queue.isEmpty());
	}
	
	@Override
	protected void handleInterruption(InterruptedException ex) {
		logger.log(Level.SEVERE, "ConduitEntranceController " + this + " interrupted", ex);
	}
	
	/** Waits for a resume call if the thread was paused. Returns the transmitter if the thread is no longer paused and false if the thread should stop. */
	private synchronized Transmitter<T, ?,?,?> waitForTransmitter() throws InterruptedException {
		while (!isDisposed() && transmitter == null) {
			if (logger.isLoggable(Level.FINE)) {
				String msg = "ConduitEntrance <" + portalID + "> is waiting for connection to transmit ";
				if (queue != null) {
					msg += (1 + queue.size()) + " messages ";
				}
				msg += "over.";
				logger.fine(msg);
			}
			this.wait(WAIT_FOR_ATTACHMENT_MILLIS);
		}
		return transmitter;
	}

	@Override
	public synchronized void dispose() {
		// if we are connected to a conduit, tell conduit to detach this exit
		if (transmitter != null) {
			transmitter.signal(new DetachConduitSignal());
			transmitter.dispose();
			transmitter = null;
		}
		queue = null;
		
		super.dispose();
	}
	
	/**
	 * Actually send message to transmitter.
	 */
	private void send(Observation<T> msg) throws InterruptedException {
		Transmitter<T, ?,?,?> trans = waitForTransmitter();
		if (trans != null) {
			trans.transmit(msg);
			increment();
		}
	}
	
	public String toString() {
		return "out:" + this.getIdentifier();
	}

	@Override
	public void send(T data, Timestamp currentTime, Timestamp next) {
		T dataCopy = serializer.copy(data);
		Observation<T> msg = new Observation<T>(dataCopy, currentTime, next);
		
		try {
			this.queue.put(msg);
		} catch (InterruptedException ex) {
			throw new IllegalStateException("Can not send message", ex);
		}
		
		// Update the willStop timestamp as soon as the message is sent by the Instance, not when it is processed.
		this.setNextTimestamp(msg.getNextTimestamp());

		// Make available for processing
		this.trigger();
	}
}

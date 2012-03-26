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
package muscle.core;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.conduit.communication.Transmitter;
import muscle.core.conduit.filter.FilterChain;
import muscle.core.conduit.filter.QueueConsumer;
import muscle.core.ident.PortalID;
import muscle.core.kernel.InstanceController;
import muscle.core.messaging.Observation;
import muscle.core.messaging.signal.DetachConduitSignal;

/**
this is the (remote) head of a conduit,
an entrance sends data to the conduit exit through a transmitter
@author Jan Hegewald
 */
public class ConduitEntranceController<T extends Serializable> extends Portal<T>  implements QueueConsumer<Observation<T>> {// generic T will be the underlying unwrapped data, e.g. double[]
	private ConduitEntrance<T> conduitEntrance;
	private Transmitter<T,?,?,?> transmitter;
	private final static Logger logger = Logger.getLogger(ConduitEntranceController.class.getName());
	private final FilterChain filters;
	private boolean processingMessage;
	private boolean processingNextMessage;
	private BlockingQueue<Observation<T>> queue;
	
	public ConduitEntranceController(PortalID newPortalID, InstanceController newOwnerAgent, int newRate, DataTemplate newDataTemplate, EntranceDependency... newDependencies) {
		super(newPortalID, newOwnerAgent, newRate, newDataTemplate);
		this.queue = null;
		this.transmitter = null;
		this.conduitEntrance = null;
		this.filters = createFilterChain();
	}
	
	/** Create a filter chain from the given arguments */
	private FilterChain createFilterChain() {
		FilterChain fc = new FilterChain() {
			protected void apply(Observation subject) {
				try {
					ConduitEntranceController.this.send(subject);
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
			logger.log(Level.INFO, "In Conduit {0} the filters {1} are initialized.", new Object[] {cd, args});
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
	 * Indicate that a message has been added to the controller. Stores the fact
	 * that a message will be processed.
	 */
	@Override
	public void apply() {
		synchronized (this) {
			// Make sure that a double processingMessage is also processed
			if (processingMessage) {
				processingNextMessage = true;
			}
			else {
				processingMessage = true;
			}
		}
		this.trigger();
	}
	
	/**
	 * Processing messages. When messages are finished processing, the conduitEntrance
	 * takes note of this.
	 */
	@Override
	protected void execute() throws InterruptedException {
		while (!queue.isEmpty()) {
			Observation<T> elem = queue.remove();
			
			this.filters.process(elem);
		}
		synchronized (this) {
			if (processingNextMessage) {
				processingNextMessage = false;
				this.trigger();
			}
			else {
				processingMessage = false;
				this.notifyAll();
			}
		}
	}
	
	/** Waits until no more messages have to be sent. */
	public synchronized boolean waitUntilEmpty() throws InterruptedException {
		while (!isDone && (this.processingMessage || (queue != null && !queue.isEmpty()))) {
			wait();
		}
		return !this.processingMessage && queue.isEmpty();
	}
	
	@Override
	public void setIncomingQueue(BlockingQueue<Observation<T>> queue) {
		this.queue = queue;
	}
	
	@Override
	protected void handleInterruption(InterruptedException ex) {
		logger.log(Level.SEVERE, "ConduitEntranceController {0} interrupted: {1}", new Object[]{portalID, ex});
	}
	
	/** Waits for a resume call if the thread was paused. Returns the transmitter if the thread is no longer paused and false if the thread should stop. */
	private synchronized Transmitter<T, ?,?,?> waitForTransmitter() throws InterruptedException {
		while (!isDone && transmitter == null) {
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

		super.dispose();
	}
	
	/**
	pass raw unwrapped data to this entrance
	 */
	private void send(Observation<T> msg) throws InterruptedException {
		Transmitter<T, ?,?,?> trans = waitForTransmitter();
		if (trans != null) {
			trans.transmit(msg);
			increment();
		}
	}
	
	public String toString() {
		return "|-->" + this.getIdentifier();
	}
}

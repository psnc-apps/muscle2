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

import muscle.core.ident.PortalID;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import muscle.Constant;
import muscle.core.kernel.InstanceController;
import muscle.core.messaging.RemoteDataSinkHead;
import muscle.core.messaging.jade.DataMessage;
import muscle.core.wrapper.DataWrapper;
import muscle.exception.MUSCLERuntimeException;

/**
this is the (remote) head of a conduit,
an entrance sends data to the conduit agent
@author Jan Hegewald
 */
public class ConduitEntrance<T extends java.io.Serializable> extends Portal<T> implements RemoteDataSinkHead<DataMessage<DataWrapper<T>>> {// generic T will be the underlying unwrapped data, e.g. double[]

	private EntranceDependency[] dependencies;
	private AID dstAgent;
	private String dstSink;
	private DataMessage<DataWrapper<T>> dataMessage;
	private boolean shouldPause = false;

	public ConduitEntrance(PortalID newPortalID, InstanceController newOwnerAgent, int newRate, DataTemplate newDataTemplate, EntranceDependency... newDependencies) {
		super(newPortalID, newOwnerAgent, newRate, newDataTemplate);

		dependencies = newDependencies; // dependencies.length == 0 if there are no EntranceDependency references in argument list		
	}

	@Override
	public AID dstAgent() {
		return dstAgent;
	}

	@Override
	public synchronized void pause() {
		shouldPause = true;
		this.notifyAll();
	}

	@Override
	public synchronized void resume() {
		shouldPause = false;
	}

	@Override
	public void put(DataMessage<DataWrapper<T>> dmsg) {
		if (shouldPause) {
			synchronized (this) {
				while (shouldPause) {
					try {
						this.wait();
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}

		ownerAgent.sendDataMessage(dmsg);
	}

	@Override
	public DataMessage<DataWrapper<T>> take() {
		throw new java.lang.UnsupportedOperationException("can not take from " + getClass());
	}

	@Override
	public DataMessage<DataWrapper<T>> poll() {
		throw new java.lang.UnsupportedOperationException("can not poll from " + getClass());
	}

	public DataMessage<DataWrapper<T>> poll(long time, TimeUnit unit) throws InterruptedException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String id() {
		return dstSink;
	}

	public void setDestination(AID newDstAgent, String newDstSink) {
		// allow only once to connect this sender
		if (dstAgent != null) {
			throw new IllegalStateException("already connected to <" + dstAgent + ":" + dstSink + ">");
		}

		dstAgent = newDstAgent;
		dstSink = newDstSink;

		// set up message dummy for outgoing data messages
		dataMessage = new DataMessage(id());
		dataMessage.addReceiver(dstAgent());
	}

	/**
	pass raw unwrapped data to this entrance
	 */
	public void send(T data) {
		DataWrapper wrapper = new DataWrapper<T>(data, getSITime());
		dataMessage.store(wrapper, null);

		increment();

		// send data to target kernel
		put(dataMessage);
	}

	public EntranceDependency[] getDependencies() {
		return dependencies;
	}

	public void detachDestination() {
		assert ownerAgent != null;
		// if we are connected to a conduit, tell conduit to detach this exit		
		if (dstAgent != null) {
			ownerAgent.send(getDetachDstMessage());
		}

		dstAgent = null;
	}

	private ACLMessage getDetachDstMessage() {
		// bulid message which tells the conduit to detach this portal
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol(Constant.Protocol.PORTAL_DETACH);
		try {
			msg.setContentObject(this.getClass());
		} catch (IOException e) {
			throw new MUSCLERuntimeException();
		}

		msg.addReceiver(dstAgent);
		return msg;
	}
}

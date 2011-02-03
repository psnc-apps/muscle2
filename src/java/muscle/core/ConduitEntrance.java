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

import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.io.IOException;

import muscle.Constant;
import muscle.core.kernel.RawKernel;
import muscle.core.messaging.RemoteDataSinkHead;
import muscle.core.messaging.jade.DataMessage;
import muscle.core.wrapper.DataWrapper;
import muscle.exception.MUSCLERuntimeException;
import muscle.utilities.OTFLogger;

/**
this is the (remote) head of a conduit,
an entrance sends data to the conduit agent
@author Jan Hegewald
*/
public class ConduitEntrance<T extends java.io.Serializable> extends Portal<T> implements RemoteDataSinkHead<DataMessage<DataWrapper<T>>> {// generic T will be the underlying unwrapped data, e.g. double[]

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private EntranceDependency[] dependencies;
	private AID dstAgent;
	private String srcSink; 
	private String dstSink;
	private DataMessage<DataWrapper<T>> dataMessage;
	private boolean shouldPause = false;


	//
	public ConduitEntrance(PortalID newPortalID, RawKernel newOwnerAgent, int newRate, DataTemplate<T> newDataTemplate, EntranceDependency ... newDependencies) {
		super(newPortalID, newOwnerAgent, newRate, newDataTemplate);

		this.dependencies = newDependencies; // dependencies.length == 0 if there are no EntranceDependency references in argument list
		srcSink = newPortalID.getName(); 
	}


   //
   public AID dstAgent() {
		return this.dstAgent;
   }


	//
   public void pause() {
		this.shouldPause = true;
   }


	//
   public void resume() {
		this.shouldPause = false;
   }


	//
   public void put(DataMessage<DataWrapper<T>> dmsg) {

		while( this.shouldPause ) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		this.ownerAgent.sendDataMessage(dmsg);
	}


	//
   public DataMessage<DataWrapper<T>> take() {

		throw new java.lang.UnsupportedOperationException("can not take from "+this.getClass());
	}


	//
   public DataMessage<DataWrapper<T>> poll() {

		throw new java.lang.UnsupportedOperationException("can not poll from "+this.getClass());
	}


	//
   public String id() {

		return this.dstSink;
	}


	//
	public void setDestination(AID newDstAgent, String newDstSink) {

		// allow only once to connect this sender
		if(this.dstAgent != null) {
			throw new IllegalStateException("already connected to <"+this.dstAgent+":"+this.dstSink+">");
		}

		this.dstAgent = newDstAgent;
		this.dstSink = newDstSink;

		// set up message dummy for outgoing data messages
		this.dataMessage = new DataMessage<DataWrapper<T>>(this.id());
		this.dataMessage.addReceiver(this.dstAgent());
	}


	/**
	pass raw unwrapped data to this entrance
	*/
	public void send(T data) {

		OTFLogger.getInstance().conduitEnter(srcSink);
		DataWrapper<T> wrapper = new DataWrapper<T>(data, this.getSITime());
		this.dataMessage.store(wrapper, null);

		this.increment();

		OTFLogger.getInstance().logSend(data, srcSink, dstSink); 
		// send data to target kernel
		this.put(this.dataMessage);		
		OTFLogger.getInstance().conduitLeave(srcSink); 
	}


	//
	public EntranceDependency[] getDependencies() {
		return this.dependencies;
	}


	//
	public void detachDestination() {

		assert this.ownerAgent != null;
		// if we are connected to a conduit, tell conduit to detach this exit
		if( this.dstAgent != null ) {
			this.ownerAgent.send(this.getDetachDstMessage());
		}

		this.dstAgent = null;
	}


	//
	private ACLMessage getDetachDstMessage() {

		// bulid message which tells the conduit to detach this portal
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol(Constant.Protocol.PORTAL_DETACH);
		try {
			msg.setContentObject(this.getClass());
		} catch (IOException e) {
			throw new MUSCLERuntimeException();
		}

		msg.addReceiver(this.dstAgent);
		return msg;
	}
}


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
package muscle.core.messaging.jade;

import jade.lang.acl.ACLMessage;
import muscle.core.ident.JadeIdentifier;
import muscle.core.ident.PortalID;
import muscle.core.messaging.Timestamp;
import muscle.core.wrapper.Observation;

/**
@author Jan Hegewald
 */
public class DataMessage<E> extends ACLMessage implements Cloneable {
	// note: JADE sends messages differently if they are passed to a remote container or locally within the same container
	// for the remote container, a new ACLMessage is created and filled with the proper contents
	// for the local container, a clone is created via ACLMessage#clone and thus remains a DataMessage class including transient fields
	public final static String DATA_KEY = DataMessage.class.getName() + "#id";
	public final static String PORT_KEY = DataMessage.class.getName() + "#portid";
	public final static String TYPE_KEY = DataMessage.class.getName() + "#idtype";
	private transient E storedItem;
	private JadeIdentifier sinkID;
	private Long byteCount;

	public DataMessage() {
		super(ACLMessage.INFORM);
	}
	
	public void setRecipient(JadeIdentifier id) {
		this.sinkID = id;
		this.addReceiver(id.getAID());
		String portName = (id instanceof PortalID) ? ((PortalID)id).getPortName() : null;
		this.setIdentifierString(sinkID.getName(), sinkID.getType().name(), portName);
	}
	
	protected void setIdentifierString(String name, String type, String portName) {
		this.setIdentifierString(DATA_KEY, name, type, portName);
	}
	protected void setIdentifierString(String key, String name, String type, String portName) {
		addUserDefinedParameter(key, name);
		addUserDefinedParameter(TYPE_KEY, type);
		if (portName != null) {
			addUserDefinedParameter(PORT_KEY, portName);
		}
	}
	
	public void store(E item, Long newByteCount) {
		assert newByteCount == null || newByteCount > 0;
		byteCount = newByteCount;
		storedItem = item;
	}

	public Long getByteCount() {
		return byteCount;
	}

	public E getData() {
		return storedItem;
	}

	public JadeIdentifier getRecipient() {
		return this.sinkID;
	}

	public E getObservation() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Timestamp getTimestampNextEvent() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}

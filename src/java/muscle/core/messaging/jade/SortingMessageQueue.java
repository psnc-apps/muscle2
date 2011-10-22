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

import jade.core.PublicMessageQueue;
import jade.lang.acl.ACLMessage;
import java.util.Collection;
import java.util.Queue;
import muscle.core.messaging.Message;
import muscle.core.messaging.serialization.ACLConverter;

/**
custom message queue for a jade.core.Agent which sorts arriving messages to pure jade acl messages and other messages
@author Jan Hegewald
 */
public class SortingMessageQueue<E> extends PublicMessageQueue {
	private final Queue<Message<E>> nonACLQueue;
	private final ACLConverter<E> deserializer;

	public SortingMessageQueue(Queue<Message<E>> newNonACLQueue) {
		nonACLQueue = newNonACLQueue;
		deserializer = new ACLConverter<E>();
	}

	@Override
	public void addFirst(ACLMessage msg) {
		Message<E> dmsg;
		if ((dmsg = deserializer.deserialize(msg)) != null) {
			nonACLQueue.add(dmsg);
		} else {
			super.addFirst(msg);
		}
	}

	@Override
	public void addLast(ACLMessage msg) {
		Message<E> dmsg;
		if ((dmsg = deserializer.deserialize(msg)) != null) {
			nonACLQueue.add(dmsg);
		} else {
			super.addLast(msg);
		}
	}
}

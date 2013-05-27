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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.client.communication.message;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.client.communication.Receiver;
import muscle.id.Identifier;
import muscle.util.concurrency.Disposable;

/**
 *
 * @author Joris Borgdorff
 */
public class LocalDataHandler implements Disposable, IncomingMessageProcessor {
	private final Map<Identifier,Receiver> listener;
	private final static Logger logger = Logger.getLogger(LocalDataHandler.class.getName());
	private boolean isDone;

	public LocalDataHandler() {
		listener = new ConcurrentHashMap<Identifier,Receiver>();
	}
	
	@Override
	public void addReceiver(Identifier id, Receiver recv) {
		listener.put(id, recv);
	}
	
	@SuppressWarnings("unchecked")
	public void put(Message msg) {
		Identifier recipient = msg.getRecipient();
		Receiver recv = listener.get(recipient);

		if (recv == null) {
			if (!(msg.getSignal() instanceof DetachConduitSignal)) {
				logger.log(Level.WARNING, "No receiver registered for message {0}.", msg);					
			}
		} else {
			recv.put(msg);
		}
	}
	
	@Override
	public void removeReceiver(Identifier id) {
		listener.remove(id);
	}
	
	@Override
	public void dispose() {
		this.isDone = true;
		for (Receiver recv : listener.values()) {
			recv.dispose();
		}
	}

	@Override
	public boolean isDisposed() {
		return this.isDone;
	}
}

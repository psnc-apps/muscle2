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

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.client.LocalManager;
import muscle.client.communication.Receiver;
import muscle.id.Identifier;
import muscle.id.Resolver;
import muscle.net.AbstractConnectionHandler;
import muscle.util.concurrency.NamedCallable;

/**
 * Accepts TCP/IP connections and registers receivers for those connections
 * @author Joris Borgdorff
 */
@SuppressWarnings("rawtypes") // We don't know the type of the receivers, and we don't need to
public class TcpIncomingConnectionHandler extends AbstractConnectionHandler<LocalManager,Boolean> implements IncomingMessageProcessor {
	private final Resolver resolver;
	private final Map<Identifier,Receiver> receivers;
	private final static Logger logger = Logger.getLogger(TcpIncomingConnectionHandler.class.getName());

	public TcpIncomingConnectionHandler(ServerSocket ss, Resolver res) {
		super(ss, LocalManager.getInstance());
		logger.log(Level.CONFIG, "Listening for data connections on {0}:{1}", new Object[]{ss.getInetAddress().getHostAddress(), ss.getLocalPort()});
		this.resolver = res;
		Map<Identifier,Receiver> rawRecv = new ConcurrentHashMap<Identifier,Receiver>();
		this.receivers = rawRecv;
	}
	
	@Override
	protected NamedCallable<Boolean> createProtocolHandler(Socket s) {
		return new TcpIncomingMessageHandler(s, receivers, resolver, this);
	}
	
	void result(Boolean bool) {
		if (bool != null && !bool.booleanValue() && !isDisposed()) {
			logger.log(Level.SEVERE, "Could not receive message, message lost. Aborting.");
			LocalManager.getInstance().shutdown(6);
		}
	}

	@Override
	public void addReceiver(Identifier id, Receiver recv) {
		receivers.put(id, recv);
		logger.log(Level.FINER, "Added receiver {0} to receivers", id);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		for (Receiver recv : receivers.values()) {
			recv.dispose();
		}
	}

	@Override
	public void removeReceiver(Identifier id) {
		this.receivers.remove(id);
	}
}

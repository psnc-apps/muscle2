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
 * 
 */

package muscle.net;

import muscle.exception.ExceptionListener;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.util.concurrency.NamedCallable;
import muscle.util.concurrency.NamedExecutor;
import muscle.util.concurrency.SafeThread;

/**
 * Handles connections over a socket.
 * 
 * By overriding executeProtocol(Socket), you can send
 * and receive messages over a socket. This function will repeatedly be called for every
 * accepted socket, until the dispose() function is called.
 * 
 * @author Joris Borgdorff
 */
public abstract class AbstractConnectionHandler<T extends ExceptionListener, E> extends SafeThread {
	protected final ServerSocket ss;
	protected final T listener;
	protected final NamedExecutor executor;
	private final static Logger logger = Logger.getLogger(AbstractConnectionHandler.class .getName());

	public AbstractConnectionHandler(ServerSocket ss, T listener) {
		super("ConnectionHandler-" + ss);
		this.ss = ss;
		this.listener = listener;
		executor = new NamedExecutor();
	}

	@Override
	protected final synchronized void handleInterruption(InterruptedException ex) {
		if (isDisposed()) {
			logger.log(Level.FINE, "ConnectionHandler {0} finished.", this.getClass());		 
		} else {
			logger.log(Level.WARNING, "ConnectionHandler interrupted", ex);
		}
	}
	
	protected final synchronized void handleException(Throwable ex) {
		if (ex instanceof IOException) {
			if (!isDisposed())
				logger.log(Level.SEVERE, "ConnectionHandler could not accept connection.", ex);
			// Else, we're closing the serversocket ourselves.
			ex = null;
		} else {
			logger.log(Level.SEVERE, "Fatal exception in connectionhandling occurred", ex);
		}
		if (ex != null) {
			listener.fatalException(ex);
		}
	}
	
	@Override
	protected final void execute() throws Exception {
		Socket s = this.ss.accept();
        s.setKeepAlive(true);
        s.setTcpNoDelay(true);
        //int target = 16777216;
        //s.setReceiveBufferSize(target);
        //while (s.getReceiveBufferSize() < target) {
        //    target = (target*2)/3;
        //    s.setReceiveBufferSize(target);
        //}
        //logger.log(Level.FINER, "Set TCP receive buffer size to {0}", target);
		logger.log(Level.FINE, "Accepted connection from: {0}", s.getRemoteSocketAddress());
		executor.submit(this.createProtocolHandler(s));
	}
	
	protected abstract NamedCallable<E> createProtocolHandler(Socket s);
	
	public synchronized void dispose() {
		logger.finer("Stopping connection handler");
		super.dispose();
		try {
			logger.finest("Closing connection handler server socket");
			this.ss.close();
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "ServerSocket could not be closed.", ex);
		}
		logger.finest("Stopping connection handler threads");
		executor.shutdown();
	}
	
	public InetSocketAddress getSocketAddress() {
		return (InetSocketAddress)ss.getLocalSocketAddress();
	}
	
	public void resubmit(NamedCallable<E> protocolHandler) {
		if (!this.isDisposed()) {
			executor.submit(protocolHandler);
		}
	}
}

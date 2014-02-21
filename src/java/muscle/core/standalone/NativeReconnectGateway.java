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
package muscle.core.standalone;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.util.concurrency.Disposable;

/**
 * Communicates with the API in cppmuscle.cpp with a TCP/IP connection using the XDR protocol.
 * 
 * When the socket between Java and the native code has an error (disconnected, could not connect), it is assumed
 * that the native command had an unrecoverable error, and the gateway stops. By setting the Java property
 * muscle.core.standalone.use_async to false, synchronous sockets will be used instead of asynchronous sockets.
 */
public class NativeReconnectGateway extends NativeGateway implements Disposable {
	private final static Logger logger = Logger.getLogger(NativeReconnectGateway.class.getName());
	
	public NativeReconnectGateway(NativeController listener) throws UnknownHostException, IOException {
		super(listener);
	}
	
	@Override
	protected void communicate() throws IOException {
		final boolean isFinestLog = logger.isLoggable(Level.FINEST);
		
		while (!isDisposed) {
			if (nativeSock == null) {
				if (isFinestLog) logger.log(Level.FINEST, "{0} is listening for new connection", getName());
				acceptSocket();
			}

			if (isFinestLog) logger.finest("Starting decoding...");
			in.refresh();

			NativeProtocol proto = handleProtocol();

			if (proto == NativeProtocol.FINALIZE) {
				return; // Finalized, we can exit now
			} else if (proto == NativeProtocol.SEND) { // Flush and close socket, except when forwarding message
				in.cleanUp();
			} else {
				if (isFinestLog) logger.finest("flushing response");
				out.flush();
				in.close();
				in = null;
				out.close();
				out = null;
				
				if (isFinestLog) logger.finest("closing socket ...");
				nativeSock.close();
				nativeSock = null;
			}
			if (isFinestLog) logger.finest("proceeding to next native call");
		}
	}
}

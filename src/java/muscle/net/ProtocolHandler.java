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

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.exception.MUSCLERuntimeException;
import muscle.id.Location;
import muscle.id.LocationType;
import muscle.id.TcpLocation;
import muscle.util.concurrency.NamedCallable;
import muscle.util.serialization.ConverterWrapperFactory;
import muscle.util.serialization.DeserializerWrapper;
import muscle.util.serialization.SerializerWrapper;

/**
 * Handles a protocol using serialization.
 * 
 * By overriding executeProtocol(XdrTcpDecodingStream xdrIn, XdrTcpEncodingStream xdrOut), you can send
 * and receive messages over a socket using XDR serialization. This class will repeatedly be created for every
 * accepted socket.
 * 
 * @author Joris Borgdorff
 */
public abstract class ProtocolHandler<S,T> implements NamedCallable<S> {
	protected final T listener;
	protected final Socket socket;
	private final static Logger logger = Logger.getLogger(ProtocolHandler.class.getName());
	private final boolean closeSocket;
	private DeserializerWrapper in;
	private SerializerWrapper out;
	private final boolean outIsControl;
	private final boolean inIsControl;
	private final int tries;
	
	public ProtocolHandler(Socket s, T listener, boolean inIsControl, boolean outIsControl, int tries, boolean closeSocket) {
		this.listener = listener;
		this.socket = s;
		this.closeSocket = closeSocket;
		this.in = null;
		this.out = null;
		this.inIsControl = inIsControl;
		this.outIsControl = outIsControl;
		this.tries = tries;
	}
	
	public ProtocolHandler(Socket s, T listener, boolean inIsControl, boolean outIsControl, boolean closeSocket) {
		this(s, listener, inIsControl, outIsControl, 1, closeSocket);
	}
	
	public ProtocolHandler(Socket s, T listener, boolean inIsControl, boolean outIsControl) {
		this(s, listener, inIsControl, outIsControl, 1, false);
	}
	
	public ProtocolHandler(Socket s, T listener, boolean inIsControl, boolean outIsControl, int tries) {
		this(s, listener, inIsControl, outIsControl, tries, false);
	}
	
	@Override
	public S call() {
		S ret = null;
		boolean succeeded = false;
		for (int i = 1; !succeeded && i <= tries; i++) {
			try {
				if (in == null) {
					if (inIsControl) {
						in = ConverterWrapperFactory.getControlDeserializer(socket);
					} else {
						in = ConverterWrapperFactory.getDataDeserializer(socket);
					}
				}
				if (out == null) {
					if (outIsControl) {
						out = ConverterWrapperFactory.getControlSerializer(socket);
					} else {
						out = ConverterWrapperFactory.getDataSerializer(socket);
					}
				}

				ret = executeProtocol(in, out);
				handleResult(ret);
				succeeded = true;
			} catch (MUSCLERuntimeException ex) {
				logger.log(Level.SEVERE, "Communication aborted: MUSCLE is not properly set up.", ex);
				handleThrowable(ex);
				break;
			} catch (SocketException ex) {
				logger.log(Level.SEVERE, "Communication aborted: the other side hung up.", ex);
				handleThrowable(ex);
				break;
			} catch (IOException ex) {
				logger.log(Level.SEVERE, "Communication error; could not encode/decode from socket. Try " + i + "/" +tries+ ".", ex);
				if (i == tries) {
					handleThrowable(ex);
				}
			} catch (Exception ex) {
				logger.log(Level.SEVERE, "Could not finish protocol due to an error. Try " + i + "/" +tries+ ".", ex);
				if (i == tries) {
					handleThrowable(ex);
				}
			}
		}
		if (this.closeSocket) {
			try {
				this.socket.close();
			} catch (IOException ex) {
				logger.log(Level.SEVERE, "Failure to close socket after protocol has finished", ex);
			}
		}
		return ret;
	}
	
	/**
	 * Execute a protocol over a serialized stream.
	 * It is the responsibility of the protocol handler to perform in.refresh() and out.flush(). All exceptions
	 * are handled by ProtocolHandler.
	 */
	protected abstract S executeProtocol(DeserializerWrapper in, SerializerWrapper out) throws IOException;

	/**
	 * Encodes a TcpLocation over a serialized stream.
	 * @param out a stream to encode over; flush() will not be called over it.
	 * @param loc must be a TcpLocation
	 */
	protected static void encodeLocation(SerializerWrapper out, Location loc) throws IOException {
		if (!(loc instanceof TcpLocation)) {
			throw new IllegalArgumentException("Location belonging to identity is not a TcpLocation; can only encode TcpLocation");
		}
		TcpLocation tcpLoc = (TcpLocation)loc;

		byte[] addr = tcpLoc.getAddress().getAddress();
		out.writeInt(LocationType.TCP_DIR_LOCATION.intValue());
		out.writeByteArray(addr);
		out.writeInt(tcpLoc.getPort());
		out.writeString(tcpLoc.getTmpDir());
	}

	/**
	 * Decodes a TcpLocation from a serialized stream.
	 * @param in a stream to decode from; refresh() will not be called over it.
	 * @returns the TcpLocation that was transmitted.
	 */
	protected static TcpLocation decodeLocation(DeserializerWrapper in) throws IOException {
		LocationType locType = LocationType.valueOf(in.readInt());
		byte[] addr = in.readByteArray();
		InetAddress inetAddr = InetAddress.getByAddress(addr);
		int port = in.readInt();
		String tmpDir;
		if (locType == LocationType.TCP_DIR_LOCATION) {
			tmpDir = in.readString();
		} else {
			tmpDir = "";
		}
		return new TcpLocation(inetAddr, port, tmpDir);
	}
	
	public abstract String getName();
	
	protected void handleThrowable(Throwable ex) {}
	protected void handleResult(S s) {}
}

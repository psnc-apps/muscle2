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

package muscle.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.util.SynchronizedTimer;
import muscle.util.Timer;
import muscle.util.concurrency.SafeTriggeredThread;
import muscle.util.serialization.ConverterWrapperFactory;
import muscle.util.serialization.DeserializerWrapper;
import muscle.util.serialization.SerializerWrapper;

/**
 * A socket that opens or closes when requested.
 * After a timeout, it disconnects and tries to reconnect at the next opportunity.
 * 
 * @author Joris Borgdorff
 */
public class AliveSocket extends SafeTriggeredThread {
	private final static Logger logger = Logger.getLogger(AliveSocket.class.getName());
	
	private final InetSocketAddress address;
	private final SocketFactory socketFactory;
	private Socket socket;
	private final ReentrantLock lock;
	private final Timer timeSinceUnlock;
	private final long keepAlive;
	private SerializerWrapper out;
	private DeserializerWrapper in;
	
	/**
	 * 
	 * @param sf
	 * @param addr
	 * @param keepAlive milliseconds to keep a socket alive before quitting
	 * @param keepAlivePing milliseconds before you send a stillAlive message
	 */
	public AliveSocket(SocketFactory sf, InetSocketAddress addr, long keepAlive) {
		super("AliveSocket-" + addr);
		this.socketFactory = sf;
		this.address = addr;
		this.socket = null;
		this.lock = new ReentrantLock();
		this.timeSinceUnlock = new SynchronizedTimer();
		this.keepAlive = keepAlive;
		this.in = null;
		this.out = null;
	}
	
	/** 
	 * Locks the socket.
	 * 
	 * It must always be followed by a call to unlock(), for instance in a finally
	 * block. This will fail only if the socket is disposed of.
	 * 
	 * @return whether the lock succeeded
	 */
	public boolean lock() {
		if (this.isDisposed()) {
			return false;
		}
		lock.lock();
		if (this.isDisposed()) {
			lock.unlock();
			return false;
		}
		return true;
	}
	
	/** Get a output serializer. */
	public SerializerWrapper getOutput() throws IOException {				
		this.updateSocket();
		if (this.out == null) {
			out = ConverterWrapperFactory.getDataSerializer(this.socket);
		}
		return this.out;
	}

	/** Get a input deserializer. */
	public DeserializerWrapper getInput() throws IOException {
		this.updateSocket();
		if (this.in == null) {
			in = ConverterWrapperFactory.getDataDeserializer(this.socket);
		}
		return this.in;
	}
	
	/**
	 * Try to use the current socket, or create a new one if it is expired. 
	 * @throws IllegalStateException if the lock on the socket is not held when calling
	 */
	protected void updateSocket() throws IOException {
		if (!lock.isHeldByCurrentThread()) {
			throw new IllegalStateException("Can only use AliveSocket with a lock.");
		}
		
		if (socket != null && socket.isClosed()) {
			this.out = null;
			this.in = null;
			this.socket = null;
		}
		
		if (socket == null) {
			this.socket = this.socketFactory.createSocket();
            this.socket.setKeepAlive(true);
            this.socket.setTcpNoDelay(true);

            //int target = 16777216;
            //this.socket.setReceiveBufferSize(target);
            //while (this.socket.getReceiveBufferSize() < target) {
            //    target = (target*2)/3;
            //    this.socket.setReceiveBufferSize(target);
            //}
            //logger.log(Level.FINER, "Set TCP receive buffer size to {0}", target);
			this.socket.connect(this.address);
		}
	}
		
	/**
	 * Unlocks the socket for some other's use.
	 */
	public void unlock() {
		this.timeSinceUnlock.reset();
		lock.unlock();
		this.trigger();
	}
	
	@Override
	protected void handleInterruption(InterruptedException ex) {
		// Do nothing
	}
	@Override
	protected void handleException(Throwable ex) {
		logger.log(Level.SEVERE, "Exception in Socket occurred. Closing connection.", ex);
		this.dispose();
	}
	
	@Override
	public void dispose() {
		super.dispose();
		lock.lock();
		try {
			this.reset();
		} finally {
			lock.unlock();
		}
	}

	@Override
	protected void execute() throws InterruptedException {
		long milli = this.timeSinceUnlock.millisec();
		if (milli < this.keepAlive) {
			Thread.sleep(this.keepAlive - milli);
		}
		try {
			// Disposed, or there has been another unlock, and thus trigger.
			if (isDisposed() || this.timeSinceUnlock.millisec() < this.keepAlive) {
				return;
			}
			lock.lock();
			
			if (isDisposed() || this.timeSinceUnlock.millisec() < this.keepAlive) {
				this.trigger();
				return;
			}
		
			this.reset();
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Exception in Socket occurred", ex);
		} finally {
			// lock may not yet be locked.
			if (lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
		}
	}
	
	/**
	 * Close the active socket, if any.
	 * The next call to AliveSocket will have to open a new connection.
	 * @throws IllegalStateException if the lock on this alivesocket is not held
	 */
	public void reset() {
		if (!lock.isHeldByCurrentThread()) {
			throw new IllegalStateException("Can only use AliveSocket with a lock.");
		}

		if (this.socket != null) {
			if (!this.socket.isClosed()) {
				logger.fine("Closing stale socket.");
				try {
					getOutput();
					out.writeInt(-1);
					out.close();
					if (in != null) {
						in.close();
					}
				} catch (IOException ex) {
					logger.log(Level.WARNING, "Could not properly send end-sequence to socket", ex);
					try {
						this.socket.close();
					} catch (IOException ex1) {
						Logger.getLogger(AliveSocket.class.getName()).log(Level.SEVERE, "Could not properly close socket", ex1);
					}
				}
			}
			this.out = null;
			this.in = null;
			this.socket = null;
		}
	}
}

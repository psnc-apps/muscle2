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
import muscle.core.messaging.serialization.ConverterWrapperFactory;
import muscle.core.messaging.serialization.SerializerWrapper;
import muscle.utilities.parallelism.SafeTriggeredThread;
import utilities.Timer;

/**
 *
 * @author Joris Borgdorff
 */
public class AliveSocket extends SafeTriggeredThread {
	private final InetSocketAddress address;
	private final SocketFactory socketFactory;
	private Socket socket;
	private final ReentrantLock lock;
	private final Timer timeSinceUnlock;
	private final long keepAlive;
	
	/**
	 * 
	 * @param sf
	 * @param addr
	 * @param keepAlive milliseconds to keep a socket alive before quitting
	 * @param keepAlivePing milliseconds before you send a stillAlive message
	 */
	public AliveSocket(SocketFactory sf, InetSocketAddress addr, long keepAlive) {
		this.socketFactory = sf;
		this.address = addr;
		this.socket = null;
		this.lock = new ReentrantLock();
		this.timeSinceUnlock = new Timer();
		this.keepAlive = keepAlive;
	}
	
	/** 
	 * Locks the socket.
	 * 
	 * @return whether a new socket should be created
	 * @throws IllegalStateException when the AliveSocket has been disposed.
	 */
	public boolean lockSocket() {
		synchronized (this) {
			if (this.isDone) {
				throw new IllegalStateException("AliveSocket has already been disposed.");
			}
			lock.lock();
		}
		return socket == null;
	}
	
	public Socket getOrCreateSocket() throws IOException {
		if (socket == null) {
			this.socket = this.socketFactory.createSocket();
			this.socket.connect(this.address);
		}
		return this.socket;		
	}
	
	public void unlockSocket() {
		this.timeSinceUnlock.reset();
		this.trigger();
		lock.unlock();
	}
	
	@Override
	protected void handleInterruption(InterruptedException ex) {
		// Do nothing
	}
	
	@Override
	public void dispose() {
		super.dispose();
		lock.lock();
		this.closeSocket();
	}

	@Override
	protected void execute() throws InterruptedException {
		long milli = this.timeSinceUnlock.millisec();
		if (milli < this.keepAlive) {
			Thread.sleep(this.keepAlive - milli);
		}
		try {
			synchronized (this) {
				if (isDone || this.timeSinceUnlock.millisec() < this.keepAlive) {
					return;
				}
				lock.lock();
			}
			if (this.timeSinceUnlock.millisec() < this.keepAlive) {
				this.trigger();
				return;
			}

			this.closeSocket();
		} catch (Exception ex) {
			Logger.getLogger(AliveSocket.class.getName()).log(Level.SEVERE, "Exception occurred", ex);
		} finally {
			if (lock.isHeldByCurrentThread())
				lock.unlock();
		}
	}
	
	private void closeSocket() {
		if (this.socket != null) {
			try {
				SerializerWrapper out = ConverterWrapperFactory.getControlSerializer(this.socket);
				out.writeInt(-1);
				out.close();
				this.socket.close();
				this.socket = null;
			} catch (IOException ex) {}
		}
	}
}

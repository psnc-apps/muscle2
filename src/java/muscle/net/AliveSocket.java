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
import muscle.util.Timer;
import muscle.util.concurrency.SafeTriggeredThread;
import muscle.util.serialization.ConverterWrapperFactory;
import muscle.util.serialization.SerializerWrapper;

/**
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
		this.timeSinceUnlock = new Timer();
		this.keepAlive = keepAlive;
	}
	
	/** 
	 * Locks the socket.
	 * 
	 * It must always be followed by a unlockSocket() statement, in a finally
	 * block of the try/catch construction that envelops getOrCreateSocket().
	 * 
	 * @return whether a new socket should be created
	 * @throws IllegalStateException when the AliveSocket has been disposed.
	 */
	public boolean lockSocket() {
		synchronized (this) {
			if (this.isDisposed()) {
				throw new IllegalStateException("AliveSocket has already been disposed of.");
			}
			lock.lock();
		}
		return socket == null;
	}
	
	/** 
	 * Gets or creates the underlying socket.
	 * 
	 * @return socket to be used
	 * @throws IllegalStateException when a lock has not been obtained through lockSocket()
	 */
	public Socket getOrCreateSocket() throws IOException {
		if (!lock.isHeldByCurrentThread()) {
			throw new IllegalStateException("Socket can only be retrieved once a lock has been obtained.");
		}
		if (socket == null) {
			this.socket = this.socketFactory.createSocket();
			this.socket.connect(this.address);
		}
		return this.socket;
	}
	
	/**
	 * Unlocks the socket for some other's use.
	 */
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
		boolean lockHeld = false;
		
		long milli = this.timeSinceUnlock.millisec();
		if (milli < this.keepAlive) {
			Thread.sleep(this.keepAlive - milli);
		}
		try {
			synchronized (this) {
				if (isDisposed() || this.timeSinceUnlock.millisec() < this.keepAlive) {
					// also runs finally block, but lockHeld is false so
					// it won't try to unlock
					return;
				}
				lock.lock();
				lockHeld = true;
			}
			if (this.timeSinceUnlock.millisec() < this.keepAlive) {
				this.trigger();
				return;
			}

			this.closeSocket();
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Exception occurred", ex);
		} finally {
			if (lockHeld)
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
			} catch (IOException ex) {
				logger.log(Level.WARNING, "Could not properly close socket", ex);
			}
		}
	}
}

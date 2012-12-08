/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import muscle.util.concurrency.Disposable;
import muscle.util.serialization.ConverterWrapperFactory;
import muscle.util.serialization.SerializerWrapper;

/**
 *
 * @author Joris Borgdorff
 */
public class SocketPool implements Disposable {
	private final int limit;
	private final BlockingQueue<Socket> socketPool;
	private boolean isDone;
	private volatile int size;
	private final SocketFactory sockets;
	private final InetSocketAddress addr;
	private final int closeValue;
	
	public SocketPool(int size, SocketFactory sf, InetSocketAddress addr, int closeValue) {
		this.sockets = sf;
		this.size = 0;
		this.limit = size;
		this.socketPool = new LinkedBlockingQueue<Socket>();
		this.isDone = false;
		this.addr = addr;
		this.closeValue = closeValue;
	}
	
	@Override
	public synchronized void dispose() {
		if (this.isDisposed()) {
			return;
		}
		this.isDone = true;
		
		Socket s;
		while ((s = this.socketPool.poll()) != null) {
			try {
				SerializerWrapper out = ConverterWrapperFactory.getControlSerializer(s);
				out.writeInt(closeValue);
				out.close();
				s.close();
			} catch (IOException ex) {
				// do nothing
			}
		}
	}

	@Override
	public synchronized boolean isDisposed() {
		return this.isDone;
	}

	public Socket createSocket(boolean longLasting) throws IOException, InterruptedException {
		if (!longLasting) {
			if (this.socketPool.isEmpty() && this.size < limit) {
				// create a socket below
				this.size++;
			} else {
				return this.socketPool.take();
			}
		}
		Socket s = this.sockets.createSocket();
		s.connect(addr);
		return s;
	}
	
	public void socketFinished(Socket s, SerializerWrapper out) throws IOException {
		if (socketPool.size() < limit) {
			synchronized (this) {
				if (!isDisposed()) {
					this.socketPool.add(s);
					return;
				}
			}
		} else if (this.size < limit) {
			this.size = limit;
		}
		out.writeInt(closeValue);
		out.close();
		s.close();
	}
}

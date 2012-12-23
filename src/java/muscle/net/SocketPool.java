/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import muscle.util.concurrency.Disposable;
import muscle.util.serialization.ConverterWrapperFactory;
import muscle.util.serialization.SerializerWrapper;

/**
 *
 * @author Joris Borgdorff
 */
public class SocketPool implements Disposable {
	private final int limit;
	private final ArrayBlockingQueue<Socket> socketPool;
	private boolean isDone;
	private int size;
	private final SocketFactory sockets;
	private final InetSocketAddress addr;
	private final int closeValue;
	
	public SocketPool(int size, SocketFactory sf, InetSocketAddress addr, int closeValue) {
		this.sockets = sf;
		this.size = 0;
		this.limit = size;
		this.socketPool = new ArrayBlockingQueue<Socket>(size);
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
		notifyAll();
	}

	@Override
	public synchronized boolean isDisposed() {
		return this.isDone;
	}

	public Socket createSocket(boolean longLasting) throws IOException, InterruptedException {
		Socket s = null;
		if (!longLasting) {
			s = this.socketPool.poll();
			while (s == null) {
				synchronized (this) {
					if (this.size < limit || isDisposed()) {
						break;
					}
					wait();
				}
				s = this.socketPool.poll();
			}
		}
		if (s == null) {
			synchronized (this) {
				this.size++;
			}
			s = this.sockets.createSocket();
			s.connect(addr);
		}
		return s;
	}
	
	public void socketFinished(Socket s, SerializerWrapper out) throws IOException {
		synchronized (this) {
			if (!this.isDisposed()) {
				boolean succeed = this.socketPool.offer(s);
				if (succeed) {
					notify();
					return;
				} else {
					this.size = this.limit;
				}
			}
		}
		
		out.writeInt(closeValue);
		out.close();
		s.close();
	}

	public synchronized void discard(Socket socket) throws IOException {
		this.size--;
		socket.close();
		notify();
	}
}

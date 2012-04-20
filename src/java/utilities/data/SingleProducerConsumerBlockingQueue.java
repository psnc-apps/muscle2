/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.data;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * An efficient BlockingQueue for a single producer and single consumer.
 * Correct behavior is guaranteed only if used by a single
 * producing thread and a single consuming thread.
 * Null values are allowed. Except size(), isEmpty() and clear(), its collection
 * interface is not implemented.
 * 
 * @author Joris Borgdorff
 */
public class SingleProducerConsumerBlockingQueue<E> implements BlockingQueue<E> {
	private final E[] elements;
	private volatile int min, max;
	private int consMin, consMax, prodMin, prodMax;
	private final Object prodLock = new Object(), consLock = new Object();
	
	public SingleProducerConsumerBlockingQueue(int size) {
		this.elements = (E[])new Object[size + 1];
		this.min = this.consMin = this.prodMin = 0;
		this.max = this.consMax = this.prodMax = 0;
	}
	
	// Producer methods
	@Override
	public boolean add(E e) {
		if (!offer(e)) throw new IllegalStateException("Queue is full");
		return true;
	}

	@Override
	public boolean offer(E e) {
		int newMax = (this.prodMax + 1)%this.elements.length;
		
		if (this.prodMin == newMax) {
			this.prodMin = this.min;
			if (this.prodMin == newMax) return false;
		}
		
		elements[this.prodMax] = e;
		this.max = this.prodMax = newMax;

		synchronized (prodLock) {
			prodLock.notify();
		}
		
		return true;
	}

	@Override
	public void put(E e) throws InterruptedException {
		int newMax = (this.prodMax + 1)%this.elements.length;
		
		if (this.prodMin == newMax) {
			synchronized (consLock) {
				while (this.min == newMax) {
					consLock.wait();
				}
			}
			this.prodMin = this.min;
		}

		elements[this.prodMax] = e;
		this.max = this.prodMax = newMax;
		
		synchronized (prodLock) {
			prodLock.notify();
		}
	}

	// Consumer methods
	@Override
	public E take() throws InterruptedException {
		int newMin = (this.consMin + 1)%this.elements.length;
		E ret;
		
		if (this.consMin == this.consMax) {
			synchronized (prodLock) {
				while (this.consMin == this.max) {
					prodLock.wait();
				}
			}
			this.consMax = this.max;
		}
		
		ret = elements[this.consMin];

		elements[this.consMin] = null;
		this.min = this.consMin = newMin;
		
		synchronized (consLock) {
			consLock.notify();
		}
		
		return ret;
	}

	@Override
	// Same as poll, but we can not reuse poll, since it returns null for
	// both null elements and with an emtpy queue.
	public E remove() {
		int newMin = (this.consMin + 1)%this.elements.length;
		E ret;

		if (this.consMin == this.consMax) {
			this.consMax = this.max;
			if (this.consMin == this.consMax) throw new IllegalStateException("Queue is empty");
		}
		
		ret = elements[this.consMin];
		elements[this.min] = null;
		this.min = this.consMin = newMin;

		synchronized (consLock) {	
			consLock.notify();
		}
		
		return ret;
	}

	@Override
	public E poll() {
		int newMin = (this.consMin + 1)%this.elements.length;
		E ret;

		if (this.consMin == this.consMax) {
			this.consMax = this.max;
			if (this.consMin == this.consMax) return null;
		}
		
		ret = elements[this.min];
		elements[this.consMin] = null;
		this.min = this.consMin = newMin;

		synchronized (consLock) {	
			consLock.notify();
		}
		
		return ret;
	}

	// Non-modifying methods
	@Override
	public E element() {
		if (this.consMin == this.consMax) {
			this.consMax = this.max;
			if (this.consMin == this.consMax) throw new IllegalStateException("Queue is empty");
		}
		
		return elements[this.consMin];
	}

	@Override
	public E peek() {
		if (this.consMin == this.consMax) {
			this.consMax = this.max;
			if (this.consMin == this.consMax) return null;
		}
		
		return elements[this.consMin];
	}

	@Override
	public int remainingCapacity() {
		return min > max ? min - max - 1: this.elements.length - (max - min) - 1;
	}

	@Override
	public int size() {
		return min > max ? this.elements.length - (min - max) : (max - min);
	}

	@Override
	public boolean isEmpty() {
		return max == min;
	}

	@Override
	/** May only be called by the consumer thread. */
	public void clear() {
		this.consMax = this.max;
		if (this.consMin <= this.consMax) {
			for (int i = this.consMin; i < this.consMax; i++) {
				elements[i] = null;
			}
		} else {
			for (int i = 0; i < this.consMax; i++) {
				elements[i] = null;
			}
			for (int i = this.consMin; i < elements.length; i++) {
				elements[i] = null;
			}
		}
		this.min = this.consMin = this.consMax;
	}
	
	@Override
	public Iterator<E> iterator() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public <T> T[] toArray(T[] ts) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean containsAll(Collection<?> clctn) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean addAll(Collection<? extends E> clctn) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean removeAll(Collection<?> clctn) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean retainAll(Collection<?> clctn) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public E poll(long l, TimeUnit tu) throws InterruptedException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean offer(E e, long l, TimeUnit tu) throws InterruptedException {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
		@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean contains(Object o) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int drainTo(Collection<? super E> clctn) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int drainTo(Collection<? super E> clctn, int i) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}

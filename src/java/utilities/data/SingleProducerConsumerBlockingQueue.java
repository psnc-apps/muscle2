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
 *
 * @author Joris Borgdorff
 */
public class SingleProducerConsumerBlockingQueue<E> implements BlockingQueue<E> {
	private final E[] elements;
	private int min, max;
	private final Object prodLock = new Object(), consLock = new Object();
	
	public SingleProducerConsumerBlockingQueue(int size) {
		this.elements = (E[])new Object[size + 1];
		this.min = 0;
		this.max = 0;
	}
	
	// Producer methods
	@Override
	public boolean add(E e) {
		if (!offer(e)) throw new IllegalStateException("Queue is full");
		return true;
	}

	@Override
	public boolean offer(E e) {
		int newMax = (this.max + 1)%this.elements.length;
		
		if (this.min == newMax) {
			synchronized (consLock) {
				if (this.min == newMax) return false;
			}
		}
		
		elements[this.max] = e;
		
		synchronized (prodLock) {
			this.max = newMax;
			prodLock.notify();
		}
		
		return true;
	}

	@Override
	public void put(E e) throws InterruptedException {
		int newMax = (this.max + 1)%this.elements.length;
		
		if (this.min == newMax) {
			synchronized (consLock) {
				while (this.min == newMax) {
					consLock.wait();
				}
			}
		}

		elements[this.max] = e;
		
		synchronized (prodLock) {
			this.max = newMax;
			prodLock.notify();
		}
	}

	// Consumer methods
	@Override
	public E take() throws InterruptedException {
		int newMin = (this.min + 1)%this.elements.length;
		E ret;
		
		if (this.min == this.max) {
			synchronized (prodLock) {
				while (this.min == this.max) {
					prodLock.wait();
				}
			}
		}
		
		ret = elements[this.min];
		
		synchronized (consLock) {	
			elements[this.min] = null;
			this.min = newMin;
			consLock.notify();
		}
		
		return ret;
	}

	@Override
	public E remove() {
		E ret = poll();
		if (ret == null) throw new IllegalStateException("Queue is empty");
		
		return ret;
	}

	@Override
	public E poll() {
		int newMin = (this.min + 1)%this.elements.length;
		E ret;

		if (this.min == this.max) {
			synchronized (prodLock) {
				if (this.min == this.max) return null;
			}
		}
		
		ret = elements[this.min];

		synchronized (consLock) {	
			elements[this.min] = null;
			this.min = newMin;
			consLock.notify();
		}
		
		return ret;
	}

	// Non-modifying methods
	@Override
	public E element() {
		E ret = peek();
		
		if (ret == null) throw new IllegalStateException("Queue is empty");
		
		return ret;
	}

	@Override
	public E peek() {
		if (this.min == this.max) {
			synchronized (prodLock) {
				if (this.min == this.max) return null;
			}
		}
		
		return elements[this.min];
	}

	@Override
	public int remainingCapacity() {
		synchronized (prodLock) {
			synchronized (consLock) {		
				return min > max ? min - max - 1: this.elements.length - (max - min) - 1;
			}
		}
	}

	@Override
	public int size() {
		synchronized (prodLock) {
			synchronized (consLock) {		
				return min > max ? this.elements.length - (min - max) : (max - min);
			}
		}
	}

	@Override
	public boolean isEmpty() {
		synchronized (prodLock) {
			synchronized (consLock) {
				return max == min;
			}
		}
	}

	@Override
	public synchronized void clear() {
		synchronized (prodLock) {
			synchronized (consLock) {
				if (this.min <= this.max) {
					for (int i = this.min; i < this.max; i++) {
						elements[i] = null;
					}
				} else {
					for (int i = 0; i < this.max; i++) {
						elements[i] = null;
					}
					for (int i = this.min; i < elements.length; i++) {
						elements[i] = null;
					}
				}
				this.min = this.max = 0;
			}
		}
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

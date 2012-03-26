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
	
	public SingleProducerConsumerBlockingQueue(int size) {
		this.elements = (E[])new Object[size];
		this.min = 0;
		this.max = 0;
	}
	
	@Override
	public boolean add(E e) {
		if (!offer(e)) throw new IllegalStateException("Queue is full");
		return true;
	}

	@Override
	public boolean offer(E e) {
		int newMax = (this.max + 1)%this.elements.length;
		
		synchronized (this) {
			if (this.min == newMax) return false;
			elements[this.max] = e;
			this.max = newMax;
			notify();
		}
		
		return true;
	}

	@Override
	public void put(E e) throws InterruptedException {
		int newMax = (this.max + 1)%this.elements.length;
		
		synchronized (this) {
			while (this.min == newMax) {
				wait();
			}
			elements[this.max] = e;

			this.max = newMax;
			notify();
		}
	}

	
	@Override
	public E take() throws InterruptedException {
		int newMin = (this.min + 1)%this.elements.length;
		E ret;
		
		synchronized (this) {
			while (this.min == this.max) {
				wait();
			}
			ret = elements[this.min];
			elements[this.min] = null;
			this.min = newMin;
			notify();
		}
		
		return ret;
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
	public int remainingCapacity() {
		return min > max ? min - max : this.elements.length - (max - min);
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

	@Override
	public E remove() {
		int newMin = (this.min + 1)%this.elements.length;
		E ret;
		
		synchronized (this) {
			if (this.min == this.max) throw new IllegalStateException("Queue is empty");
			ret = elements[this.min];
			elements[this.min] = null;
			this.min = newMin;
			notify();
		}
		
		return ret;
	}

	@Override
	public E poll() {
		int newMin = (this.min + 1)%this.elements.length;
		E ret;
		
		synchronized (this) {
			if (this.min == this.max) return null;
			ret = elements[this.min];
			elements[this.min] = null;
			this.min = newMin;
			notify();
		}
		
		return ret;
	}

	@Override
	public E element() {
		synchronized (this) {
			if (this.min == this.max) throw new IllegalStateException("Queue is empty");
		}
		
		return elements[this.min];		
	}

	@Override
	public E peek() {
		synchronized (this) {
			if (this.min == this.max) return null;
		}
		
		return elements[this.min];		
	}

	@Override
	public synchronized int size() {
		return min > max ? this.elements.length - (min - max) : (max - min);
	}

	@Override
	public synchronized boolean isEmpty() {
		return max == min;
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
	public void clear() {
		this.min = this.max = 0;
	}
}

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

package muscle.util.data;

import java.util.Collection;
import java.util.Iterator;
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
public class SingleProducerConsumerBlockingQueue<E> implements TakeableQueue<E>, TakeAddable<E> {
	private final TailPointer tail;
	private final HeadPointer head;
	
	public SingleProducerConsumerBlockingQueue() {
		head = new HeadPointer();
		tail = new TailPointer();
	}
	
	// Producer methods
	@Override
	public boolean add(E e) {
		tail.put(e);
		return true;
	}

	@Override
	public boolean offer(E e) {
		tail.put(e);
		return true;
	}

	@Override
	public void put(E e) throws InterruptedException {
		tail.put(e);
	}

	// Consumer methods
	@Override
	public E take() throws InterruptedException {
		return head.take();
	}

	public class HeadPointer {
		private boolean empty = true;
		private SingleProducerConsumerBlockingQueue.Element<E> head;
		
		public E take() throws InterruptedException {
			if (empty) {
				synchronized (this) {
					while (head == null) {
						wait();
					}
				}
				empty = false;
			}

			return advance();
		}
		
		/**
		 * Advance the head of the queue to the next element.
		 * May only be called if the queue is non-empty. If the queue is empty
		 * after advancing, the variable empty is set to true.
		 * 
		 * @return the previous head of the queue
		 */
		private E advance() {
			final SingleProducerConsumerBlockingQueue.Element<E> oldHead = head;

			synchronized (oldHead) {
				head = oldHead.prevElement;
				if (head == null) empty = true;
			}
			// If the queue is empty, the oldHead will still be stored as the tail, so it will not be garbage collected.
			// Thus its contents is set to null.
			if (empty) {
				final E ret = oldHead.value;
				oldHead.value = null;
				return ret;
			} else {
				return oldHead.value;
			}
		}
		
		public E remove() {
			if (isEmpty()) throw new IllegalStateException("Queue is empty");

			return advance();			
		}
		
		public E poll() {
			if (isEmpty()) return null;
			
			return advance();
		}
		
		public E element() {
			if (isEmpty()) throw new IllegalStateException("Queue is empty");

			return head.value;
		}
		
		public E peek() {
			if (isEmpty()) return null;
			
			return head.value;
		}
		
		synchronized void setHead(SingleProducerConsumerBlockingQueue.Element<E> value) {
			if (value == null) {
				empty = true;
			}
			head = value;
			notify();
		}
		
		/** Unsynchronized version of isEmpty. */
		boolean unsafeIsEmpty() {
			return head == null;
		}
		
		public boolean isEmpty() {
			if (empty) {
				synchronized (this) {
					if (head == null) return true;
				}
				empty = false;
			}
			return false;
		}
		
		public int size() {
			int size = 0;
			if (!isEmpty()) {
				SingleProducerConsumerBlockingQueue.Element<E> tmp = head;
				while (tmp != null) {
					size++;
					synchronized (tmp) {
						tmp = tmp.prevElement;
					}
				}
			}
			return size;
		}
	}
	
	public class TailPointer {
		private SingleProducerConsumerBlockingQueue.Element<E> tail = new SingleProducerConsumerBlockingQueue.Element<E>(null);
		
		public void put(E e) {
			final SingleProducerConsumerBlockingQueue.Element<E> oldTail = tail;
			tail = new SingleProducerConsumerBlockingQueue.Element<E>(e);
		
			synchronized (oldTail) {
				if (head.unsafeIsEmpty()) {
					head.setHead(tail);
				} else {
					oldTail.prevElement = tail;
				}
			}
		}
	}
	
	@Override
	public E remove() {
		return head.remove();
	}

	@Override
	public E poll() {
		return head.poll();
	}

	// Non-modifying methods
	@Override
	public E element() {
		return head.element();
	}

	@Override
	public E peek() {
		return head.peek();
	}

	@Override
	public int remainingCapacity() {
		return Integer.MAX_VALUE;
	}

	@Override
	public int size() {
		return head.size();
	}

	@Override
	public boolean isEmpty() {
		return head.isEmpty();
	}

	@Override
	public void clear() {
		head.setHead(null);
	}
	
	private static class Element<E> {
		E value;
		SingleProducerConsumerBlockingQueue.Element<E> prevElement;
		
		public Element(E val) {
			value = val;
			prevElement = null;
		}
	}
	
	/** Not implemented. */
	@Override
	public Iterator<E> iterator() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/** Not implemented. */
	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/** Not implemented. */
	@Override
	public <T> T[] toArray(T[] ts) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/** Not implemented. */
	@Override
	public boolean containsAll(Collection<?> clctn) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/** Not implemented. */
	@Override
	public boolean addAll(Collection<? extends E> clctn) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/** Not implemented. */
	@Override
	public boolean removeAll(Collection<?> clctn) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/** Not implemented. */
	@Override
	public boolean retainAll(Collection<?> clctn) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/** Not implemented. */
	@Override
	public E poll(long l, TimeUnit tu) throws InterruptedException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/** Not implemented. */
	@Override
	public boolean offer(E e, long l, TimeUnit tu) throws InterruptedException {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	/** Not implemented. */
	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/** Not implemented. */
	@Override
	public boolean contains(Object o) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/** Not implemented. */
	@Override
	public int drainTo(Collection<? super E> clctn) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/** Not implemented. */
	@Override
	public int drainTo(Collection<? super E> clctn, int i) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}

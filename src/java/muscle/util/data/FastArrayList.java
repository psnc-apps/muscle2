package muscle.util.data;

import java.util.*;

/**
 * A fast implementation of a List. It does no runtime checking to keep access times as fast as possible. 
 * Direct manipulation is preferred over the helper class java.util.Arrays.
 * @author Joris Borgdorff
 */
public class FastArrayList<T> implements List<T> {
	private int size;
	private T[] elems;
	private final FastIterator iter;
	
	public FastArrayList() {
		this(5);
	}
	
	@SuppressWarnings("unchecked")
	public FastArrayList(int initialCapacity) {
		this((T[])new Object[initialCapacity]);
		this.size = 0;
	}
	
	public FastArrayList(T[] initialArray) {
		this.elems = initialArray;
		this.iter = new FastIterator();
		this.size = initialArray.length;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public boolean contains(Object o) {
		return indexOf(o) != -1;
	}

	@Override
	public Iterator<T> iterator() {
		this.iter.reset();
		return this.iter;
	}

	@Override
	public Object[] toArray() {
		Object[] ret = new Object[size];
		System.arraycopy(elems, 0, ret, 0, size);
		return ret;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] ts) {
		if (ts.length >= size) {
			System.arraycopy(elems, 0, ts, 0, size);
			return ts;
		}
		else {
			return (T[])this.toArray();
		}
	}

	@Override
	public boolean add(T e) {
		ensureCapacity(size + 1);
		elems[size] = e;
		this.size++;
		return true;
	}

	@Override
	public boolean remove(Object o) {
		int i = indexOf(o);
		if (i != -1) {
			remove(i);
			return true;
		}
		return false;
	}

	/** Not implemented */
	@Override
	public boolean containsAll(Collection<?> clctn) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean addAll(Collection<? extends T> clctn) {
		int newSize = size + clctn.size();
		ensureCapacity(newSize);
		if (clctn instanceof List) {
			List<? extends T> list = (List<? extends T>) clctn;
			for (int i = size; i < newSize; i++) {
				elems[i] = list.get(i - size);
			}
		} else {
			Iterator<? extends T> addIter = clctn.iterator();
			for (int i = size; i < newSize; i++) {
				elems[i] = addIter.next();
			}
		}
		size = newSize;
		return true;
	}

	/** Not implemented */
	@Override
	public boolean addAll(int i, Collection<? extends T> clctn) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/** Not implemented */
	@Override
	public boolean removeAll(Collection<?> clctn) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/** Not implemented */
	@Override
	public boolean retainAll(Collection<?> clctn) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void clear() {
		for (int i = 0; i < size; i++) {
			this.elems[i] = null;
		}
		size = 0;
	}

	@Override
	public T get(int i) {
		return elems[i];
	}

	@Override
	public T set(int i, T e) {
		T tmp = elems[i];
		elems[i] = e;
		return tmp;
	}

	@Override
	public void add(int i, T e) {
		if (i == size) {
			this.add(e);
		}
		else {
			ensureCapacity(size + 1);
			System.arraycopy(elems, i, elems, i + 1, size - i);
			elems[i] = e;
		}
	}

	@Override
	public T remove(int i) {
		size--;
		T tmp = elems[i];
		if (i == size) {
			return elems[i];
		} else {
			System.arraycopy(elems, i + 1, elems, i, size - i);
		}
		// Free space
		elems[size] = null;
		
		return tmp;
	}

	@Override
	public int indexOf(Object o) {
		if (o == null) {
			for (int i = 0; i < size; i++) {
				if (elems[i] == null) return i;
			}
		}
		else {
			for (int i = 0; i < size; i++) {
				if (o.equals(elems[i])) return i;
			}
		}
		return -1;
	}

	@Override
	public int lastIndexOf(Object o) {
		if (o == null) {
			for (int i = size - 1; i >= 0; i--) {
				if (elems[i] == null) return i;
			}
		}
		else {
			for (int i = size - 1; i >= 0; i--) {
				if (o.equals(elems[i])) return i;
			}
		}
		return -1;
	}

	@Override
	public ListIterator<T> listIterator() {
		this.iter.reset();
		return this.iter;
	}

	@Override
	public ListIterator<T> listIterator(int i) {
		this.iter.reset(i);
		return this.iter;
	}

	@Override
	public List<T> subList(int i, int i1) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	public T[] getBackingArray() {
		return elems;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(50*size);
		sb.append('[');
		for (int i = 0; i < size - 1; i++) {
			sb.append(elems[i]).append(", ");
		}
		if (size > 0) {
			sb.append(elems[size-1]);
		}
		sb.append(']');
		return sb.toString();
	}
	
	private void ensureCapacity(int capacity) {
		if (elems.length <= capacity) {
			int newLen = Math.max(elems.length * 2 + 1, capacity);
			@SuppressWarnings("unchecked")
			T[] tmp = (T[]) new Object[newLen];
			System.arraycopy(elems, 0, tmp, 0, size);
			elems = tmp;
		}
	}
	
	public boolean equals(Object o) {
		if (o == null || !o.getClass().equals(this.getClass())) return false;
		FastArrayList other = (FastArrayList)o;
		if (size != other.size) return false;
		for (int i = 0; i < size; i++) {
			if (elems[i] == null ? other.elems[i] != null : !elems[i].equals(other.elems[i])) return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + this.size;
		for (int i = 0; i < size; i++) {
			hash = 31 * hash + (elems[i] == null ? 0 : elems[i].hashCode());
		}
		return hash;
	}
	
	private final class FastIterator implements ListIterator<T> {
		private int i;
		
		public FastIterator() {
			i = -1;
		}
		
		@Override
		public boolean hasNext() {
			return i + 1 < size;
		}

		@Override
		public boolean hasPrevious() {
			return i >= 0;
		}

		
		@Override
		public T next() {
			return elems[++i];
		}

		@Override
		public void remove() {
			FastArrayList.this.remove(i);
			i--;
		}
		
		void reset() {
			i = -1;
		}
		
		void reset(int i_) {
			this.i = i_;
		}

		@Override
		public T previous() {
			return elems[i--];
		}

		@Override
		public int nextIndex() {
			return i + 1;
		}

		@Override
		public int previousIndex() {
			return i;
		}

		@Override
		public void set(T e) {
			elems[i] = e;
		}

		@Override
		public void add(T e) {
			FastArrayList.this.add(i, e);
		}	
	}
}

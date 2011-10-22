package utilities;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 *
 * @author Joris Borgdorff
 */
public class ArraySet<V> implements Set<V> {
	private V[] values;
	int size;
	
	public ArraySet() {
		this(5);
	}
	
	public ArraySet(Set<? extends V> col) {
		if (col instanceof ArraySet) {
			values = Arrays.copyOf(((ArraySet<? extends V>)col).values, col.size());
		}
		else {
			values = (V[]) new Object[col.size()];
			Iterator<? extends V> iter = col.iterator();
			for (int i = 0; i < col.size(); i++) {
				values[i] = iter.next();
			}
		}
		this.size = col.size();
	}
	
	@SuppressWarnings({"unchecked"})
	public ArraySet(int initialCapacity) {
		values = (V[]) new Object[initialCapacity];
		size = 0;
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
		return indexOf(o) >= 0;
	}

	@Override
	public Iterator<V> iterator() {
		return new SetIterator();
	}

	@Override
	public Object[] toArray() {
		return Arrays.copyOf(values, size);
	}

	@Override
	@SuppressWarnings({"unchecked", "unchecked", "unchecked"})
	public <T> T[] toArray(T[] ts) {
		if (ts.length >= size) {
			System.arraycopy(values, 0, ts, 0, size);
			return ts;
		}
		else {
			return (T[])Arrays.copyOf(values, size);
		}
	}

	@Override
	public boolean add(V e) {
		int index = indexOf(e);
		if (index == -1) {
			this.ensureCapacity(size + 1);
			this.values[size] = e;
			this.size++;
		}
		else {
			values[index] = e;
		}
		return index == -1;
	}

	@Override
	public boolean remove(Object o) {
		int index = indexOf(o);
		if (index >= 0) {
			remove(index);
		}
		return index >= 0;
	}

	private void remove(int index) {
		System.arraycopy(values, index + 1, values, index, size - index - 1);
		size--;
	}

	@Override
	public boolean containsAll(Collection<?> clctn) {
		for (Object o : clctn) {
			if (!contains(o)) return false;
		}
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends V> clctn) {
		this.ensureCapacity(size + clctn.size());
		int initial = size;
		
		for (V v : clctn) {
			int index = indexOf(v);
			if (index == -1) {
				this.values[size] = v;
				this.size++;
			}
			else {
				values[index] = v;
			}
		}
		return initial != size;
	}

	@Override
	public boolean retainAll(Collection<?> clctn) {
		int i = 0, initial = size;
		while (i < size) {
			if (!clctn.contains(values[i])) {
				remove(i);
			}
			else {
				i++;
			}
		}
		return size == initial;
	}

	@Override
	public boolean removeAll(Collection<?> clctn) {
		int initial = size;
		for (Object o : clctn) {
			remove(o);
		}
		return initial == size;
	}

	@Override
	public void clear() {
		this.size = 0;
	}
	
	private int indexOf(Object o) {
		if (o == null) {
			for (int i = 0; i < size; i++) {
				if (values[i] == null) return i;
			}
		}
		else {
			for (int i = 0; i < size; i++) {
				if (o.equals(values[i])) return i;
			}
		}
		return -1;
	}
	
	private void ensureCapacity(int newSize) {
		if (newSize > values.length) {
			int newCapacity = Math.max(newSize, values.length*2 + 1);
			values = Arrays.copyOf(values, newCapacity);
		}
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder(size*50);
		sb.append('{');
		for (int i = 0; i < size-1; i++) {
			sb.append(values[i]).append(',');
		}
		if (size != 0) {
			sb.append(values[size-1]);
		}
		sb.append('}');
		return sb.toString();
	}
	
	private class SetIterator implements Iterator<V> {
		private int i;
		
		public SetIterator() {
			i = -1;
		}
		
		@Override
		public boolean hasNext() {
			return i + 1 < size;
		}

		@Override
		public V next() {
			i++;
			if (i >= size) {
				throw new NoSuchElementException("Iterator has no next element.");
			}
			return values[i];
		}

		@Override
		public void remove() {
			ArraySet.this.remove(i);
			i--;
		}
	}
}

package utilities.data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A full Map implementation meant for small maps. It stores its values in two arrays, making storage
 * requirements low, and gives lookup a low overhead but an asymptotic time of O(n). Instead of calling equals
 * for each comparison, hashCode is called and if the hashCode matches then equals is called. This means
 * hashCode and equals should both be implemented.
 * @author Joris Borgdorff
 */
public class ArrayMap<K,V> implements Map<K, V>, Serializable {
	enum IteratorType {
		KEYS, VALUES, ENTRY;
	}

	private int size;
	private int[] hashes;
	private K[] keys;
	private V[] values;
	
	public ArrayMap(Map<? extends K, ? extends V> map) {
		this(map.size());
		if (map instanceof ArrayMap) {
			ArrayMap<? extends K, ? extends V> amap = (ArrayMap<? extends K, ? extends V>)map;
			hashes = Arrays.copyOf(amap.hashes, map.size());
			keys = Arrays.copyOf(amap.keys, map.size());
			values = Arrays.copyOf(amap.values, map.size());
		}
		else {
			Iterator<? extends Entry<? extends K, ? extends V>> iter = map.entrySet().iterator();
			for (int i = 0; i < map.size(); i++) {
				Entry<? extends K, ? extends V>entry = iter.next();
				keys[i] = entry.getKey();
				hashes[i] = keys[i] == null ? -1 : keys[i].hashCode();
				values[i] = entry.getValue();				
			}
		}
	}
	
	public ArrayMap() {
		this(5);
	}
	
	@SuppressWarnings({"unchecked", "unchecked"})
	public ArrayMap(int initialCapacity) {
		hashes = new int[initialCapacity];
		keys = (K[])new Object[initialCapacity];
		values = (V[])new Object[initialCapacity];
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
	public boolean containsKey(Object o) {
		return indexOfKey(o) >= 0;
	}

	@Override
	public boolean containsValue(Object o) {
		return indexOfValue(o) >= 0;
	}

	private int indexOfKey(Object o) {
		if (o == null) {
			for (int i = 0; i < size; i++) {
				if (keys[i] == null) return i;
			}
		}
		else {
			int hash = o.hashCode();
			for (int i = 0; i < size; i++) {
				if (hashes[i] == hash && o.equals(keys[i])) return i;
			}
		}
		return -1;
	}

	private int indexOfValue(Object o) {
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
	
	@Override
	public V get(Object o) {
		int index = indexOfKey(o);
		if (index == -1) return null;
		return values[index];
	}

	@Override
	public V put(K k, V v) {
		int index = indexOfKey(k);
		if (index == -1) {
			this.ensureCapacity(size + 1);
			this.hashes[size] = k == null ? -1 : k.hashCode();
			this.keys[size] = k;
			this.values[size] = v;
			this.size++;
			return null;
		}
		else {
			V tmp = this.values[index];
			this.values[index] = v;
			return tmp;
		}
	}

	@Override
	public V remove(Object o) {
		int index = indexOfKey(o);
		if (index == -1) return null;
		return remove(index);
	}

	V remove(int index) {
		V tmp = values[index];
		int start = index + 1, end = size - index - 1;
		System.arraycopy(hashes, start, hashes, index, end);
		System.arraycopy(keys, start, keys, index, end);
		System.arraycopy(values, start, values, index, end);
		size--;
		return tmp;
	}

	
	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		this.ensureCapacity(map.size() + size);
				
		for (Entry<? extends K, ? extends V> entry : map.entrySet()) {
			int index = indexOfKey(entry.getKey());
			if (index == -1) {
				this.keys[size] = entry.getKey();
				this.hashes[size] = keys[size] == null ? -1 : keys[size].hashCode();
				this.values[size] = entry.getValue();
				this.size++;
			}
			else {
				this.values[index] = entry.getValue();
			}
		}
	}

	@Override
	public void clear() {
		size = 0;
	}

	@Override
	public Set<K> keySet() {
		return new KeySet();
	}

	@Override
	public Collection<V> values() {
		return new ValueCollection();
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return new EntrySet();
	}
	
	private void ensureCapacity(int newSize) {
		if (newSize > keys.length) {
			int newCapacity = Math.max(newSize, keys.length*2 + 1);
			hashes = Arrays.copyOf(hashes, newCapacity);
			keys = Arrays.copyOf(keys, newCapacity);
			values = Arrays.copyOf(values, newCapacity);
		}
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder(size*100);
		sb.append('{');
		for (int i = 0; i < size - 1; i++) {
			sb.append(keys[i]).append(" => ").append(values[i]).append(',');
		}
		if (size > 0) {
			sb.append(keys[size-1]).append(" => ").append(values[size-1]);
		}
		sb.append('}');
		return sb.toString();
	}
	
	private class KeySet implements Set<K> {
		@Override
		public int size() {
			return size;
		}

		@Override
		public boolean isEmpty() {
			return ArrayMap.this.isEmpty();
		}

		@Override
		public boolean contains(Object o) {
			return containsKey(o);
		}

		@Override
		public Iterator<K> iterator() {
			return new MapIterator<K>(IteratorType.KEYS);
		}

		@Override
		public Object[] toArray() {
			return Arrays.copyOf(keys, size);
		}

		@Override
		@SuppressWarnings({"unchecked", "unchecked"})
		public <T> T[] toArray(T[] ts) {
			if (ts.length == size) {
				System.arraycopy(keys, 0, ts, 0, size);
				return ts;
			}
			else {
				return (T[])Arrays.copyOf(keys, size);
			}
		}

		@Override
		public boolean add(K e) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public boolean remove(Object o) {
			int index = indexOfKey(o);
			if (index == -1) return false;
			ArrayMap.this.remove(index);
			return true;
		}

		@Override
		public boolean containsAll(Collection<?> clctn) {
			for (Object o : clctn) {
				if (!containsKey(o)) return false;
			}
			return true;
		}

		@Override
		public boolean addAll(Collection<? extends K> clctn) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public boolean retainAll(Collection<?> clctn) {
			int i = 0, initial = size;
			while (i < size) {
				if (!clctn.contains(keys[i])) {
					ArrayMap.this.remove(i);
				}
				else {
					i++;
				}
			}
			return initial == size;
		}

		@Override
		public boolean removeAll(Collection<?> clctn) {
			int initial = size;
			for (Object o : clctn) {
				this.remove(o);
			}
			return initial == size;
		}

		@Override
		public void clear() {
			ArrayMap.this.clear();
		}
	
	}

	private class EntrySet implements Set<Entry<K,V>> {
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
			if (!(o instanceof Entry)) throw new ClassCastException("Only contains Map.Entry.");
			Entry me = (Entry)o;
			int index = indexOfKey(me.getKey());
			if (index == -1) return false;
			return (values[index] == null ? me.getValue() == null : values[index].equals(me.getValue()));
		}

		@Override
		public Iterator<Entry<K, V>> iterator() {
			return new MapIterator<Entry<K, V>>(IteratorType.ENTRY);
		}

		@Override
		public Object[] toArray() {
			@SuppressWarnings("unchecked")
			MapEntry[] map = (MapEntry[])new Object[size];
			
			for (int i = 0; i < size; i++) {
				map[i] = new MapEntry(i);
			}
			return map;
		}

		@Override
		public <T> T[] toArray(T[] ts) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public boolean add(Entry<K, V> e) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public boolean remove(Object o) {
			if (!(o instanceof Entry)) throw new ClassCastException("Only contains Map.Entry.");
			Entry e = (Entry)o;
			int index = indexOfKey(o);
			if (index == -1) return false;
			if (values[index] == null ? e.getValue() != null : !values[index].equals(e.getValue())) return false;
			ArrayMap.this.remove(index);
			return true;
		}

		@Override
		public boolean containsAll(Collection<?> clctn) {
			for (Object o : clctn) {
				if (!contains(o)) return false;
			}
			return true;
		}

		@Override
		public boolean addAll(Collection<? extends Entry<K, V>> clctn) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public boolean retainAll(Collection<?> clctn) {
			int i = 0, initial = size;
			Entry<K,V> entry = new MapEntry(0);
			
			while (i < size) {
				if (!clctn.contains(entry)) {
					ArrayMap.this.remove(i);
				}
				else {
					entry = new MapEntry(++i);
				}
			}
			return initial == size;
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
			ArrayMap.this.clear();
		}
	}
	
	private class MapIterator<T> implements Iterator<T> {
		private int i;
		private IteratorType type;
		private MapEntry entry;
		
		public MapIterator(IteratorType type) {
			i = -1;
			this.type = type;
			if (type == IteratorType.ENTRY) {
				entry = new MapEntry(0);
			}
		}
		
		@Override
		public boolean hasNext() {
			return i + 1 < size;
		}

		@Override
		@SuppressWarnings({"unchecked", "unchecked"})
		public T next() {
			i++;
			if (i >= size) {
				throw new NoSuchElementException("Iterator has no next element.");
			}
			switch (type) {
				case KEYS: return (T)keys[i];
				case VALUES: return (T)values[i];
				default:
					entry.setIndex(i);
					return (T)entry;
			}
		}

		@Override
		public void remove() {
			ArrayMap.this.remove(i);
			i--;
		}
	}
	
	private class MapEntry implements Entry<K, V> {
		private int i;
		
		MapEntry(int i) {
			this.i = i;
		}
		private void setIndex(int i) {
			this.i = i;
		}
		
		@Override
		public K getKey() {
			return keys[i];
		}

		@Override
		public V getValue() {
			return values[i];
		}

		@Override
		public V setValue(V v) {
			V tmp = values[i];
			values[i] = v;
			return tmp;
		}
	}
	
	private class ValueCollection implements Collection<V> {
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
			return containsValue(o);
		}

		@Override
		public Iterator<V> iterator() {
			return new MapIterator<V>(IteratorType.VALUES);
		}

		@Override
		public Object[] toArray() {
			return Arrays.copyOf(values, size);
		}

		@Override
		@SuppressWarnings({"unchecked", "unchecked"})
		public <T> T[] toArray(T[] ts) {
			if (ts.length == size) {
				System.arraycopy(values, 0, ts, 0, size);
				return ts;
			}
			else {
				return (T[])Arrays.copyOf(values, size);
			}
		}
		
		@Override
		public boolean remove(Object o) {
			int index = indexOfValue(o);
			if (index == -1) return false;
			ArrayMap.this.remove(index);
			return true;
		}

		@Override
		public boolean containsAll(Collection<?> clctn) {
			for (Object o : clctn) {
				if (!containsKey(o)) return false;
			}
			return true;
		}

		@Override
		public boolean addAll(Collection<? extends V> clctn) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public boolean retainAll(Collection<?> clctn) {
			int i = 0, initial = size;
			while (i < size) {
				if (!clctn.contains(values[i])) {
					ArrayMap.this.remove(i);
				}
				else {
					i++;
				}
			}
			return initial == size;
		}

		@Override
		public boolean removeAll(Collection<?> clctn) {
			int initial = size;
			for (Object o : clctn) {
				while (remove(o)){}
			}
			return initial == size;
		}

		@Override
		public void clear() {
			ArrayMap.this.clear();
		}

		@Override
		public boolean add(V e) {
			throw new UnsupportedOperationException("Not supported.");
		}
	}
}

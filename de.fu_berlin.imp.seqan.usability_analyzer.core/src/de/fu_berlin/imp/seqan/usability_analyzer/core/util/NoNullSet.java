package de.fu_berlin.imp.seqan.usability_analyzer.core.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class NoNullSet<E> implements Set<E> {
	final private Set<E> set = new HashSet<E>();

	@Override
	public int size() {
		return set.size();
	}

	@Override
	public boolean isEmpty() {
		return set.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return set.contains(o);
	}

	@Override
	public Iterator<E> iterator() {
		return set.iterator();
	}

	@Override
	public Object[] toArray() {
		return set.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return set.toArray(a);
	}

	@Override
	public boolean add(E e) {
		if (e == null)
			throw new IllegalArgumentException("null is forbidden");
		return set.add(e);
	}

	@Override
	public boolean remove(Object o) {
		if (o == null)
			throw new IllegalArgumentException("null is forbidden");
		return set.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object e : c) {
			if (e == null)
				throw new IllegalArgumentException("null is forbidden");
		}
		return this.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		for (Object e : c) {
			if (e == null)
				throw new IllegalArgumentException("null is forbidden");
		}
		return this.addAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		for (Object e : c) {
			if (e == null)
				throw new IllegalArgumentException("null is forbidden");
		}
		return this.retainAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		for (Object e : c) {
			if (e == null)
				throw new IllegalArgumentException("null is forbidden");
		}
		return this.removeAll(c);
	}

	@Override
	public void clear() {
		this.clear();
	}

}
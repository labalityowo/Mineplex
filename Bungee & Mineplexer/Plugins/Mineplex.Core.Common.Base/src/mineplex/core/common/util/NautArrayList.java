package mineplex.core.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class NautArrayList<Elem> implements Iterable<Elem>
{
	private ArrayList<Elem> _wrappedArrayList = new ArrayList<Elem>();
	
	public NautArrayList()
	{
	}
	
	public NautArrayList(Elem[] elements)
	{
		UtilCollections.addAll(elements, _wrappedArrayList);
	}
	
	public boolean add(Elem elem)
	{
		return _wrappedArrayList.add(elem);
	}
	
	public void add(int index, Elem elem)
	{
		_wrappedArrayList.add(index, elem);
	}
	
	public boolean addAll(Collection<? extends Elem> elements)
	{
		return _wrappedArrayList.addAll(elements);
	}
	
	public boolean addAll(int index, Collection<? extends Elem> elements)
	{
		return _wrappedArrayList.addAll(index, elements);
	}
	
	public void clear()
	{
		_wrappedArrayList.clear();
	}
	
	public boolean contains(Elem elem)
	{
		return _wrappedArrayList.contains(elem);
	}
	
	public boolean containsAll(Collection<? extends Elem> elements)
	{
		return _wrappedArrayList.containsAll(elements);
	}
	
	public Elem get(int index)
	{
		return _wrappedArrayList.get(index);
	}
	
	public boolean equals(Object o)
	{
		return _wrappedArrayList.equals(o);
	}
	
	public int hashCode()
	{
		return _wrappedArrayList.hashCode();
	}
	
	public int indexOf(Elem elem)
	{
		return _wrappedArrayList.indexOf(elem);
	}
	
	public boolean isEmpty()
	{
		return _wrappedArrayList.isEmpty();
	}
	
	public Iterator<Elem> iterator()
	{
		return _wrappedArrayList.iterator();
	}
	
	public int lastIndexOf(Elem elem)
	{
		return _wrappedArrayList.lastIndexOf(elem);
	}
	
	public ListIterator<Elem> listIterator()
	{
		return _wrappedArrayList.listIterator();
	}
	
	public ListIterator<Elem> listIterator(int index)
	{
		return _wrappedArrayList.listIterator(index);
	}
	
	public Elem remove(int index)
	{
		return _wrappedArrayList.remove(index);
	}
	
	public boolean remove(Elem element)
	{
		return _wrappedArrayList.remove(element);
	}
	
	public boolean removeAll(Collection<? extends Elem> elements)
	{
		return _wrappedArrayList.removeAll(elements);
	}
	
	public boolean retainAll(Collection<? extends Elem> elements)
	{
		return _wrappedArrayList.retainAll(elements);
	}
	
	public Elem set(int index, Elem element)
	{
		return _wrappedArrayList.set(index, element);
	}
	
	public int size()
	{
		return _wrappedArrayList.size();
	}
	
	public List<Elem> subList(int begin, int end)
	{
		return _wrappedArrayList.subList(begin, end);
	}
	
	public Object[] toArray()
	{
		return _wrappedArrayList.toArray();
	}

	public void forEach(Consumer<? super Elem> consumer)
	{
		_wrappedArrayList.forEach(consumer);
	}

	public Stream<Elem> stream()
	{
		return _wrappedArrayList.stream();
	}

	public List<Elem> getWrapped()
	{
		return _wrappedArrayList;
	}
}

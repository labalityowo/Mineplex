package mineplex.core.disguise.playerdisguise;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Set with contents that expire after X amount of time units. Essentially HashSet reimplemented with a Cache instead of HashMap.
 * @author Dan
 */
public class ExpiringSet<E> extends AbstractSet<E> implements Set<E>
{
	private transient Cache<E, Object> cache;

	private static final Object DUMMY = new Object();

	public ExpiringSet(long duration, TimeUnit unit)
	{
		this.cache = CacheBuilder.newBuilder().expireAfterWrite(duration, unit).build();
	}

	@Override
	public Iterator<E> iterator()
	{
		return cache.asMap().keySet().iterator();
	}

	@Override
	public void forEach(Consumer<? super E> action)
	{
		cache.asMap().keySet().forEach(action);
	}

	@Override
	public boolean removeIf(Predicate<? super E> filter)
	{
		return cache.asMap().keySet().removeIf(filter);
	}

	@Override
	public Spliterator<E> spliterator()
	{
		return cache.asMap().keySet().spliterator();
	}

	@Override
	public Stream<E> stream()
	{
		return cache.asMap().keySet().stream();
	}

	@Override
	public Stream<E> parallelStream()
	{
		return cache.asMap().keySet().parallelStream();
	}

	@Override
	public int size()
	{
		return (int) cache.size();
	}

	@Override
	public boolean contains(Object o)
	{
		return cache.getIfPresent(o) != null;
	}

	@Override
	public boolean add(E e)
	{
		boolean contained = contains(e);
		cache.put(e, DUMMY);
		return contained;
	}

	@Override
	public boolean remove(Object o)
	{
		boolean contained = contains(o);
		cache.invalidate(o);
		return contained;
	}

	@Override
	public void clear()
	{
		cache.invalidateAll();
	}
}

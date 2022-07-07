package mineplex.core.common;

import java.util.HashMap;
import java.util.function.Function;

public class DefaultHashMap<K, V>
{
	private HashMap<K, V> _map;
	
	private Function<K, V> _defaultPopulator;
	
	public DefaultHashMap(Function<K, V> defaultPopulator)
	{
		_map = new HashMap<K, V>();
		
		_defaultPopulator = defaultPopulator;
	}
	
	public V get(K key)
	{
		_map.putIfAbsent(key, _defaultPopulator.apply(key));
		
		return _map.get(key);
	}
	
	public void put(K key, V value)
	{
		_map.put(key, value);
	}

	public void remove(K key)
	{
		_map.remove(key);
	}
}

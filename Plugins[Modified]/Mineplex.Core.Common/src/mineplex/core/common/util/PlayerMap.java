package mineplex.core.common.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.Validate;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerMap<V> implements Map<UUID, V>
{
	private static final Object LOCK = new Object();

	private static final RemovalListener REMOVAL_LISTENER = new RemovalListener();
	private static final Set<PlayerMap<?>> ALL_PLAYER_MAPS = Collections.newSetFromMap(new WeakHashMap<>());

	static
	{
		UtilServer.RegisterEvents(REMOVAL_LISTENER);
	}

	private final Map<UUID, V> _backingMap;

	private PlayerMap(Map<UUID, V> backingMap)
	{
		this._backingMap = backingMap;

		synchronized (LOCK)
		{
			ALL_PLAYER_MAPS.add(this);
		}
	}

	public static <V> PlayerMap<V> newMap()
	{
		return new PlayerMap<>(new HashMap<>());
	}

	public static <V> PlayerMap<V> newConcurrentMap()
	{
		return new PlayerMap<>(new ConcurrentHashMap<>());
	}

	@Override
	public int size()
	{
		return _backingMap.size();
	}

	@Override
	public boolean isEmpty()
	{
		return _backingMap.isEmpty();
	}

	@Override
	@Deprecated
	public boolean containsKey(Object key)
	{
		Validate.notNull(key, "Key cannot be null");
		if (key instanceof Player)
		{
			return containsKey((Player) key);
		}
		else if (key instanceof UUID)
		{
			return containsKey((UUID) key);
		}
		throw new UnsupportedOperationException("Unknown key type: " + key.getClass().getName());
	}

	public boolean containsKey(Player key)
	{
		Validate.notNull(key, "Player cannot be null");
		return _backingMap.containsKey(key.getUniqueId());
	}

	public boolean containsKey(UUID key)
	{
		return _backingMap.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		return _backingMap.containsValue(value);
	}

	@Override
	@Deprecated
	public V get(Object key)
	{
		Validate.notNull(key, "Key cannot be null");
		if (key instanceof Player)
		{
			return get((Player) key);
		}
		else if (key instanceof UUID)
		{
			return get((UUID) key);
		}
		throw new UnsupportedOperationException("Unknown key type: " + key.getClass().getName());
	}

	public V get(Player key)
	{
		return _backingMap.get(key.getUniqueId());
	}

	public V get(UUID key)
	{
		return _backingMap.get(key);
	}

	@Override
	public V put(UUID key, V value)
	{
		return _backingMap.put(key, value);
	}

	public V put(Player key, V value)
	{
		Validate.notNull(key, "Player cannot be null");
		return put(key.getUniqueId(), value);
	}

	@Override
	@Deprecated
	public V remove(Object key)
	{
		Validate.notNull(key, "Key cannot be null");
		if (key instanceof Player)
		{
			return remove((Player) key);
		}
		else if (key instanceof UUID)
		{
			return remove((UUID) key);
		}
		throw new UnsupportedOperationException("Unknown key type: " + key.getClass().getName());
	}

	public V remove(Player key)
	{
		return _backingMap.remove(key.getUniqueId());
	}

	public V remove(UUID key)
	{
		return _backingMap.remove(key);
	}

	@Override
	public void putAll(@Nonnull Map<? extends UUID, ? extends V> m)
	{
		_backingMap.putAll(m);
	}

	@Override
	public void clear()
	{
		_backingMap.clear();
	}

	@Override
	@Nonnull
	public Set<UUID> keySet()
	{
		return _backingMap.keySet();
	}

	@Override
	@Nonnull
	public Collection<V> values()
	{
		return _backingMap.values();
	}

	@Override
	@Nonnull
	public Set<Entry<UUID, V>> entrySet()
	{
		return _backingMap.entrySet();
	}

	@Override
	public String toString()
	{
		return _backingMap.toString();
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PlayerMap<?> playerMap = (PlayerMap<?>) o;

		return _backingMap.equals(playerMap._backingMap);

	}

	@Override
	public int hashCode()
	{
		return _backingMap.hashCode();
	}

	private static class RemovalListener implements Listener
	{
		@EventHandler (priority = EventPriority.MONITOR)
		public void onQuit(PlayerQuitEvent event)
		{
			synchronized (LOCK)
			{
				for (PlayerMap<?> map : ALL_PLAYER_MAPS)
				{
					map.remove(event.getPlayer());
				}
			}
		}
	}
}